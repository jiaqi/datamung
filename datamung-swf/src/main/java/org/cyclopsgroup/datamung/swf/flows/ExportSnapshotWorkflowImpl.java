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
import org.cyclopsgroup.datamung.swf.types.DatabaseInstance;

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

    @Asynchronous
    private void dumpDatabase( Promise<DatabaseInstance> database )
    {
    }

    /**
     * @inheritDoc
     */
    @Override
    public void export( final ExportSnapshotRequest request )
    {
        this.request = request;
        final Promise<String> databaseName =
            controlActivities.createDatabaseName( request.getSnapshotName() );
        Promise<DatabaseInstance> done =
            rdsActivities.restoreSnapshot( Promise.asPromise( request.getSnapshotName() ),
                                           databaseName,
                                           Promise.asPromise( request.getIdentity() ) );
        new TryFinally( done )
        {

            @Override
            protected void doFinally()
            {
                rdsActivities.terminateInstance( databaseName,
                                                 Promise.asPromise( request.getIdentity() ) );
            }

            @Override
            protected void doTry()
            {
                Promise<Void> sourceAvailable =
                    waitUntilDatabaseAvailable( databaseName );
                Promise<DatabaseInstance> source =
                    rdsActivities.describeInstance( databaseName,
                                                    Promise.asPromise( request.getIdentity() ),
                                                    sourceAvailable );
                dumpDatabase( source );
            }
        };
    }

    @Asynchronous
    private Promise<Void> waitUntilDatabaseAvailable( Promise<String> databaseId,
                                                      Promise<?>... waitFor )
    {
        long now =
            contextProvider.getDecisionContext().getWorkflowClock().currentTimeMillis();

        CheckAndWait check = new CheckAndWait();
        check.setCheckType( CheckAndWait.Type.DATABASE_CREATION );
        // Hardcoded 1 hour wait for now
        check.setExpireOn( now + 3600 * 1000L );
        check.setIdentity( request.getIdentity() );
        check.setObjectName( databaseId.get() );
        return waitFlowFactory.getClient( "restore-db-" + databaseId.get() ).checkAndWait( check );
    }
}
