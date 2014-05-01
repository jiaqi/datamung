package org.cyclopsgroup.datamung.api;

public abstract class AbstractJobEventListener
    implements JobEventListener
{
    @Override
    public void onActionCompleted( String actionId, String result,
                                   long elapsedMillis )
    {
    }

    @Override
    public void onActionFailed( String actionId, String result,
                                String failureDetails, long elaspseMillis )
    {
    }

    @Override
    public String onActionStarted( String actionName, String description )
    {
        return null;
    }

    @Override
    public void onJobCompleted( String workflowId )
    {
    }

    @Override
    public void onJobFailed( String workflowId, Throwable cause )
    {
    }

    @Override
    public void onJobStarted( String workflowId, String reference )
    {
    }
}
