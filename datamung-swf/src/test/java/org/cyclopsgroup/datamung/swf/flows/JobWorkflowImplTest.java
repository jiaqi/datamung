package org.cyclopsgroup.datamung.swf.flows;

import com.amazonaws.services.simpleworkflow.flow.junit.FlowBlockJUnit4ClassRunner;
import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.WorkerOptions;
import org.cyclopsgroup.datamung.swf.interfaces.AgentActivities;
import org.cyclopsgroup.datamung.swf.interfaces.Constants;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivities;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2Activities;
import org.cyclopsgroup.datamung.swf.interfaces.JobWorkflowClientFactoryImpl;
import org.cyclopsgroup.datamung.swf.types.CommandLineJob;
import org.cyclopsgroup.datamung.swf.types.CreateInstanceOptions;
import org.cyclopsgroup.datamung.swf.types.Job;
import org.cyclopsgroup.datamung.swf.types.JobResult;
import org.cyclopsgroup.datamung.swf.types.RunJobRequest;
import org.cyclopsgroup.datamung.swf.types.WorkerInstance;
import org.cyclopsgroup.kaufman.logging.InvocationLoggingDecorator;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(FlowBlockJUnit4ClassRunner.class)
public class JobWorkflowImplTest extends AbstractWorkflowTestCase {
  private AgentActivities agentActivities;

  private ControlActivities controlActivities;

  private Ec2Activities ec2Activities;

  @Before
  public void setUpWorkflow() {
    ec2Activities = context.mock(Ec2Activities.class);
    agentActivities = context.mock(AgentActivities.class);
    controlActivities = context.mock(ControlActivities.class);

    workflowTest.addWorkflowImplementationType(CheckWaitWorkflowImpl.class);
    workflowTest.addWorkflowImplementationType(JobWorkflowImpl.class);
    workflowTest.addActivitiesImplementation(
        Constants.ACTIVITY_TASK_LIST,
        InvocationLoggingDecorator.decorate(Ec2Activities.class, ec2Activities));
    workflowTest.addActivitiesImplementation(
        "dm-agent-tl-test",
        InvocationLoggingDecorator.decorate(AgentActivities.class, agentActivities));
    workflowTest.addActivitiesImplementation(
        Constants.ACTIVITY_TASK_LIST,
        InvocationLoggingDecorator.decorate(ControlActivities.class, controlActivities));
  }

  @Test
  public void testRun() {
    final Identity identity = Identity.of("a", "b", "c");

    WorkerOptions workerOptions = new WorkerOptions();

    RunJobRequest request = new RunJobRequest();
    request.setWorkerOptions(workerOptions);

    final Job job = new CommandLineJob();

    request.setIdentity(identity);

    request.setJob(job);

    final CreateInstanceOptions options = new CreateInstanceOptions();
    options.setWorkerOptions(workerOptions);
    options.setInstanceProfileName("dm-profile-test");
    options.setUserData("test-data");

    final JobResult result = new JobResult();
    result.setStandardOutput("something");
    result.setElapsedMillis(1000);
    context.checking(
        new Expectations() {
          {
            one(controlActivities)
                .createAgentControllerRole("dm-master-role-test", "dm-agent-tl-test", identity);
            will(returnValue("aws:test:arn"));

            one(ec2Activities)
                .createAgentInstanceProfile("dm-profile-test", "aws:test:arn", identity);

            one(controlActivities).createAgentUserData("aws:test:arn", "dm-agent-tl-test");
            will(returnValue("test-data"));

            one(ec2Activities).launchInstance(options, identity);
            will(returnValue("dmw-test"));

            one(ec2Activities).describeInstance("dmw-test", identity);
            will(returnValue(new WorkerInstance().withInstanceStatus("running")));

            one(controlActivities)
                .notifyActionStarted("AgentActivities.runJob", "Start running command on instance");
            will(returnValue("action-id"));

            one(agentActivities).runJob(job);
            will(returnValue(result));

            one(controlActivities)
                .notifyActionCompleted(
                    with(equal("action-id")), with(aNonNull(String.class)), with(equal(1000L)));

            one(ec2Activities).terminateInstance("dmw-test", identity);
            one(ec2Activities).deleteInstanceProfile("dm-profile-test", identity);
            one(controlActivities).deleteRole("dm-master-role-test");
          }
        });

    new JobWorkflowClientFactoryImpl().getClient("test").executeCommand(request);
  }
}
