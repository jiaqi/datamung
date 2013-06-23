package org.cyclopsgroup.datamung.swf.interfaces;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.swf.types.Queue;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.amazonaws.services.simpleworkflow.flow.annotations.ExponentialRetry;

@Activities( version = "1.0" )
@ActivityRegistrationOptions( defaultTaskStartToCloseTimeoutSeconds = 600, defaultTaskScheduleToStartTimeoutSeconds = 600, defaultTaskList = Constants.ACTIVITY_TASK_LIST )
public interface SqsActivities
{
    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    Queue createQueue( String queueName, Identity identity );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void deleteQueue( String queueUrlj, Identity identity );
}
