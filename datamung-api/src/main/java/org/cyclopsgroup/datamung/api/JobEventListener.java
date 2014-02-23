package org.cyclopsgroup.datamung.api;

public interface JobEventListener
{
    void onJobStarted( String workflowId, String reference );

    void onJobCompleted( String workflowId );

    void onJobFailed( String workflowId, Throwable cause );
}
