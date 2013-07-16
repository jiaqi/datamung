package org.cyclopsgroup.datamung.agent;

import org.apache.commons.lang.RandomStringUtils;
import org.cyclopsgroup.datamung.api.types.AgentConfig;
import org.springframework.beans.factory.FactoryBean;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;

public class AssumedSessionCredentialsFactoryBean
    implements FactoryBean<AWSCredentialsProvider>
{
    private class Provider
        implements AWSCredentialsProvider
    {
        private AWSCredentials creds;

        private long expirationDate = System.currentTimeMillis();

        private void create()
        {
            AssumeRoleResult result =
                sts.assumeRole( new AssumeRoleRequest().withRoleArn( assumedRoleArn ).withExternalId( AgentConfig.ROLE_EXTERNAL_ID ).withRoleSessionName( "rs-"
                                                                                                                                                              + RandomStringUtils.randomAlphabetic( 8 ) ) );

            synchronized ( this )
            {
                expirationDate =
                    result.getCredentials().getExpiration().getTime();
                creds =
                    new BasicSessionCredentials(
                                                 result.getCredentials().getAccessKeyId(),
                                                 result.getCredentials().getSecretAccessKey(),
                                                 result.getCredentials().getSessionToken() );
            }
        }

        /**
         * @inheritDoc
         */
        @Override
        public AWSCredentials getCredentials()
        {
            synchronized ( this )
            {
                if ( creds == null
                    || System.currentTimeMillis() > expirationDate )
                {
                    create();
                }
                return creds;
            }
        }

        /**
         * @inheritDoc
         */
        @Override
        public void refresh()
        {
            create();
        }
    }

    private final String assumedRoleArn;

    private final AWSSecurityTokenService sts;

    public AssumedSessionCredentialsFactoryBean( AWSSecurityTokenService sts,
                                                 AgentConfig config )
    {
        this.sts = sts;
        this.assumedRoleArn = config.getControllerRoleArn();
    }

    /**
     * @inheritDoc
     */
    @Override
    public AWSCredentialsProvider getObject()
    {
        return new Provider();
    }

    /**
     * @inheritDoc
     */
    @Override
    public Class<AWSCredentialsProvider> getObjectType()
    {
        return AWSCredentialsProvider.class;
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
