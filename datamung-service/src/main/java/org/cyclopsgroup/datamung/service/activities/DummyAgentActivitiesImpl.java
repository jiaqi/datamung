package org.cyclopsgroup.datamung.service.activities;

import org.cyclopsgroup.datamung.api.types.Job;
import org.cyclopsgroup.datamung.api.types.JobResult;
import org.cyclopsgroup.datamung.swf.interfaces.AgentActivities;
import org.springframework.stereotype.Component;

@Component( "workflow.AgentActivities" )
public class DummyAgentActivitiesImpl
    implements AgentActivities
{
    @Override
    public JobResult runJob( Job job )
    {
        throw new UnsupportedOperationException(
                                                 "This method is not supposed to be called" );
    }
}
