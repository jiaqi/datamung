package org.cyclopsgroup.datamung.swf.flows;

import com.amazonaws.services.simpleworkflow.flow.junit.FlowBlockJUnit4ClassRunner;
import org.cyclopsgroup.datamung.swf.interfaces.Constants;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivities;
import org.cyclopsgroup.datamung.swf.interfaces.NoOpWorkflowClientFactoryImpl;
import org.cyclopsgroup.kaufman.logging.InvocationLoggingDecorator;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(FlowBlockJUnit4ClassRunner.class)
public class NoOpWorkflowImplTest extends AbstractWorkflowTestCase {
  private ControlActivities control;

  @Before
  public void setUpWorkflow() {
    control = context.mock(ControlActivities.class);
    workflowTest.addActivitiesImplementation(
        Constants.ACTIVITY_TASK_LIST,
        InvocationLoggingDecorator.decorate(ControlActivities.class, control));
    workflowTest.addWorkflowImplementationType(NoOpWorkflowImpl.class);
  }

  @Test
  public void testRun() {
    context.checking(
        new Expectations() {
          {
            exactly(3).of(control).createDatabaseName("test");
            will(returnValue("bbb"));
          }
        });

    new NoOpWorkflowClientFactoryImpl().getClient("test").run();
  }
}
