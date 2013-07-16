package org.cyclopsgroup.datadump.agent;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.cyclopsgroup.datamung.agent.AgentActivitiesImpl;
import org.cyclopsgroup.datamung.api.types.Job;
import org.cyclopsgroup.datamung.api.types.JobResult;
import org.junit.Test;

public class AgentActivitiesImplTest
{
    @Test
    public void testRunJobWithPwd()
    {
        Job job = new Job();
        job.setCommand( "pwd" );
        JobResult result = new AgentActivitiesImpl().runJob( job );
        assertEquals( new File( "" ).getAbsoluteFile(),
                      new File( result.getStandardOutput() ).getAbsoluteFile() );
    }

    @Test
    public void testRunJobWithEcho()
    {
        Job job = new Job();
        job.setCommand( "echo 1 2 3" );
        JobResult result = new AgentActivitiesImpl().runJob( job );
        assertEquals( "1 2 3", result.getStandardOutput() );
    }
}
