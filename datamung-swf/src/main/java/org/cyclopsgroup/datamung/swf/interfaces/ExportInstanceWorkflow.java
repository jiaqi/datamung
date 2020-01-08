package org.cyclopsgroup.datamung.swf.interfaces;

import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;
import com.amazonaws.services.simpleworkflow.flow.annotations.Workflow;
import com.amazonaws.services.simpleworkflow.flow.annotations.WorkflowRegistrationOptions;
import org.cyclopsgroup.datamung.api.types.ExportInstanceRequest;

@Workflow
@WorkflowRegistrationOptions(
    defaultExecutionStartToCloseTimeoutSeconds = 3600L * 8,
    defaultTaskList = Constants.WORKFLOW_TASK_LIST)
public interface ExportInstanceWorkflow {
  String WORKFLOW_TYPE = "ExportInstanceWorkflow";

  String WORKFLOW_VERSION = "1.0";

  @Execute(
      name = ExportInstanceWorkflow.WORKFLOW_TYPE,
      version = ExportInstanceWorkflow.WORKFLOW_VERSION)
  void export(ExportInstanceRequest request);
}
