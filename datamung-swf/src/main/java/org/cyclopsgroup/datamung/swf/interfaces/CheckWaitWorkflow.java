package org.cyclopsgroup.datamung.swf.interfaces;

import java.util.concurrent.TimeoutException;

import org.cyclopsgroup.datamung.swf.types.CheckAndWait;

import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;
import com.amazonaws.services.simpleworkflow.flow.annotations.Workflow;
import com.amazonaws.services.simpleworkflow.flow.annotations.WorkflowRegistrationOptions;

@Workflow
@WorkflowRegistrationOptions( defaultExecutionStartToCloseTimeoutSeconds = 3600L * 8, defaultTaskList = Constants.WORKFLOW_TASK_LIST )
public interface CheckWaitWorkflow
{
    @Execute( name = "CheckWaitWorkflow", version = "1.0" )
    void checkAndWait( CheckAndWait request )
        throws TimeoutException;
}
