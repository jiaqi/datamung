package org.cyclopsgroup.datamung.service.activities;

import org.cyclopsgroup.datamung.swf.interfaces.ControlActivities;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProvider;
import com.amazonaws.services.simpleworkflow.flow.ActivityExecutionContextProviderImpl;

@Component( "workflow.ControlActivities" )
public class ControlActivitiesImpl
    implements ControlActivities
{
    private final ActivityExecutionContextProvider contextProvider =
        new ActivityExecutionContextProviderImpl();

    /**
     * @inheritDoc
     */
    @Override
    public String createSnapshotName( String instanceName )
    {
        return instanceName + "-" + new DateTime().toString( "yyMMdd-HHmm" );
    }

    /**
     * @inheritDoc
     */
    @Override
    public String createWorkerName( String snapshotName )
    {
        return "dm-"
            + contextProvider.getActivityExecutionContext().getWorkflowExecution().getWorkflowId().hashCode();
    }
}
