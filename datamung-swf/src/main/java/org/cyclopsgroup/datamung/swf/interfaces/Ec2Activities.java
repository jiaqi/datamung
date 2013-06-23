package org.cyclopsgroup.datamung.swf.interfaces;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.swf.types.CreateInstanceOptions;
import org.cyclopsgroup.datamung.swf.types.InstanceProfile;
import org.cyclopsgroup.datamung.swf.types.Queue;
import org.cyclopsgroup.datamung.swf.types.WorkerInstance;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.amazonaws.services.simpleworkflow.flow.annotations.ExponentialRetry;

@Activities( version = "1.0" )
@ActivityRegistrationOptions( defaultTaskStartToCloseTimeoutSeconds = 600, defaultTaskScheduleToStartTimeoutSeconds = 600, defaultTaskList = Constants.ACTIVITY_TASK_LIST )
public interface Ec2Activities
{
    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    InstanceProfile createInstanceProfileForSqs( String profileName,
                                                 Queue queue,
                                                 Identity identity );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void deleteInstanceProfile( InstanceProfile profile, Identity identity );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    WorkerInstance describeInstance( String instanceId, Identity identity );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void launchInstance( String instanceId, CreateInstanceOptions options,
                         Identity identity );

    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void terminateInstance( String instanceId, Identity identity );
}
