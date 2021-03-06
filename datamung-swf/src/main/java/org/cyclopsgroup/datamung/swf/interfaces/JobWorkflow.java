package org.cyclopsgroup.datamung.swf.interfaces;

import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;
import com.amazonaws.services.simpleworkflow.flow.annotations.Workflow;
import com.amazonaws.services.simpleworkflow.flow.annotations.WorkflowRegistrationOptions;
import org.cyclopsgroup.datamung.swf.types.RunJobRequest;

@Workflow
@WorkflowRegistrationOptions(
    defaultExecutionStartToCloseTimeoutSeconds = 3600L * 8,
    defaultTaskList = Constants.WORKFLOW_TASK_LIST)
public interface JobWorkflow {
  @Execute(name = "JobWorkflow", version = "1.0")
  void executeCommand(RunJobRequest request);
}
