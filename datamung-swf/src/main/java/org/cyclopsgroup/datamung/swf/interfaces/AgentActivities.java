package org.cyclopsgroup.datamung.swf.interfaces;

import org.cyclopsgroup.datamung.swf.types.Job;
import org.cyclopsgroup.datamung.swf.types.JobResult;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.amazonaws.services.simpleworkflow.flow.annotations.ExponentialRetry;

@Activities( version = "1.0" )
@ActivityRegistrationOptions( defaultTaskStartToCloseTimeoutSeconds = 1800, defaultTaskScheduleToStartTimeoutSeconds = 600 )
public interface AgentActivities
{
    @Description( value = "Run command from EC2 instance", result = "Job printed out [$output.standardOutput] and returned $output.exitCode after $output.elapsedMillis milliseconds" )
    @ExponentialRetry( initialRetryIntervalSeconds = 30, maximumAttempts = 5 )
    JobResult runJob( Job job );
}
