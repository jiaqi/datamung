package org.cyclopsgroup.datamung.service.core;

import org.cyclopsgroup.datamung.api.JobEventListener;

public class NoOpJobEventListener
    implements JobEventListener
{
    @Override
    public void onJobStarted( String workflowId, String runId )
    {
    }

    @Override
    public void onJobCompleted( String workflowId )
    {
    }

    @Override
    public void onJobFailed( String workflowId, Throwable cause )
    {
    }
}
