package org.cyclopsgroup.datamung.swf.flows;

import org.cyclopsgroup.datamung.api.types.ExportInstanceRequest;
import org.cyclopsgroup.datamung.swf.interfaces.AwsCloudActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.AwsCloudActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflow;

import com.amazonaws.services.simpleworkflow.flow.DecisionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;

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

		Promise<String> snapshotName = cloudActivities
				.createSnapshotName(request.getInstanceName());

		Promise<Void> done = cloudActivities.createSnapshot(snapshotName,
				Promise.asPromise(request.getInstanceName()),
				Promise.asPromise(request.getIdentity()));
		
		
	}
}
