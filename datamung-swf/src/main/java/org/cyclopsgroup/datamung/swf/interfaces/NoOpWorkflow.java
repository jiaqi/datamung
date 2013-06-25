package org.cyclopsgroup.datamung.swf.interfaces;

import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;
import com.amazonaws.services.simpleworkflow.flow.annotations.Workflow;
import com.amazonaws.services.simpleworkflow.flow.annotations.WorkflowRegistrationOptions;

/**
 * This workflow is just for testing purpose. The unit test of it makes sure the
 * development environment and dependencies are setup correctly.
 */
@Workflow
@WorkflowRegistrationOptions( defaultExecutionStartToCloseTimeoutSeconds = 3600L * 8, defaultTaskList = Constants.WORKFLOW_TASK_LIST )
public interface NoOpWorkflow
{
    /**
     * Entry of workflow
     */
    @Execute( name = "NoOpWorkflow", version = "1.0" )
    void run();
}
