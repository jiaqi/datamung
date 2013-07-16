package org.cyclopsgroup.datamung.swf.flows;

import java.util.Arrays;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.InstanceNetwork;
import org.cyclopsgroup.datamung.api.types.Job;
import org.cyclopsgroup.datamung.api.types.RunJobRequest;
import org.cyclopsgroup.datamung.swf.interfaces.AgentActivities;
import org.cyclopsgroup.datamung.swf.interfaces.CommandJobWorkflowClientFactoryImpl;
import org.cyclopsgroup.datamung.swf.interfaces.Constants;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivities;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2Activities;
import org.cyclopsgroup.datamung.swf.types.CreateInstanceOptions;
import org.cyclopsgroup.datamung.swf.types.WorkerInstance;
import org.cyclopsgroup.kaufman.logging.InvocationLoggingDecorator;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.amazonaws.services.simpleworkflow.flow.junit.FlowBlockJUnit4ClassRunner;

@RunWith( FlowBlockJUnit4ClassRunner.class )
public class CommandJobWorkflowImplTest
    extends AbstractWorkflowTestCase
{
    private AgentActivities agentActivities;

    private ControlActivities controlActivities;

    private Ec2Activities ec2Activities;

    @Before
    public void setUpWorkflow()
    {
        ec2Activities = context.mock( Ec2Activities.class );
        agentActivities = context.mock( AgentActivities.class );
        controlActivities = context.mock( ControlActivities.class );

        workflowTest.addWorkflowImplementationType( CheckWaitWorkflowImpl.class );
        workflowTest.addWorkflowImplementationType( CommandJobWorkflowImpl.class );
        workflowTest.addActivitiesImplementation( Constants.ACTIVITY_TASK_LIST,
                                                  InvocationLoggingDecorator.decorate( Ec2Activities.class,
                                                                                       ec2Activities ) );
        workflowTest.addActivitiesImplementation( "dm-agent-tl-test",
                                                  InvocationLoggingDecorator.decorate( AgentActivities.class,
                                                                                       agentActivities ) );
        workflowTest.addActivitiesImplementation( Constants.ACTIVITY_TASK_LIST,
                                                  InvocationLoggingDecorator.decorate( ControlActivities.class,
                                                                                       controlActivities ) );
    }

    @Test
    public void testRun()
    {
        final Identity identity = Identity.of( "a", "b", "c" );
        InstanceNetwork network =
            InstanceNetwork.ofVpc( "test-subset", "test-vpc",
                                   Arrays.asList( "test-group" ) );

        RunJobRequest request = new RunJobRequest();
        request.setNetwork( network );

        final Job job = new Job();
        job.setTimeoutSeconds( 10 );
        job.setIdentity( identity );

        request.setJob( job );

        final CreateInstanceOptions options = new CreateInstanceOptions();
        options.setNetwork( network );
        options.setInstanceProfileName( "dm-profile-test" );
        options.setUserData( "test-data" );

        context.checking( new Expectations()
        {
            {
                one( controlActivities ).createAgentControllerRole( "dm-master-role-test",
                                                                    "dm-agent-tl-test",
                                                                    identity );
                will( returnValue( "aws:test:arn" ) );

                one( ec2Activities ).createAgentInstanceProfile( "dm-profile-test",
                                                                 "aws:test:arn",
                                                                 identity );

                one( controlActivities ).createAgentUserData( "aws:test:arn",
                                                              "dm-agent-tl-test" );
                will( returnValue( "test-data" ) );

                one( ec2Activities ).launchInstance( options, identity );
                will( returnValue( "dmw-test" ) );

                one( ec2Activities ).describeInstance( "dmw-test", identity );
                will( returnValue( new WorkerInstance().withInstanceStatus( "running" ) ) );

                one( agentActivities ).runJob( job );
                will( returnValue( null ) );

                one( ec2Activities ).terminateInstance( "dmw-test", identity );
                one( ec2Activities ).deleteInstanceProfile( "dm-profile-test",
                                                            identity );
                one( controlActivities ).deleteRole( "dm-master-role-test" );
            }
        } );

        new CommandJobWorkflowClientFactoryImpl().getClient( "test" ).executeCommand( request );
    }
}
