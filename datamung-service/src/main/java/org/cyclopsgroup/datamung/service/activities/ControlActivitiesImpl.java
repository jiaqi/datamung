package org.cyclopsgroup.datamung.service.activities;

import org.apache.commons.lang.RandomStringUtils;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivities;
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
    public String createSnapshotName( String instanceName )
    {
        return instanceName + "-" + new DateTime().toString( "ddHHmm" );
    }

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
    public String createWorkerName( String databaseName )
    {
        return databaseName + "-worker-"
            + RandomStringUtils.randomAlphabetic( 4 );
    }
}
