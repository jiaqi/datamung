package org.cyclopsgroup.datamung.swf.interfaces;

import org.cyclopsgroup.datamung.api.types.Identity;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.amazonaws.services.simpleworkflow.flow.annotations.ExponentialRetry;

@Activities( version = "1.0" )
@ActivityRegistrationOptions( defaultTaskStartToCloseTimeoutSeconds = 600, defaultTaskScheduleToStartTimeoutSeconds = 600, defaultTaskList = Constants.ACTIVITY_TASK_LIST )
public interface ControlActivities
{
    @Description( value = "Create role $params.get(0) with permission to SWF task list $params.get(1) that caller account can assume", result = "Role with ARN $output" )
    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    String createAgentControllerRole( String roleName, String workflowTaskList,
                                      Identity clientIdentity );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    String createAgentUserData( String roleArn, String workflowTaskList );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    String createDatabaseName( String snapshotName );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    String createSnapshotName( String databaseName );

    @Description( "Delete role $params.get(0)" )
    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void deleteRole( String roleName );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void notifyJobCompleted();

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void notifyJobFailed( Throwable e );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void notifyJobStarted();
}
