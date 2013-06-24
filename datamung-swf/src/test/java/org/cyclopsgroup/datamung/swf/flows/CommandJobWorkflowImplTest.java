package org.cyclopsgroup.datamung.swf.flows;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.InstanceNetwork;
import org.cyclopsgroup.datamung.api.types.Job;
import org.cyclopsgroup.datamung.api.types.JobResult;
import org.cyclopsgroup.datamung.api.types.JobResultHandler;
import org.cyclopsgroup.datamung.api.types.RunJobRequest;
import org.cyclopsgroup.datamung.api.types.S3JobResultHandler;
import org.cyclopsgroup.datamung.swf.interfaces.CommandJobWorkflowClientFactoryImpl;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivities;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2Activities;
import org.cyclopsgroup.datamung.swf.interfaces.SqsActivities;
import org.cyclopsgroup.datamung.swf.types.CreateInstanceOptions;
import org.cyclopsgroup.datamung.swf.types.InstanceProfile;
import org.cyclopsgroup.datamung.swf.types.Queue;
import org.cyclopsgroup.datamung.swf.types.WorkerInstance;
import org.cyclopsgroup.datamung.swf.types.Wrapper;
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
    private static final Log LOG =
        LogFactory.getLog( CommandJobWorkflowImplTest.class );

    private Ec2Activities ec2Activities;

    private SqsActivities sqsActivities;

    private ControlActivities controlActivities;

    @Before
    public void testUpWorkflow()
    {
        ec2Activities = context.mock( Ec2Activities.class );
        sqsActivities = context.mock( SqsActivities.class );
        controlActivities = context.mock( ControlActivities.class );

        workflowTest.addWorkflowImplementationType( CheckWaitWorkflowImpl.class );
        workflowTest.addWorkflowImplementationType( CommandJobWorkflowImpl.class );
        workflowTest.addActivitiesImplementation( InvocationLoggingDecorator.decorate( Ec2Activities.class,
                                                                                       ec2Activities ) );
        workflowTest.addActivitiesImplementation( InvocationLoggingDecorator.decorate( SqsActivities.class,
                                                                                       sqsActivities ) );
        workflowTest.addActivitiesImplementation( InvocationLoggingDecorator.decorate( ControlActivities.class,
                                                                                       controlActivities ) );
    }

    @Test
    public void testRun()
    {
        final Identity identity = Identity.of( "a", "b", "c" );
        InstanceNetwork network =
            InstanceNetwork.ofVpc( "test-subset", "test-vpc" );

        RunJobRequest request = new RunJobRequest();
        request.setIdentity( identity );
        request.setNetwork( network );

        final Job job = new Job();
        job.setTimeoutSeconds( 10 );

        JobResultHandler handler = new S3JobResultHandler();
        job.setResultHandler( handler );

        final Queue queue = new Queue();
        queue.setQueueName( "http://queue" );

        final InstanceProfile profile = new InstanceProfile();

        final CreateInstanceOptions options = new CreateInstanceOptions();
        options.setNetwork( network );
        options.setProfile( profile );
        options.setUserData( "test-data" );

        context.checking( new Expectations()
        {
            {
                one( sqsActivities ).createQueue( "dmp-test", identity );
                will( returnValue( queue ) );

                one( ec2Activities ).createInstanceProfileForSqs( "dmip-test",
                                                                  queue,
                                                                  identity );
                will( returnValue( profile ) );

                one( controlActivities ).createJobWorkerUserData( queue );
                will( returnValue( "test-data" ) );

                one( ec2Activities ).launchInstance( "dmw-test", options,
                                                     identity );

                one( ec2Activities ).describeInstance( "dmw-test", identity );
                will( returnValue( new WorkerInstance().withInstanceStatus( "running" ) ) );

                one( sqsActivities ).sendJobToQueue( queue, job, identity );
                exactly( 2 ).of( sqsActivities ).pollJobResult( job, identity );
                will( returnValue( Wrapper.<JobResult> of( null ) ) );

                one( sqsActivities ).pollJobResult( job, identity );
                will( returnValue( Wrapper.of( new JobResult() ) ) );

                one( ec2Activities ).deleteInstanceProfile( profile, identity );
                one( sqsActivities ).deleteQueue( "http://queue", identity );
            }
        } );

        new CommandJobWorkflowClientFactoryImpl().getClient( "test" ).executeCommand( request );
    }
}
