package org.cyclopsgroup.datamung.swf.interfaces;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.amazonaws.services.simpleworkflow.flow.annotations.ExponentialRetry;

@Activities( version = "1.0" )
@ActivityRegistrationOptions( defaultTaskStartToCloseTimeoutSeconds = 600, defaultTaskScheduleToStartTimeoutSeconds = 600, defaultTaskList = Constants.ACTIVITY_TASK_LIST )
public interface SqsActivities
{
    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    String createQueue( String queueName );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void deleteQueue( String queueUrl );
}
