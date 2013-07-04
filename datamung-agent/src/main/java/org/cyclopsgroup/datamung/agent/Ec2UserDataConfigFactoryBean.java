package org.cyclopsgroup.datamung.agent;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.datamung.api.types.AgentConfig;
import org.springframework.beans.factory.FactoryBean;

import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;

public class Ec2UserDataConfigFactoryBean
    implements FactoryBean<AgentConfig>
{
    private static final Log LOG =
        LogFactory.getLog( Ec2UserDataConfigFactoryBean.class );

    private final JsonDataConverter converter = new JsonDataConverter();

    // A magic IP for metadata server in EC2
    private static final String USER_DATA_URL =
        "http://169.254.169.254/latest/user-data";

    /**
     * @inheritDoc
     */
    @Override
    public AgentConfig getObject()
        throws IOException
    {
        LOG.info( "Getting user data from  " + USER_DATA_URL );
        String userData = IOUtils.toString( new URL( USER_DATA_URL ) );
        LOG.info( "User data is retrieved from metadata server: " + userData );
        return converter.fromData( userData, AgentConfig.class );
    }

    /**
     * @inheritDoc
     */
    @Override
    public Class<AgentConfig> getObjectType()
    {
        return AgentConfig.class;
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
