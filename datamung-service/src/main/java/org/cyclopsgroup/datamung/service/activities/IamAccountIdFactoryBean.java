package org.cyclopsgroup.datamung.service.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.Role;

public class IamAccountIdFactoryBean
    implements FactoryBean<String>
{
    private static final Log LOG =
        LogFactory.getLog( IamAccountIdFactoryBean.class );

    private final AmazonIdentityManagement aim;

    public IamAccountIdFactoryBean( AmazonIdentityManagement aim )
    {
        this.aim = aim;
    }

    private String getAccountIdFromInstanceProfile()
        throws IOException
    {
        URLConnection con =
            new URL(
                     "http://169.254.169.254/latest/meta-data/iam/security-credentials/" ).openConnection();
        con.setConnectTimeout( 5000 );
        con.setReadTimeout( 2000 );

        String text;
        InputStream in = con.getInputStream();
        try
        {
            text = IOUtils.toString( in );
        }
        finally
        {
            IOUtils.closeQuietly( in );
        }
        LOG.info( "Read instance profile " + text + " from EC2 metadata" );
        String profileName = StringUtils.trimToNull( text );
        if ( profileName == null )
        {
            throw new IllegalStateException(
                                             "Can't read profile name from content ["
                                                 + profileName + "]" );
        }
        Role role =
            aim.getRole( new GetRoleRequest().withRoleName( profileName ) ).getRole();
        String[] parts = role.getArn().split( ":" );
        if ( parts.length < 5 )
        {
            throw new IllegalStateException( "Can't parse role ARN from "
                + role );
        }
        return parts[4];
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getObject()
    {
        LOG.info( "Try getting account ID from instance profile..." );
        try
        {
            return getAccountIdFromInstanceProfile();
        }
        catch ( IOException e )
        {
            LOG.info( "Failed(" + e.getMessage()
                + "), then try getting account ID from IAM directly..." );
            return ActivityUtils.getAccountId( aim );
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Class<String> getObjectType()
    {
        return String.class;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
