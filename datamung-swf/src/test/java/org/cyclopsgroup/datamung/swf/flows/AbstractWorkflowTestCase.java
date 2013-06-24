package org.cyclopsgroup.datamung.swf.flows;

import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import com.amazonaws.services.simpleworkflow.flow.junit.WorkflowTest;

public abstract class AbstractWorkflowTestCase
{
    Mockery context;

    @Before
    public void setUpMock()
    {
        context = new Mockery();
    }

    @Rule
    public WorkflowTest workflowTest = new WorkflowTest();

    @After
    public void assertMock()
    {
        context.assertIsSatisfied();
    }
}
