package org.cyclopsgroup.datamung.service.activities;

import org.apache.commons.lang.RandomStringUtils;
import org.cyclopsgroup.datamung.api.types.AgentConfig;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivities;
import org.cyclopsgroup.datamung.swf.types.Queue;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;

@Component( "workflow.ControlActivities" )
public class ControlActivitiesImpl
    implements ControlActivities
{
    private final JsonDataConverter converter = new JsonDataConverter();

    /**
     * @inheritDoc
     */
    @Override
    public String createDatabaseName( String snapshotName )
    {
        return snapshotName + "-" + RandomStringUtils.randomAlphanumeric( 6 );
    }

    /**
     * @inheritDoc
     */
    @Override
    public String createJobWorkerUserData( Queue queue )
    {
        AgentConfig config = new AgentConfig();
        config.setJobQueueUrl( queue.getQueueUrl() );
        return converter.toData( config );
    }

    /**
     * @inheritDoc
     */
    @Override
    public String createSnapshotName( String instanceName )
    {
        return instanceName + "-" + new DateTime().toString( "ddHHmm" );
    }
}
