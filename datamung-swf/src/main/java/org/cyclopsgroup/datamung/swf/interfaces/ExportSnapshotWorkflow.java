package org.cyclopsgroup.datamung.swf.interfaces;

import org.cyclopsgroup.datamung.api.types.ExportSnapshotRequest;

import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;
import com.amazonaws.services.simpleworkflow.flow.annotations.Workflow;
import com.amazonaws.services.simpleworkflow.flow.annotations.WorkflowRegistrationOptions;

@Workflow
@WorkflowRegistrationOptions(defaultExecutionStartToCloseTimeoutSeconds = 3600L * 8, defaultTaskList = Constants.WORKFLOW_TASK_LIST)
public interface ExportSnapshotWorkflow {
	@Execute(name = "ExportSnapshotWorkflow", version = "1.0")
	void export(ExportSnapshotRequest request);
}