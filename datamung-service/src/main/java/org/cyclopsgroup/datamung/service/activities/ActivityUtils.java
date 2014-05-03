package org.cyclopsgroup.datamung.service.activities;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.datamung.api.types.Identity;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleResult;
import com.amazonaws.services.identitymanagement.model.DeleteRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest;
import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.Role;

class ActivityUtils
{
    private static final Log LOG = LogFactory.getLog( Ec2ActivitiesImpl.class );

    static <T extends AmazonWebServiceClient> T createClient( Class<T> clientType,
                                                              Identity identity )
    {
        Regions r = Regions.US_EAST_1;
        if ( StringUtils.isNotBlank( identity.getAwsRegionName() ) )
        {
            r = Regions.fromName( identity.getAwsRegionName() );
        }
        AWSCredentialsProvider creds =
            new StaticCredentialsProvider(
                                           new BasicAWSCredentials(
                                                                    identity.getAwsAccessKeyId(),
                                                                    identity.getAwsSecretKey() ) );

        return Region.getRegion( r ).createClient( clientType, creds, null );
    }

    static Role createRole( String roleName, AmazonIdentityManagement aim,
                            String policyTemplate,
                            Map<String, String> policyVariables,
                            String trustTemplate,
                            Map<String, String> trustVariables )
    {
        boolean roleExisted = false;
        String assumeRolePolicy = mergeDocument( trustTemplate, trustVariables );
        LOG.info( "Assume role policy will be: " + assumeRolePolicy );

        Role r;
        try
        {
            LOG.info( "Creating new role " + roleName );
            CreateRoleResult role =
                aim.createRole( new CreateRoleRequest().withRoleName( roleName ).withAssumeRolePolicyDocument( assumeRolePolicy ) );
            r = role.getRole();
        }
        catch ( EntityAlreadyExistsException e )
        {
            LOG.info( "Role already exists! " + roleName + ", ignore" );
            r =
                aim.getRole( new GetRoleRequest().withRoleName( roleName ) ).getRole();
            roleExisted = true;
        }

        String policyName = roleName + "-policy";
        boolean policyRequired = true;
        if ( roleExisted )
        {
            try
            {
                aim.getRolePolicy( new GetRolePolicyRequest().withPolicyName( policyName ).withRoleName( roleName ) );
                policyRequired = false;
                LOG.info( "Policy " + policyName + " already exists, exit" );
            }
            catch ( NoSuchEntityException e )
            {
            }
        }
        if ( policyRequired )
        {
            String rolePolicy = mergeDocument( policyTemplate, policyVariables );
            LOG.info( "Attaching policy " + policyName + " with content "
                + rolePolicy + " to role " + roleName );
            aim.putRolePolicy( new PutRolePolicyRequest().withRoleName( roleName ).withPolicyName( policyName ).withPolicyDocument( rolePolicy ) );
        }
        return r;
    }

    static void deleteRole( String roleName, AmazonIdentityManagement aim )
    {
        try
        {
            aim.getRole( new GetRoleRequest().withRoleName( roleName ) );
        }
        catch ( NoSuchEntityException e )
        {
            LOG.info( "Role " + roleName + " is already gone" );
            return;
        }
        try
        {
            aim.deleteRolePolicy( new DeleteRolePolicyRequest().withRoleName( roleName ).withPolicyName( roleName
                                                                                                             + "-policy" ) );
        }
        catch ( NoSuchEntityException e )
        {
            LOG.info( "Role policy of role " + roleName + " is already gone" );
        }

        try
        {
            aim.deleteRole( new DeleteRoleRequest().withRoleName( roleName ) );
        }
        catch ( NoSuchEntityException e )
        {
            LOG.info( "Role " + roleName + " is already gone" );
        }
    }

    static String getAccountId( AmazonIdentityManagement aim )
    {
        String[] parts = aim.getUser().getUser().getArn().split( ":" );
        return parts.length > 4 ? parts[4] : null;
    }

    private static String mergeDocument( String template,
                                         Map<String, String> variables )
    {
        try
        {
            String result =
                IOUtils.toString( ActivityUtils.class.getClassLoader().getResource( template ) );
            if ( variables == null || variables.isEmpty() )
            {
                return result;
            }
            for ( Map.Entry<String, String> entry : variables.entrySet() )
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
                + template );
        }
    }
}
