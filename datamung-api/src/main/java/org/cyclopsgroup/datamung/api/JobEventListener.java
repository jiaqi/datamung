package org.cyclopsgroup.datamung.api;

public interface JobEventListener
{
    void onActionCompleted( String actionId, String result, long elapsedMillis );

    void onActionFailed( String actionId, String result, String failureDetails,
                         long elaspseMillis );

    String onActionStarted( String actionName, String description );

    void onJobCompleted( String workflowId );

    void onJobFailed( String workflowId, Throwable cause );

    void onJobStarted( String workflowId, String reference );
}
