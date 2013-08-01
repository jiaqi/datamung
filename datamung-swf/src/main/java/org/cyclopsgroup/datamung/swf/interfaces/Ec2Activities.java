package org.cyclopsgroup.datamung.swf.interfaces;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.swf.types.CreateInstanceOptions;
import org.cyclopsgroup.datamung.swf.types.WorkerInstance;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.amazonaws.services.simpleworkflow.flow.annotations.ExponentialRetry;

@Activities( version = "1.0" )
@ActivityRegistrationOptions( defaultTaskStartToCloseTimeoutSeconds = 600, defaultTaskScheduleToStartTimeoutSeconds = 600, defaultTaskList = Constants.ACTIVITY_TASK_LIST )
public interface Ec2Activities
{
    @Description( "Create instance profile $params.get(0) under caller's account with permission to assume role $params.get(1)" )
    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void createAgentInstanceProfile( String profileName,
                                     String controllerRoleArn, Identity identity );

    @Description( "Delete instance profile $params.get(0)" )
    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void deleteInstanceProfile( String profileName, Identity identity );

    @Description( value = "Check status of EC2 instance $params.get(0)", result = "$output.instanceStatus" )
    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    WorkerInstance describeInstance( String instanceId, Identity identity );

    @Description( value = "Launch EC2 instance with under caller's account with instance profile $params.get(0).getInstanceProfileName()", result = "EC2 instance $output" )
    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    String launchInstance( CreateInstanceOptions options, Identity identity );

    @Description( "Terminate EC2 instance $params.get(0) under caller's account" )
    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    void terminateInstance( String instanceId, Identity identity );
}
