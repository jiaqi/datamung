package org.cyclopsgroup.datamung.service.activities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2Activities;
import org.cyclopsgroup.datamung.swf.types.CreateInstanceOptions;
import org.cyclopsgroup.datamung.swf.types.InstanceProfile;
import org.cyclopsgroup.datamung.swf.types.Queue;
import org.cyclopsgroup.datamung.swf.types.WorkerInstance;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.CreateInstanceProfileResult;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleResult;
import com.amazonaws.services.identitymanagement.model.DeleteInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.GetInstanceProfileResult;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.RemoveRoleFromInstanceProfileRequest;

public class Ec2ActivitiesImpl
    implements Ec2Activities
{
    private static final Log LOG = LogFactory.getLog( Ec2ActivitiesImpl.class );

    @Autowired
    private AmazonEC2 ec2;

    @Autowired
    private AmazonIdentityManagement iam;

    /**
     * @inheritDoc
     */
    @Override
    public InstanceProfile createInstanceProfileForSqs( String profileName,
                                                        Queue queue,
                                                        Identity identity )
    {
        String roleName = "role-" + profileName;
        CreateRoleResult role =
            iam.createRole( ActivityUtils.decorate( new CreateRoleRequest().withRoleName( roleName ).withAssumeRolePolicyDocument( "" ),
                                                    identity ) );
        CreateInstanceProfileResult profile =
            iam.createInstanceProfile( ActivityUtils.decorate( new CreateInstanceProfileRequest().withInstanceProfileName( profileName ).withPath( role.getRole().getPath() ),
                                                               identity ) );
        InstanceProfile result = new InstanceProfile();
        result.setName( profile.getInstanceProfile().getInstanceProfileName() );
        result.setArn( profile.getInstanceProfile().getArn() );
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
            request.setIamInstanceProfile( new IamInstanceProfileSpecification().withArn( options.getProfile().getArn() ).withName( options.getProfile().getName() ) );
        }
        if ( options.getNetwork() != null
            && options.getNetwork().getVpcId() != null )
        {
            request.setSubnetId( options.getNetwork().getSubnetId() );
        }
        if ( options.getUserData() != null )
        {
            request.setUserData( options.getUserData() );
        }
        request.withMinCount( 1 ).withMaxCount( 1 );
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
}
