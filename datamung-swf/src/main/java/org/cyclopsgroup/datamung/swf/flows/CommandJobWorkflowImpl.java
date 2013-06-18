package org.cyclopsgroup.datamung.swf.flows;

import java.util.concurrent.atomic.AtomicReference;

import org.cyclopsgroup.datamung.api.types.Job;
import org.cyclopsgroup.datamung.swf.interfaces.CommandJobWorkflow;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2ActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2ActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.SqsActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.SqsActivitiesClientImpl;

import com.amazonaws.services.simpleworkflow.flow.DecisionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.amazonaws.services.simpleworkflow.flow.core.TryFinally;

public class CommandJobWorkflowImpl
    implements CommandJobWorkflow
{
    private final DecisionContextProvider contextProvider =
        new DecisionContextProviderImpl();

    private final SqsActivitiesClient sqsActivities =
        new SqsActivitiesClientImpl();

    private final Ec2ActivitiesClient ec2Activities =
        new Ec2ActivitiesClientImpl();

    /**
     * @inheritDoc
     */
    @Override
    public void executeCommand( Job job )
    {
        final String workflowId =
            contextProvider.getDecisionContext().getWorkflowContext().getWorkflowExecution().getWorkflowId();
        final AtomicReference<Promise<String>> queueUrl =
            new AtomicReference<Promise<String>>();
        new TryFinally()
        {
            @Override
            protected void doTry()
            {
                queueUrl.set( sqsActivities.createQueue( "dmqueue-"
                    + workflowId ) );
                executeCommand( "dmworker-" + workflowId, queueUrl.get() );
            }

            @Override
            protected void doFinally()
                throws Throwable
            {
                if ( queueUrl.get() != null )
                {
                    sqsActivities.deleteQueue( queueUrl.get() );
                }
            }
        };
    }

    @Asynchronous
    private void executeCommand( final String instanceId,
                                 Promise<String> queueUrl )
    {
        new TryFinally()
        {

            @Override
            protected void doTry()
            {
                ec2Activities.launchInstance( instanceId, null );
            }

            @Override
            protected void doFinally()
            {
                ec2Activities.terminateInstance( instanceId );
            }
        };
    }
}
