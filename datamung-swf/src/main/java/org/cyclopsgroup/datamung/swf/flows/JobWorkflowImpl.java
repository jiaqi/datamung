package org.cyclopsgroup.datamung.swf.flows;

import org.cyclopsgroup.datamung.swf.interfaces.AgentActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.AgentActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflowClientFactory;
import org.cyclopsgroup.datamung.swf.interfaces.CheckWaitWorkflowClientFactoryImpl;
import org.cyclopsgroup.datamung.swf.interfaces.JobWorkflow;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2ActivitiesClient;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2ActivitiesClientImpl;
import org.cyclopsgroup.datamung.swf.types.CheckAndWait;
import org.cyclopsgroup.datamung.swf.types.CreateInstanceOptions;
import org.cyclopsgroup.datamung.swf.types.RunJobRequest;

import com.amazonaws.services.simpleworkflow.flow.ActivitySchedulingOptions;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.amazonaws.services.simpleworkflow.flow.core.TryFinally;

public class JobWorkflowImpl
    implements JobWorkflow
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
                                                                 request.getIdentity() );

                Promise<Void> profileCreated =
                    ec2Activities.createAgentInstanceProfile( Promise.asPromise( agentProfileName ),
                                                              masterRoleArn,
                                                              Promise.asPromise( request.getIdentity() ) );
                Promise<String> userData =
                    controlActivities.createAgentUserData( masterRoleArn,
                                                           Promise.asPromise( taskListName ) );

                Promise<String> workerId =
                    launchInstances( agentProfileName, userData, profileCreated );
                Promise<Void> set = setWorkerId( workerId );

                agentActivities.runJob( request.getJob(),
                                        new ActivitySchedulingOptions().withTaskList( taskListName ).withStartToCloseTimeoutSeconds( request.getJobTimeoutSeconds() ),
                                        set, waitUntilWorkerReady( workerId ) );
            }

            @Override
            protected void doFinally()
            {
                Promise<Void> done = Promise.Void();
                if ( workerId != null )
                {
                    done =
                        ec2Activities.terminateInstance( workerId,
                                                         request.getIdentity() );
                }
                done =
                    ec2Activities.deleteInstanceProfile( agentProfileName,
                                                         request.getIdentity(),
                                                         done );
                controlActivities.deleteRole( masterRoleName, done );
            }
        };
    }

    @Asynchronous
    private Promise<String> launchInstances( String instanceProfile,
                                             Promise<String> userData,
                                             Promise<?>... waitFor )
    {
        final CreateInstanceOptions options = new CreateInstanceOptions();
        options.setNetwork( request.getNetwork() );
        options.setInstanceProfileName( instanceProfile );
        options.setUserData( userData.get() );
        options.setKeyPairName( request.getKeyPairName() );
        return ec2Activities.launchInstance( options, request.getIdentity() );
    }

    @Asynchronous
    private Promise<Void> setWorkerId( Promise<String> workerId )
    {
        this.workerId = workerId.get();
        return Promise.Void();
    }

    @Asynchronous
    private Promise<Void> waitUntilWorkerReady( Promise<String> workerId )
    {
        CheckAndWait waitWorker = new CheckAndWait();
        waitWorker.setCheckType( CheckAndWait.Type.WORKER_LAUNCH );
        waitWorker.setIdentity( request.getIdentity() );
        waitWorker.setObjectName( workerId.get() );
        waitWorker.setWaitIntervalSeconds( 30 );
        // Given 10 minutes for instance to start up
        waitWorker.setExpireOn( 600 * 1000L + contextProvider.getDecisionContext().getWorkflowClock().currentTimeMillis() );
        return checkWaitWorkflow.getClient( "dmwf-" + workerId.get() + "-wait" ).checkAndWait( waitWorker );
    }
}
