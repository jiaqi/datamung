package org.cyclopsgroup.datadump.agent;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.cyclopsgroup.datamung.agent.AgentActivitiesImpl;
import org.cyclopsgroup.datamung.swf.types.CommandLineJob;
import org.cyclopsgroup.datamung.swf.types.JobResult;
import org.junit.Test;

public class AgentActivitiesImplTest
{
    @Test
    public void testRunJobWithPwd()
    {
        CommandLineJob job = new CommandLineJob();
        job.setCommand( "pwd" );
        JobResult result = new AgentActivitiesImpl().runJob( job );
        assertEquals( new File( "" ).getAbsoluteFile(),
                      new File( result.getStandardOutput() ).getAbsoluteFile() );
    }

    @Test
    public void testRunJobWithEcho()
    {
        CommandLineJob job = new CommandLineJob();
        job.setCommand( "echo 1 2 3" );
        JobResult result = new AgentActivitiesImpl().runJob( job );
        assertEquals( "1 2 3", result.getStandardOutput() );
    }
}
