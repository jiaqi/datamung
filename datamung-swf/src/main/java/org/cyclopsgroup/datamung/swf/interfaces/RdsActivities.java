package org.cyclopsgroup.datamung.swf.interfaces;

import java.io.IOException;

import org.cyclopsgroup.datamung.api.types.DataArchive;
import org.cyclopsgroup.datamung.api.types.Identity;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.amazonaws.services.simpleworkflow.flow.annotations.ExponentialRetry;

@Activities( version = "1.0" )
@ActivityRegistrationOptions( defaultTaskStartToCloseTimeoutSeconds = 600, defaultTaskScheduleToStartTimeoutSeconds = 600, defaultTaskList = Constants.ACTIVITY_TASK_LIST )
public interface RdsActivities
{
    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void createSnapshot( String snapshotName, String instanceName,
                         Identity identity );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void deleteSnapshot( String snapshotName, Identity identity );

    @ActivityRegistrationOptions( defaultTaskStartToCloseTimeoutSeconds = 1200, defaultTaskScheduleToStartTimeoutSeconds = 600 )
    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void dumpAndArchive( String instanceName, DataArchive archive,
                         Identity identity )
        throws IOException;

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    String getInstanceStatus( String instanceName, Identity identity );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    String getSnapshotStatus( String snapshotName, Identity identity );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void restoreSnapshot( String snapshotName, String instanceName,
                          Identity identity );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void terminateInstance( String instanceName, Identity identity );
}
