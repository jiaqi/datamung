package org.cyclopsgroup.datamung.swf.flows;

import org.cyclopsgroup.datamung.api.types.ExportInstanceRequest;
import org.cyclopsgroup.datamung.api.types.ExportSnapshotRequest;
import org.cyclopsgroup.datamung.swf.interfaces.AwsRdsActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.AwsRdsActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflowClientFactory;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflowClientFactoryImpl;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflow;
import org.cyclopsgroup.datamung.swf.interfaces.ExportSnapshotWorkflowClientFactory;
import org.cyclopsgroup.datamung.swf.interfaces.ExportSnapshotWorkflowClientFactoryImpl;
import org.cyclopsgroup.datamung.swf.types.CheckAndWait;

import com.amazonaws.services.simpleworkflow.flow.DecisionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;

public class ExportInstanceWorkflowImpl
    implements ExportInstanceWorkflow
{
    private final DecisionContextProvider contextProvider =
        new DecisionContextProviderImpl();

    private final ExportSnapshotWorkflowClientFactory exportSnapshotFlowFactory =
        new ExportSnapshotWorkflowClientFactoryImpl();

    private final AwsRdsActivitiesClient rdsActivities =
        new AwsRdsActivitiesClientImpl();

    private final ControlActivitiesClient controlActivities =
        new ControlActivitiesClientImpl();

    private ExportInstanceRequest request;

    private final CheckWaitWorkflowClientFactory waitFlowFactory =
        new CheckWaitWorkflowClientFactoryImpl();

    @Asynchronous
    private Promise<ExportSnapshotRequest> createExportSnapshotRequest( Promise<String> snapshotName )
    {
        ExportSnapshotRequest snapshotRequest = new ExportSnapshotRequest();
        snapshotRequest.setDestinationArchive( request.getDestinationArchive() );
        snapshotRequest.setIdentity( request.getIdentity() );
        snapshotRequest.setSnapshotName( snapshotName.get() );
        return Promise.asPromise( snapshotRequest );
    }

    /**
     * @inheritDoc
     */
    @Override
    public void export( ExportInstanceRequest request )
    {
        this.request = request;

        Promise<String> snapshotName =
            controlActivities.createSnapshotName( request.getInstanceName() );
        Promise<Void> done =
            rdsActivities.createSnapshot( snapshotName,
                                          Promise.asPromise( request.getInstanceName() ),
                                          Promise.asPromise( request.getIdentity() ) );

        done = waitUntilSnapshotAvailable( snapshotName, done );

        Promise<ExportSnapshotRequest> snapshotRequest =
            createExportSnapshotRequest( snapshotName );
        exportSnapshotFlowFactory.getClient( "snapshot-export-" + snapshotName ).export( snapshotRequest,
                                                                                         done );
    }

    @Asynchronous
    private Promise<Void> waitUntilSnapshotAvailable( Promise<String> snapshotName,
                                                      Promise<?>... waitFor )
    {
        long now =
            contextProvider.getDecisionContext().getWorkflowClock().currentTimeMillis();

        CheckAndWait check = new CheckAndWait();
        check.setCheckType( CheckAndWait.Type.SNAPSHOT_CREATION );
        // Hardcoded 1 hour wait for now
        check.setExpireOn( now + 3600 * 1000L );
        check.setIdentity( request.getIdentity() );
        check.setObjectName( snapshotName.get() );
        return waitFlowFactory.getClient( "snapshot-creation-" + snapshotName ).checkAndWait( check );
    }
}
