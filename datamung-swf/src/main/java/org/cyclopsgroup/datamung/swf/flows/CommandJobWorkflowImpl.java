package org.cyclopsgroup.datamung.swf.flows;

import java.util.concurrent.atomic.AtomicReference;

import org.cyclopsgroup.datamung.api.types.JobResult;
import org.cyclopsgroup.datamung.api.types.RunJobRequest;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflowClientFactory;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflowClientFactoryImpl;
import org.cyclopsgroup.datamung.swf.interfaces.CommandJobWorkflow;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2ActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2ActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.SqsActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.SqsActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.types.CheckAndWait;
import org.cyclopsgroup.datamung.swf.types.CreateInstanceOptions;
import org.cyclopsgroup.datamung.swf.types.InstanceProfile;
import org.cyclopsgroup.datamung.swf.types.Queue;
import org.cyclopsgroup.datamung.swf.types.Wrapper;

import com.amazonaws.services.simpleworkflow.flow.DecisionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.amazonaws.services.simpleworkflow.flow.core.TryFinally;

public class CommandJobWorkflowImpl
    implements CommandJobWorkflow
{
    private final CheckWaitWorkflowClientFactory checkWaitWorkflow =
        new CheckWaitWorkflowClientFactoryImpl();

    private final DecisionContextProvider contextProvider =
        new DecisionContextProviderImpl();

    private final ControlActivitiesClient controlActivities =
        new ControlActivitiesClientImpl();

    private final Ec2ActivitiesClient ec2Activities =
        new Ec2ActivitiesClientImpl();

    private RunJobRequest request;

    private final SqsActivitiesClient sqsActivities =
        new SqsActivitiesClientImpl();

    private Queue queue;

    /**
     * @inheritDoc
     */
    @Override
    public void executeCommand( final RunJobRequest request )
    {
        System.out.println( "Run job request is " + request );
        this.request = request;

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
                                               request.getIdentity() );
                }
            }

            @Override
            protected void doTry()
            {
                queue.set( sqsActivities.createQueue( "dmq-" + workflowId,
                                                      request.getIdentity() ) );
                executeCommandWithQueue( queue.get() );
            }
        };
    }

    @Asynchronous
    private void executeCommandWithProfile( Promise<InstanceProfile> profile,
                                            Promise<String> userData )
    {
        final CreateInstanceOptions options = new CreateInstanceOptions();
        options.setNetwork( request.getNetwork() );
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
                ec2Activities.terminateInstance( workerId,
                                                 request.getIdentity() );
            }

            @Override
            protected void doTry()
            {
                Promise<Void> launched =
                    ec2Activities.launchInstance( workerId, options,
                                                  request.getIdentity() );
                Promise<Void> ready = waitUntilWorkerReady( workerId, launched );
                Promise<Void> sent =
                    sqsActivities.sendJobToQueue( queue, request.getJob(),
                                                  request.getIdentity(), ready );
                returnJobResult( sqsActivities.pollJobResult( request.getJob(),
                                                              request.getIdentity(),
                                                              sent ),
                                 timestamp( sent ) );
            }
        };
    }

    @Asynchronous
    private Promise<Long> timestamp( Promise<?>... waitFor )
    {
        return Promise.asPromise( contextProvider.getDecisionContext().getWorkflowClock().currentTimeMillis() );
    }

    @Asynchronous
    private Promise<Void> returnJobResult( Promise<Wrapper<JobResult>> result,
                                           Promise<Long> jobStart )
    {
        if ( result.get().getObject() != null )
        {
            return Promise.Void();
        }
        if ( contextProvider.getDecisionContext().getWorkflowClock().currentTimeMillis()
            - jobStart.get() > request.getJob().getTimeoutSeconds() * 1000L )
        {
            // Job timed out
            return Promise.Void();
        }
        Promise<Void> nextRun =
            contextProvider.getDecisionContext().getWorkflowClock().createTimer( request.getJob().getTimeoutSeconds() / 10 );
        return returnJobResult( sqsActivities.pollJobResult( request.getJob(),
                                                             request.getIdentity(),
                                                             nextRun ),
                                jobStart );
    }

    @Asynchronous
    private Promise<Void> waitUntilWorkerReady( String workerId,
                                                Promise<?>... waitFor )
    {
        CheckAndWait waitWorker = new CheckAndWait();
        waitWorker.setCheckType( CheckAndWait.Type.WORKER_LAUNCH );
        waitWorker.setIdentity( request.getIdentity() );
        waitWorker.setObjectName( workerId );
        waitWorker.setExpireOn( waitWorker.getWaitIntervalSeconds()
            * 4000L
            + contextProvider.getDecisionContext().getWorkflowClock().currentTimeMillis() );
        return checkWaitWorkflow.getClient( "workerId" + "-wait" ).checkAndWait( waitWorker );
    }

    @Asynchronous
    private void executeCommandWithQueue( final Promise<Queue> queue )
    {
        this.queue = queue.get();
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
                                                         Promise.asPromise( request.getIdentity() ) );
                }
            }

            @Override
            protected void doTry()
            {
                profile.set( ec2Activities.createInstanceProfileForSqs( "dmip-"
                    + workflowId, queue.get(), request.getIdentity() ) );
                Promise<String> userData =
                    controlActivities.createJobWorkerUserData( queue );
                executeCommandWithProfile( profile.get(), userData );
            }
        };
    }
}
