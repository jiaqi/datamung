package org.cyclopsgroup.datamung.service.activities;

import org.apache.commons.lang.RandomStringUtils;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivities;
import org.cyclopsgroup.datamung.swf.types.Queue;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

@Component( "workflow.ControlActivities" )
public class ControlActivitiesImpl
    implements ControlActivities
{
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
        // TODO Auto-generated method stub
        return null;
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
