package org.cyclopsgroup.datamung.api.types;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class JobResult
    extends BaseComparableBean
{
    private long elapsedMillis;

    private String errorOutput;

    private int exitCode;

    private Job job;

    private String runsOn;

    private String stackTrace;

    private String standardOutput;

    private long started;

    private boolean timedOut;

    public long getElapsedMillis()
    {
        return elapsedMillis;
    }

    public String getErrorOutput()
    {
        return errorOutput;
    }

    public int getExitCode()
    {
        return exitCode;
    }

    public Job getJob()
    {
        return job;
    }

    public String getRunsOn()
    {
        return runsOn;
    }

    public String getStackTrace()
    {
        return stackTrace;
    }

    public String getStandardOutput()
    {
        return standardOutput;
    }

    public long getStarted()
    {
        return started;
    }

    public boolean isTimedOut()
    {
        return timedOut;
    }

    public void setElapsedMillis( long elapsedMillis )
    {
        this.elapsedMillis = elapsedMillis;
    }

    public void setErrorOutput( String errorOutput )
    {
        this.errorOutput = errorOutput;
    }

    public void setExitCode( int exitCode )
    {
        this.exitCode = exitCode;
    }

    public void setJob( Job job )
    {
        this.job = job;
    }

    public void setRunsOn( String runsOn )
    {
        this.runsOn = runsOn;
    }

    public void setStackTrace( String stackTrace )
    {
        this.stackTrace = stackTrace;
    }

    public void setStandardOutput( String standardOutput )
    {
        this.standardOutput = standardOutput;
    }

    public void setStarted( long started )
    {
        this.started = started;
    }

    public void setTimedOut( boolean timedOut )
    {
        this.timedOut = timedOut;
    }
}
