package org.cyclopsgroup.datamung.swf.flows;

import com.amazonaws.services.simpleworkflow.flow.junit.WorkflowTest;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

public abstract class AbstractWorkflowTestCase {
  Mockery context;

  @Rule public WorkflowTest workflowTest = new WorkflowTest();

  @After
  public void assertMock() {
    context.assertIsSatisfied();
  }

  @Before
  public void setUpMock() {
    context = new Mockery();
  }
}
