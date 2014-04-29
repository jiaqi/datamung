package org.cyclopsgroup.datamung.service.activities;

import org.springframework.beans.factory.FactoryBean;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;

public class IamAccountIdFactoryBean
    implements FactoryBean<String>
{
    private final AmazonIdentityManagement iam;

    public IamAccountIdFactoryBean( AWSCredentials iam )
    {
        this.iam = new AmazonIdentityManagementClient( iam );
    }

    public IamAccountIdFactoryBean( AWSCredentialsProvider iam )
    {
        this.iam = new AmazonIdentityManagementClient( iam );
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getObject()
    {
        return ActivityUtils.getAccountId( iam );
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
