package org.cyclopsgroup.datamung.api.types;

import java.util.List;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class Job
    extends BaseComparableBean
{
    private List<String> arguments;

    private String command;

    private Identity identity;

    private JobResultHandler resultHandler;

    private int timeoutSeconds = 300;

    public List<String> getArguments()
    {
        return arguments;
    }

    public String getCommand()
    {
        return command;
    }

    public Identity getIdentity()
    {
        return identity;
    }

    public JobResultHandler getResultHandler()
    {
        return resultHandler;
    }

    public int getTimeoutSeconds()
    {
        return timeoutSeconds;
    }

    public void setArguments( List<String> arguments )
    {
        this.arguments = arguments;
    }

    public void setCommand( String command )
    {
        this.command = command;
    }

    public void setIdentity( Identity identity )
    {
        this.identity = identity;
    }

    public void setResultHandler( JobResultHandler resultHandler )
    {
        this.resultHandler = resultHandler;
    }

    public void setTimeoutSeconds( int timeoutSeconds )
    {
        this.timeoutSeconds = timeoutSeconds;
    }
}
