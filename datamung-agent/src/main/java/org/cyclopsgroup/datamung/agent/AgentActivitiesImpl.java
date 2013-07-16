package org.cyclopsgroup.datamung.agent;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.datamung.api.types.Job;
import org.cyclopsgroup.datamung.api.types.JobResult;
import org.cyclopsgroup.datamung.swf.interfaces.AgentActivities;
import org.springframework.util.StringUtils;

public class AgentActivitiesImpl
    implements AgentActivities
{
    private static Callable<String> collectOutput( final InputStream in )
    {
        return new Callable<String>()
        {
            @Override
            public String call()
                throws Exception
            {
                try
                {
                    return IOUtils.toString( in );
                }
                finally
                {
                    IOUtils.closeQuietly( in );
                }
            }
        };
    }

    private static final Log LOG =
        LogFactory.getLog( AgentActivitiesImpl.class );

    /**
     * @inheritDoc
     */
    @Override
    public JobResult runJob( Job job )
    {
        ExecutorService executor = Executors.newFixedThreadPool( 2 );

        JobResult result = new JobResult();
        result.setJob( job );
        result.setStarted( System.currentTimeMillis() );
        try
        {

            Process proc = Runtime.getRuntime().exec( job.getCommand() );
            Future<String> standardOutput =
                executor.submit( collectOutput( proc.getInputStream() ) );
            Future<String> errorOutput =
                executor.submit( collectOutput( proc.getErrorStream() ) );
            result.setExitCode( proc.waitFor() );
            result.setErrorOutput( errorOutput.get() );
            result.setStandardOutput( StringUtils.trimTrailingWhitespace( standardOutput.get() ) );
        }
        catch ( Throwable e )
        {
            result.setExitCode( -1 );
            result.setStackTrace( ExceptionUtils.getStackTrace( e ) );
            LOG.error( "Execution failed " + e.getMessage(), e );
        }
        finally
        {
            result.setElapsedMillis( System.currentTimeMillis()
                - result.getStarted() );
            executor.shutdownNow();
        }
        return result;
    }

    public static void main( String[] args )
    {
        Job job = new Job();
        job.setCommand( "whoami" );
        System.out.println( new AgentActivitiesImpl().runJob( job ) );
    }
}
