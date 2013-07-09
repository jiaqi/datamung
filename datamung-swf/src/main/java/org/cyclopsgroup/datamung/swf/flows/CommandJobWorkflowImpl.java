package org.cyclopsgroup.datamung.swf.flows;

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

    private InstanceProfile instanceProfile;

    private Queue queue;

    private RunJobRequest request;

    private String workerId;

    private final SqsActivitiesClient sqsActivities =
        new SqsActivitiesClientImpl();

    @Asynchronous
    private Promise<Void> timer( int seconds, Promise<?>... waitFor )
    {
        return contextProvider.getDecisionContext().getWorkflowClock().createTimer( seconds );
    }

    /**
     * @inheritDoc
     */
    @Override
    public void executeCommand( final RunJobRequest request )
    {
        this.request = request;
        final String workflowId =
            contextProvider.getDecisionContext().getWorkflowContext().getWorkflowExecution().getWorkflowId();
        new TryFinally()
        {
            @Override
            protected void doTry()
            {
                Promise<Queue> queue =
                    sqsActivities.createQueue( "dmq-" + workflowId,
                                               request.getJob().getIdentity() );
                Promise<Void> set = setQueue( queue );
                Promise<InstanceProfile> profile =
                    ec2Activities.createInstanceProfileForSqs( Promise.asPromise( "dmip-"
                                                                   + workflowId ),
                                                               queue,
                                                               Promise.asPromise( request.getJob().getIdentity() ),
                                                               set );
                set = setInstanceProfile( profile );
                Promise<String> userData =
                    controlActivities.createJobWorkerUserData( queue );

                // It takes a few seconds before instance profile becomes
                // available. Unfortunately, there's no deterministic way to
                // know when it is
                Promise<String> workerId =
                    runInstanceAndExecute( profile, userData, timer( 10, set ) );
                setWorkerId( workerId );
            }

            @Override
            protected void doFinally()
            {
                Promise<Void> done = Promise.Void();
                if ( workerId != null )
                {
                    done =
                        ec2Activities.terminateInstance( workerId,
                                                         request.getJob().getIdentity() );
                }
                if ( instanceProfile != null )
                {
                    done =
                        ec2Activities.deleteInstanceProfile( instanceProfile,
                                                             request.getJob().getIdentity(),
                                                             done );
                }
                if ( queue != null )
                {
                    sqsActivities.deleteQueue( queue.getQueueUrl(),
                                               request.getJob().getIdentity(),
                                               done );
                }
            }
        };
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
                                                             nextRun ),
                                jobStart );
    }

    @Asynchronous
    private Promise<String> runInstanceAndExecute( Promise<InstanceProfile> profile,
                                                   Promise<String> userData,
                                                   Promise<?>... waitFor )
    {
        Promise<Void> sent =
            sqsActivities.sendJobToQueue( queue, request.getJob() );

        final CreateInstanceOptions options = new CreateInstanceOptions();
        options.setNetwork( request.getNetwork() );
        options.setProfile( instanceProfile );
        options.setUserData( userData.get() );

        Promise<String> workerId =
            ec2Activities.launchInstance( options,
                                          request.getJob().getIdentity() );
        Promise<Void> ready = waitUntilWorkerReady( workerId );
        returnJobResult( sqsActivities.pollJobResult( request.getJob(), sent,
                                                      ready ), timestamp( sent ) );
        return workerId;
    }

    @Asynchronous
    private Promise<Void> setInstanceProfile( Promise<InstanceProfile> profile )
    {
        this.instanceProfile = profile.get();
        return Promise.Void();
    }

    @Asynchronous
    private Promise<Void> setQueue( Promise<Queue> queue )
    {
        this.queue = queue.get();
        return Promise.Void();
    }

    @Asynchronous
    private Promise<Void> setWorkerId( Promise<String> workerId )
    {
        this.workerId = workerId.get();
        return Promise.Void();
    }

    @Asynchronous
    private Promise<Long> timestamp( Promise<?>... waitFor )
    {
        return Promise.asPromise( contextProvider.getDecisionContext().getWorkflowClock().currentTimeMillis() );
    }

    @Asynchronous
    private Promise<Void> waitUntilWorkerReady( Promise<String> workerId )
    {
        CheckAndWait waitWorker = new CheckAndWait();
        waitWorker.setCheckType( CheckAndWait.Type.WORKER_LAUNCH );
        waitWorker.setIdentity( request.getJob().getIdentity() );
        waitWorker.setObjectName( workerId.get() );
        waitWorker.setExpireOn( waitWorker.getWaitIntervalSeconds()
            * 4000L
            + contextProvider.getDecisionContext().getWorkflowClock().currentTimeMillis() );
        return checkWaitWorkflow.getClient( "dmwf-" + workerId.get() + "-wait" ).checkAndWait( waitWorker );
    }
}
