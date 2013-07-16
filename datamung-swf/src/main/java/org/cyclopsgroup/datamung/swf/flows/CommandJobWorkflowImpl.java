package org.cyclopsgroup.datamung.swf.flows;

import org.cyclopsgroup.datamung.api.types.RunJobRequest;
import org.cyclopsgroup.datamung.swf.interfaces.AgentActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.AgentActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflowClientFactory;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflowClientFactoryImpl;
import org.cyclopsgroup.datamung.swf.interfaces.CommandJobWorkflow;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2ActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2ActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.types.CheckAndWait;
import org.cyclopsgroup.datamung.swf.types.CreateInstanceOptions;

import com.amazonaws.services.simpleworkflow.flow.ActivitySchedulingOptions;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.amazonaws.services.simpleworkflow.flow.core.TryFinally;

public class CommandJobWorkflowImpl
    implements CommandJobWorkflow
{
    private final AgentActivitiesClient agentActivities =
        new AgentActivitiesClientImpl();

    private final CheckWaitWorkflowClientFactory checkWaitWorkflow =
        new CheckWaitWorkflowClientFactoryImpl();

    private final DecisionContextProvider contextProvider =
        new DecisionContextProviderImpl();

    private final ControlActivitiesClient controlActivities =
        new ControlActivitiesClientImpl();

    private final Ec2ActivitiesClient ec2Activities =
        new Ec2ActivitiesClientImpl();

    private RunJobRequest request;

    private String workerId;

    /**
     * @inheritDoc
     */
    @Override
    public void executeCommand( final RunJobRequest request )
    {
        this.request = request;
        final String workflowId =
            contextProvider.getDecisionContext().getWorkflowContext().getWorkflowExecution().getWorkflowId();
        final String masterRoleName = "dm-master-role-" + workflowId;
        final String agentProfileName = "dm-profile-" + workflowId;
        new TryFinally()
        {
            @Override
            protected void doTry()
            {
                String taskListName = "dm-agent-tl-" + workflowId;
                Promise<String> masterRoleArn =
                    controlActivities.createAgentControllerRole( masterRoleName,
                                                                 taskListName,
                                                                 request.getJob().getIdentity() );

                Promise<Void> profileCreated =
                    ec2Activities.createAgentInstanceProfile( Promise.asPromise( agentProfileName ),
                                                              masterRoleArn,
                                                              Promise.asPromise( request.getJob().getIdentity() ) );
                Promise<String> userData =
                    controlActivities.createAgentUserData( masterRoleArn,
                                                           Promise.asPromise( taskListName ) );

                // It takes a few seconds before instance profile becomes
                // available. Unfortunately, there's no deterministic way to
                // know when it is
                Promise<String> workerId =
                    runInstanceAndExecute( agentProfileName, userData,
                                           timer( 10, profileCreated ) );
                setWorkerId( workerId );
                agentActivities.runJob( request.getJob(),
                                        new ActivitySchedulingOptions().withTaskList( taskListName ),
                                        workerId );
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
                done =
                    ec2Activities.deleteInstanceProfile( agentProfileName,
                                                         request.getJob().getIdentity(),
                                                         done );
                controlActivities.deleteRole( masterRoleName, done );
            }
        };
    }

    @Asynchronous
    private Promise<String> runInstanceAndExecute( String instanceProfile,
                                                   Promise<String> userData,
                                                   Promise<?>... waitFor )
    {
        final CreateInstanceOptions options = new CreateInstanceOptions();
        options.setNetwork( request.getNetwork() );
        options.setInstanceProfileName( instanceProfile );
        options.setUserData( userData.get() );
        options.setKeyPairName( request.getKeyPairName() );

        Promise<String> workerId =
            ec2Activities.launchInstance( options,
                                          request.getJob().getIdentity() );
        return and( workerId, waitUntilWorkerReady( workerId ) );
    }

    @Asynchronous
    private <T> Promise<T> and( Promise<T> result, Promise<?>... waitFor )
    {
        return result;
    }

    @Asynchronous
    private Promise<Void> setWorkerId( Promise<String> workerId )
    {
        this.workerId = workerId.get();
        return Promise.Void();
    }

    @Asynchronous
    private Promise<Void> timer( int seconds, Promise<?>... waitFor )
    {
        return contextProvider.getDecisionContext().getWorkflowClock().createTimer( seconds );
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
