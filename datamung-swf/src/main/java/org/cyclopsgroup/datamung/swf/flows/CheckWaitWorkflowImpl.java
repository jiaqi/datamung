package org.cyclopsgroup.datamung.swf.flows;

import java.util.concurrent.TimeoutException;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.swf.interfaces.AwsRdsActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.AwsRdsActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflow;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflowSelfClientImpl;
import org.cyclopsgroup.datamung.swf.types.CheckAndWait;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.amazonaws.services.simpleworkflow.flow.DecisionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;

/**
 * Workflow implementation of check and wait subworkflow
 */
public class CheckWaitWorkflowImpl
    implements CheckWaitWorkflow
{
    private int checks;

    private final DecisionContextProvider contextProvider =
        new DecisionContextProviderImpl();

    private final AwsRdsActivitiesClient rdsActivities =
        new AwsRdsActivitiesClientImpl();

    /**
     * @inheritDoc
     */
    @Override
    public void checkAndWait( CheckAndWait request )
        throws TimeoutException
    {
        doCheckAndWait( request );
    }

    @Asynchronous
    private void continueOrExit( Promise<Boolean> done, CheckAndWait request )
        throws TimeoutException
    {
        checks++;
        if ( done.get() )
        {
            return;
        }

        long now =
            contextProvider.getDecisionContext().getWorkflowClock().currentTimeMillis();
        if ( now > request.getExpireOn() )
        {
            throw new TimeoutException( "Task " + request.getCheckType()
                + " didn't complete before "
                + new DateTime( request.getExpireOn(), DateTimeZone.UTC ) );
        }

        Promise<Void> delay =
            contextProvider.getDecisionContext().getWorkflowClock().createTimer( request.getWaitIntervalSeconds() );

        if ( checks >= request.getMaxChecksPerExecution() )
        {
            new CheckWaitWorkflowSelfClientImpl().checkAndWait( request, delay );
            return;
        }
        doCheckAndWait( request, delay );
    }

    private void doCheckAndWait( CheckAndWait request, Promise<?>... waitFor )
        throws TimeoutException
    {
        Promise<Boolean> successful;
        switch ( request.getCheckType() )
        {
            case SNAPSHOT_CREATION:
                successful =
                    isSnapshotAvailable( request.getObjectName(),
                                         request.getIdentity() );
                break;
            default:
                throw new IllegalStateException( "Unexpected check type "
                    + request.getCheckType() );
        }
        continueOrExit( successful, request );
    }

    @Asynchronous
    private <T> Promise<Boolean> equals( T expects, Promise<T> actual )
    {
        return Promise.asPromise( actual.get().equals( expects ) );
    }

    @Asynchronous
    private Promise<Boolean> isSnapshotAvailable( String snapshotName,
                                                  Identity identity )
    {
        Promise<String> status =
            rdsActivities.getSnapshotStatus( snapshotName, identity );
        return equals( "available", status );
    }
}
