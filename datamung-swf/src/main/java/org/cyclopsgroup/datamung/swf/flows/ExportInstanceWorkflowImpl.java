package org.cyclopsgroup.datamung.swf.flows;

import org.cyclopsgroup.datamung.api.types.ExportInstanceRequest;
import org.cyclopsgroup.datamung.swf.interfaces.AwsCloudActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.AwsCloudActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflow;

import com.amazonaws.services.simpleworkflow.flow.DecisionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;

public class ExportInstanceWorkflowImpl implements ExportInstanceWorkflow {
	private final AwsCloudActivitiesClient cloudActivities = new AwsCloudActivitiesClientImpl();
	private final DecisionContextProvider contextProvider = new DecisionContextProviderImpl();
	private ExportInstanceRequest request;

	/**
	 * @inheritDoc
	 */
	@Override
	public void export(ExportInstanceRequest request) {
		this.request = request;

		String snapshotName = request.getInstanceName()
				+ "-"
				+ contextProvider.getDecisionContext().getWorkflowContext()
						.getWorkflowExecution().getWorkflowId();

	}
}
