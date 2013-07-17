package org.cyclopsgroup.datamung.service.activities;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.datamung.api.types.Identity;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleResult;
import com.amazonaws.services.identitymanagement.model.DeleteRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest;
import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetUserRequest;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.Role;

class ActivityUtils
{
    private static final Log LOG = LogFactory.getLog( Ec2ActivitiesImpl.class );

    static Role createRole( String roleName, AmazonIdentityManagement iam,
                            String policyTemplate,
                            Map<String, String> policyVariables,
                            String trustTemplate,
                            Map<String, String> trustVariables,
                            Identity identity )
    {
        boolean roleExisted = false;
        String assumeRolePolicy = mergeDocument( trustTemplate, trustVariables );
        LOG.info( "Assume role policy will be: " + assumeRolePolicy );

        Role r;
        try
        {
            LOG.info( "Creating new role " + roleName );
            CreateRoleResult role =
                iam.createRole( decorate( new CreateRoleRequest().withRoleName( roleName ).withAssumeRolePolicyDocument( assumeRolePolicy ),
                                          identity ) );
            r = role.getRole();
        }
        catch ( EntityAlreadyExistsException e )
        {
            LOG.info( "Role already exists! " + roleName + ", ignore" );
            r =
                iam.getRole( decorate( new GetRoleRequest().withRoleName( roleName ),
                                       identity ) ).getRole();
            roleExisted = true;
        }

        String policyName = roleName + "-policy";
        boolean policyRequired = true;
        if ( roleExisted )
        {
            try
            {
                iam.getRolePolicy( decorate( new GetRolePolicyRequest().withPolicyName( policyName ).withRoleName( roleName ),
                                             identity ) );
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
            iam.putRolePolicy( decorate( new PutRolePolicyRequest().withRoleName( roleName ).withPolicyName( policyName ).withPolicyDocument( rolePolicy ),
                                         identity ) );
        }
        return r;
    }

    static <T extends AmazonWebServiceRequest> T decorate( T request,
                                                           Identity id )
    {
        if ( id != null )
        {
            AWSCredentials creds;
            if ( id.getAwsAccessToken() == null )
            {
                creds =
                    new BasicAWSCredentials( id.getAwsAccessKeyId(),
                                             id.getAwsSecretKey() );
            }
            else
            {
                creds =
                    new BasicSessionCredentials( id.getAwsAccessKeyId(),
                                                 id.getAwsSecretKey(),
                                                 id.getAwsAccessToken() );
            }
            request.setRequestCredentials( creds );
        }
        return request;
    }

    static void deleteRole( String roleName, AmazonIdentityManagement iam,
                            Identity id )
    {
        try
        {
            iam.getRole( decorate( new GetRoleRequest().withRoleName( roleName ),
                                   id ) );
        }
        catch ( NoSuchEntityException e )
        {
            LOG.info( "Role " + roleName + " is already gone" );
            return;
        }
        try
        {
            iam.deleteRolePolicy( decorate( new DeleteRolePolicyRequest().withRoleName( roleName ).withPolicyName( roleName
                                                                                                                       + "-policy" ),
                                            id ) );
        }
        catch ( NoSuchEntityException e )
        {
            LOG.info( "Role policy of role " + roleName + " is already gone" );
        }

        try
        {
            iam.deleteRole( decorate( new DeleteRoleRequest().withRoleName( roleName ),
                                      id ) );
        }
        catch ( NoSuchEntityException e )
        {
            LOG.info( "Role " + roleName + " is already gone" );
        }
    }

    static String getAccountId( AmazonIdentityManagement iam, Identity identity )
    {
        String arn =
            iam.getUser( decorate( new GetUserRequest(), identity ) ).getUser().getArn();
        return StringUtils.removeStart( arn, "arn:aws:iam::" ).replaceAll( ":.+$",
                                                                           "" );
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
