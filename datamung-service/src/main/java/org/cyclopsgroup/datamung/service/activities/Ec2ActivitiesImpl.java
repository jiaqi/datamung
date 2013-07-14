package org.cyclopsgroup.datamung.service.activities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2Activities;
import org.cyclopsgroup.datamung.swf.types.CreateInstanceOptions;
import org.cyclopsgroup.datamung.swf.types.InstanceProfile;
import org.cyclopsgroup.datamung.swf.types.Queue;
import org.cyclopsgroup.datamung.swf.types.WorkerInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.AddRoleToInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleResult;
import com.amazonaws.services.identitymanagement.model.DeleteInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest;
import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException;
import com.amazonaws.services.identitymanagement.model.GetInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.GetInstanceProfileResult;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.RemoveRoleFromInstanceProfileRequest;

@Component( "workflow.Ec2Activities" )
public class Ec2ActivitiesImpl
    implements Ec2Activities
{
    private static final Log LOG = LogFactory.getLog( Ec2ActivitiesImpl.class );

    @Autowired
    private AmazonEC2 ec2;

    @Autowired
    private AmazonIdentityManagement iam;

    private String fetchPolicy( String templatePath,
                                Map<String, String> placeHolders )
    {
        try
        {
            String template =
                IOUtils.toString( getClass().getClassLoader().getResource( templatePath ) );
            if ( placeHolders == null || placeHolders.isEmpty() )
            {
                return template;
            }
            String result = template;
            for ( Map.Entry<String, String> entry : placeHolders.entrySet() )
            {
                result =
                    result.replaceAll( "@" + entry.getKey() + "@",
                                       entry.getValue() );
            }
            return result;
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Can't load template of policy "
                + templatePath );
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public InstanceProfile createInstanceProfileForSqs( String profileName,
                                                        Queue queue,
                                                        Identity identity )
    {
        // Create role if necessary
        String roleName = "role-" + profileName;
        boolean roleExisted = false;
        String rolePath;
        try
        {
            CreateRoleResult role =
                iam.createRole( ActivityUtils.decorate( new CreateRoleRequest().withRoleName( roleName ).withAssumeRolePolicyDocument( fetchPolicy( "datamung/agent-assume-policy.json",
                                                                                                                                                    null ) ),
                                                        identity ) );
            rolePath = role.getRole().getPath();
        }
        catch ( EntityAlreadyExistsException e )
        {
            LOG.info( "Role already exists! " + roleName + ", ignore" );
            rolePath =
                iam.getRole( ActivityUtils.decorate( new GetRoleRequest().withRoleName( roleName ),
                                                     identity ) ).getRole().getPath();
            roleExisted = true;
        }

        // Attach policy to role if necessary
        String policyName = "role-policy-" + profileName;
        boolean policyRequired = true;
        if ( roleExisted )
        {
            try
            {
                iam.getRolePolicy( new GetRolePolicyRequest().withPolicyName( policyName ).withRoleName( roleName ) );
                policyRequired = false;
            }
            catch ( NoSuchEntityException e )
            {
            }
        }
        if ( policyRequired )
        {
            Map<String, String> params = new HashMap<String, String>();
            params.put( "QUEUE_ARN", queue.getArn() );
            String policyDocument =
                fetchPolicy( "datamung/agent-instance-policy.json", params );
            iam.putRolePolicy( new PutRolePolicyRequest().withRoleName( roleName ).withPolicyName( policyName ).withPolicyDocument( policyDocument ) );
        }

        // Create instance profile and associate role if necessary
        boolean roleAssociationRequired = true;
        try
        {
            iam.createInstanceProfile( ActivityUtils.decorate( new CreateInstanceProfileRequest().withInstanceProfileName( profileName ).withPath( rolePath ),
                                                               identity ) );
        }
        catch ( EntityAlreadyExistsException e )
        {
            LOG.info( "Instance profile " + profileName + " already exists!" );
            roleAssociationRequired =
                iam.getInstanceProfile( ActivityUtils.decorate( new GetInstanceProfileRequest().withInstanceProfileName( profileName ),
                                                                identity ) ).getInstanceProfile().getRoles().isEmpty();
        }
        if ( roleAssociationRequired )
        {
            iam.addRoleToInstanceProfile( ActivityUtils.decorate( new AddRoleToInstanceProfileRequest().withInstanceProfileName( profileName ).withRoleName( "role-"
                                                                                                                                                                 + profileName ),
                                                                  identity ) );
        }
        InstanceProfile result = new InstanceProfile();
        result.setName( profileName );
        return result;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void deleteInstanceProfile( InstanceProfile profile,
                                       Identity identity )
    {
        String roleName = "role-" + profile.getName();

        try
        {
            iam.deleteRolePolicy( ActivityUtils.decorate( new DeleteRolePolicyRequest().withRoleName( roleName ).withPolicyName( "role-policy-"
                                                                                                                                     + profile.getName() ),
                                                          identity ) );
        }
        catch ( NoSuchEntityException e )
        {
            LOG.info( "Role policy " + "role-policy-" + profile.getName()
                + " is already gone" );
        }
        try
        {
            GetInstanceProfileResult profileResult =
                iam.getInstanceProfile( ActivityUtils.decorate( new GetInstanceProfileRequest().withInstanceProfileName( profile.getName() ),
                                                                identity ) );

            if ( !profileResult.getInstanceProfile().getRoles().isEmpty() )
            {
                iam.removeRoleFromInstanceProfile( ActivityUtils.decorate( new RemoveRoleFromInstanceProfileRequest().withInstanceProfileName( profile.getName() ).withRoleName( roleName ),
                                                                           identity ) );
            }

            iam.deleteInstanceProfile( ActivityUtils.decorate( new DeleteInstanceProfileRequest().withInstanceProfileName( profile.getName() ),
                                                               identity ) );
        }
        catch ( NoSuchEntityException e )
        {
            LOG.info( "Instance profile is already gone: " + profile );
        }
        try
        {
            iam.deleteRole( ActivityUtils.decorate( new DeleteRoleRequest().withRoleName( roleName ),
                                                    identity ) );
        }
        catch ( NoSuchEntityException e )
        {
            LOG.info( "Role is already gone: " + roleName );
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public WorkerInstance describeInstance( String instanceId, Identity identity )
    {
        DescribeInstancesResult result =
            ec2.describeInstances( ActivityUtils.decorate( new DescribeInstancesRequest().withInstanceIds( instanceId ),
                                                           identity ) );
        if ( result.getReservations().isEmpty() )
        {
            return null;
        }
        Reservation resv = result.getReservations().get( 0 );
        if ( resv.getInstances().isEmpty() )
        {
            return null;
        }
        Instance ins = resv.getInstances().get( 0 );
        WorkerInstance wi = new WorkerInstance();
        wi.setInstanceId( ins.getInstanceId() );
        wi.setInstanceStatus( ins.getState().getName() );
        wi.setPublicIpAddress( ins.getPublicIpAddress() );
        return wi;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String launchInstance( CreateInstanceOptions options,
                                  Identity identity )
    {
        RunInstancesRequest request = new RunInstancesRequest();
        if ( options.getProfile() != null )
        {
            request.setIamInstanceProfile( new IamInstanceProfileSpecification().withName( options.getProfile().getName() ) );
        }
        if ( options.getNetwork() != null
            && options.getNetwork().getVpcId() != null )
        {
            request.setSubnetId( options.getNetwork().getSubnetId() );
        }
        if ( options.getUserData() != null )
        {
            request.setUserData( Base64.encodeBase64URLSafeString( options.getUserData().getBytes() ) );
        }
        request.withMinCount( 1 ).withMaxCount( 1 ).withImageId( "ami-72aed21b" ).withInstanceType( InstanceType.T1Micro );
        request.setKeyName( options.getKeyPairName() );
        RunInstancesResult result =
            ec2.runInstances( ActivityUtils.decorate( request, identity ) );
        return result.getReservation().getInstances().get( 0 ).getInstanceId();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void terminateInstance( String instanceId, Identity identity )
    {
        ec2.terminateInstances( ActivityUtils.decorate( new TerminateInstancesRequest().withInstanceIds( instanceId ),
                                                        identity ) );
    }

    public static void main( String[] args )
        throws IOException
    {
        AWSCredentials creds =
            new PropertiesCredentials(
                                       new File(
                                                 "/Users/jguo/Dropbox/laogong/grpn/jguo-grpn-aws-creds.properties" ) );
        Ec2ActivitiesImpl impl = new Ec2ActivitiesImpl();
        impl.ec2 = new AmazonEC2Client( creds );

        Identity id =
            Identity.of( creds.getAWSAccessKeyId(), creds.getAWSSecretKey(),
                         null );
        CreateInstanceOptions options = new CreateInstanceOptions();
        options.setProfile( InstanceProfile.of( "dmip-test" ) );
        options.setUserData( "blah blah" );
        System.out.println( impl.launchInstance( options, id ) );
    }
}
