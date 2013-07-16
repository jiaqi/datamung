package org.cyclopsgroup.datamung.api.types;

import java.util.List;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class Job
    extends BaseComparableBean
{
    private List<String> arguments;

    private String command;

    private Identity identity;

    private int timeoutSeconds = 900;

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

    public void setTimeoutSeconds( int timeoutSeconds )
    {
        this.timeoutSeconds = timeoutSeconds;
    }
}
