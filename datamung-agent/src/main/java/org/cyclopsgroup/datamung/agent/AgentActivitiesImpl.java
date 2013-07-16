package org.cyclopsgroup.datamung.agent;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.datamung.api.types.Job;
import org.cyclopsgroup.datamung.api.types.JobResult;
import org.cyclopsgroup.datamung.swf.interfaces.AgentActivities;

public class AgentActivitiesImpl
    implements AgentActivities
{
    private static final Log LOG =
        LogFactory.getLog( AgentActivitiesImpl.class );

    /**
     * @inheritDoc
     */
    @Override
    public JobResult runJob( Job job )
    {
        JobResult result = new JobResult();
        result.setJob( job );
        result.setStarted( System.currentTimeMillis() );
        try
        {
            Process proc =
                Runtime.getRuntime().exec( job.getCommand(),
                                           job.getArguments().toArray( ArrayUtils.EMPTY_STRING_ARRAY ) );

            result.setExitCode( proc.waitFor() );
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
        }
        return result;
    }
}
