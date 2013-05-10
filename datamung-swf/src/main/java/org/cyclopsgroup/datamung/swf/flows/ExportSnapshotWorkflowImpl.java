package org.cyclopsgroup.datamung.swf.flows;

import org.cyclopsgroup.datamung.api.types.ExportSnapshotRequest;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflowClientFactory;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflowClientFactoryImpl;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.ExportSnapshotWorkflow;
import org.cyclopsgroup.datamung.swf.interfaces.RdsActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.RdsActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.types.CheckAndWait;

import com.amazonaws.services.simpleworkflow.flow.DecisionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.amazonaws.services.simpleworkflow.flow.core.TryFinally;

public class ExportSnapshotWorkflowImpl
    implements ExportSnapshotWorkflow
{
    private final DecisionContextProvider contextProvider =
        new DecisionContextProviderImpl();

    private final ControlActivitiesClient controlActivities =
        new ControlActivitiesClientImpl();

    private final RdsActivitiesClient rdsActivities =
        new RdsActivitiesClientImpl();

    private ExportSnapshotRequest request;

    private final CheckWaitWorkflowClientFactory waitFlowFactory =
        new CheckWaitWorkflowClientFactoryImpl();

    /**
     * @inheritDoc
     */
    @Override
    public void export( final ExportSnapshotRequest request )
    {
        this.request = request;
        final Promise<String> tempInstanceName =
            controlActivities.createTempInstanceName( request.getSnapshotName() );
        Promise<Void> done =
            rdsActivities.restoreSnapshot( Promise.asPromise( request.getSnapshotName() ),
                                           tempInstanceName,
                                           Promise.asPromise( request.getIdentity() ) );
        new TryFinally( done )
        {

            @Override
            protected void doTry()
            {
                Promise<Void> done =
                    waitUntilInstanceAvailable( tempInstanceName );
                rdsActivities.dumpAndArchive( tempInstanceName,
                                              Promise.asPromise( request.getDestinationArchive() ),
                                              Promise.asPromise( request.getIdentity() ),
                                              done );
            }

            @Override
            protected void doFinally()
            {
                rdsActivities.terminateInstance( tempInstanceName,
                                                 Promise.asPromise( request.getIdentity() ) );
            }
        };
    }

    @Asynchronous
    private Promise<Void> waitUntilInstanceAvailable( Promise<String> instanceName,
                                                      Promise<?>... waitFor )
    {
        long now =
            contextProvider.getDecisionContext().getWorkflowClock().currentTimeMillis();

        CheckAndWait check = new CheckAndWait();
        check.setCheckType( CheckAndWait.Type.INSTANCE_CREATION );
        // Hardcoded 1 hour wait for now
        check.setExpireOn( now + 3600 * 1000L );
        check.setIdentity( request.getIdentity() );
        check.setObjectName( instanceName.get() );
        return waitFlowFactory.getClient( "instance-creation-"
                                              + instanceName.get() ).checkAndWait( check );
    }
}
