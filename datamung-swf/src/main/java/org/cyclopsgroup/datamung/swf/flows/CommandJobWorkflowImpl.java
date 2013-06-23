package org.cyclopsgroup.datamung.swf.flows;

import java.util.concurrent.atomic.AtomicReference;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.InstanceNetwork;
import org.cyclopsgroup.datamung.api.types.RunJobRequest;
import org.cyclopsgroup.datamung.swf.interfaces.CommandJobWorkflow;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2ActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2ActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.SqsActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.SqsActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.types.CreateInstanceOptions;
import org.cyclopsgroup.datamung.swf.types.InstanceProfile;
import org.cyclopsgroup.datamung.swf.types.Queue;

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

    private final ControlActivitiesClient controlActivities =
        new ControlActivitiesClientImpl();

    private final Ec2ActivitiesClient ec2Activities =
        new Ec2ActivitiesClientImpl();

    private Identity identity;

    private InstanceNetwork network;

    private final SqsActivitiesClient sqsActivities =
        new SqsActivitiesClientImpl();

    /**
     * @inheritDoc
     */
    @Override
    public void executeCommand( RunJobRequest request )
    {
        this.identity = request.getIdentity();
        this.network = request.getNetwork();

        final String workflowId =
            contextProvider.getDecisionContext().getWorkflowContext().getWorkflowExecution().getWorkflowId();
        final AtomicReference<Promise<Queue>> queue =
            new AtomicReference<Promise<Queue>>( null );
        new TryFinally()
        {
            @Override
            protected void doFinally()
            {
                if ( queue.get() != null )
                {
                    sqsActivities.deleteQueue( queue.get().get().getQueueUrl(),
                                               identity );
                }
            }

            @Override
            protected void doTry()
            {
                queue.set( sqsActivities.createQueue( "dmq-" + workflowId,
                                                      identity ) );
                executeCommandWithQueue( queue.get() );
            }
        };
    }

    @Asynchronous
    private void executeCommandWithProfile( Promise<InstanceProfile> profile,
                                            Promise<String> userData )
    {
        final CreateInstanceOptions options = new CreateInstanceOptions();
        options.setIdentity( identity );
        options.setNetwork( network );
        options.setProfile( profile.get() );
        options.setUserData( userData.get() );

        final String workerId =
            "dmw-"
                + contextProvider.getDecisionContext().getWorkflowContext().getWorkflowExecution().getWorkflowId();
        new TryFinally()
        {
            @Override
            protected void doFinally()
            {
                ec2Activities.terminateInstance( workerId, identity );
            }

            @Override
            protected void doTry()
            {
                ec2Activities.launchInstance( workerId, options, identity );
            }
        };
    }

    @Asynchronous
    private void executeCommandWithQueue( final Promise<Queue> queue )
    {
        final String workflowId =
            contextProvider.getDecisionContext().getWorkflowContext().getWorkflowExecution().getWorkflowId();
        final AtomicReference<Promise<InstanceProfile>> profile =
            new AtomicReference<Promise<InstanceProfile>>();

        new TryFinally()
        {
            @Override
            protected void doFinally()
            {
                if ( profile.get() != null )
                {
                    ec2Activities.deleteInstanceProfile( profile.get(),
                                                         Promise.asPromise( identity ) );
                }
            }

            @Override
            protected void doTry()
            {
                profile.set( ec2Activities.createInstanceProfileForSqs( "dmip-"
                    + workflowId, queue.get(), identity ) );
                Promise<String> userData =
                    controlActivities.createJobWorkerUserData( queue );
                executeCommandWithProfile( profile.get(), userData );
            }
        };
    }
}
