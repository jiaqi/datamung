package org.cyclopsgroup.datamung.service.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cyclopsgroup.datamung.api.DataMungService;
import org.cyclopsgroup.datamung.api.types.ExportHandler;
import org.cyclopsgroup.datamung.api.types.ExportInstanceRequest;
import org.cyclopsgroup.datamung.api.types.ExportSnapshotRequest;
import org.cyclopsgroup.datamung.api.types.Workflow;
import org.cyclopsgroup.datamung.api.types.WorkflowActivity;
import org.cyclopsgroup.datamung.api.types.WorkflowDetail;
import org.cyclopsgroup.datamung.api.types.WorkflowList;
import org.cyclopsgroup.datamung.service.ServiceConfig;
import org.cyclopsgroup.datamung.swf.interfaces.AgentActivities;
import org.cyclopsgroup.datamung.swf.interfaces.ControlActivities;
import org.cyclopsgroup.datamung.swf.interfaces.Ec2Activities;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflowClientExternalFactory;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflowClientExternalFactoryImpl;
import org.cyclopsgroup.datamung.swf.interfaces.ExportSnapshotWorkflowClientExternalFactory;
import org.cyclopsgroup.datamung.swf.interfaces.ExportSnapshotWorkflowClientExternalFactoryImpl;
import org.cyclopsgroup.datamung.swf.interfaces.RdsActivities;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;
import com.amazonaws.services.simpleworkflow.flow.StartWorkflowOptions;
import com.amazonaws.services.simpleworkflow.model.DescribeWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.EventType;
import com.amazonaws.services.simpleworkflow.model.ExecutionTimeFilter;
import com.amazonaws.services.simpleworkflow.model.GetWorkflowExecutionHistoryRequest;
import com.amazonaws.services.simpleworkflow.model.History;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.amazonaws.services.simpleworkflow.model.ListClosedWorkflowExecutionsRequest;
import com.amazonaws.services.simpleworkflow.model.ListOpenWorkflowExecutionsRequest;
import com.amazonaws.services.simpleworkflow.model.TagFilter;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecutionDetail;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecutionInfo;

@Component( "dataMungService" )
public class DataMungServiceImpl
    implements DataMungService
{
    private static class ActivityComparator
        implements Comparator<WorkflowActivity>
    {
        @Override
        public int compare( WorkflowActivity o1, WorkflowActivity o2 )
        {
            return o1.getStartDate().compareTo( o2.getStartDate() );
        }
    }

    private static WorkflowActivity updateActivityEvent( long originalActivityId,
                                                         String workflowId,
                                                         WorkflowActivity.Status newStatus,
                                                         HistoryEvent event,
                                                         Map<String, WorkflowActivity> activityMap )
    {
        WorkflowActivity activity =
            activityMap.get( workflowId + "/" + originalActivityId );
        if ( activity == null )
        {
            return null;
        }
        activity.setActivityStatus( newStatus );
        if ( newStatus != WorkflowActivity.Status.RUNNING )
        {
            activity.setCompleteDate( new DateTime( event.getEventTimestamp() ) );
        }
        if ( newStatus == WorkflowActivity.Status.FAILED )
        {
            activity.setErrorDetail( event.getActivityTaskFailedEventAttributes().getDetails() );
            activity.setErrorReason( event.getActivityTaskFailedEventAttributes().getReason() );
        }
        else if ( newStatus == WorkflowActivity.Status.COMPLETED )
        {
            activity.setResult( event.getActivityTaskCompletedEventAttributes().getResult() );
        }
        return activity;
    }

    private final ActivityInvocationDescriber activityDescriber =
        new ActivityInvocationDescriber( AgentActivities.class,
                                         ControlActivities.class,
                                         Ec2Activities.class,
                                         RdsActivities.class );

    private final JsonDataConverter converter = new JsonDataConverter();

    private final ExportInstanceWorkflowClientExternalFactory instanceWorkflowFactory;

    private final ExportSnapshotWorkflowClientExternalFactory snapshotWorkflowFactory;

    private final String swfDomain;

    private final AmazonSimpleWorkflow swfService;

    @Autowired
    public DataMungServiceImpl( AmazonSimpleWorkflow swfService,
                                ServiceConfig config )
    {
        instanceWorkflowFactory =
            new ExportInstanceWorkflowClientExternalFactoryImpl(
                                                                 swfService,
                                                                 config.getSwfDomainName() );
        snapshotWorkflowFactory =
            new ExportSnapshotWorkflowClientExternalFactoryImpl(
                                                                 swfService,
                                                                 config.getSwfDomainName() );

        this.swfService = swfService;
        this.swfDomain = config.getSwfDomainName();
    }

    private Workflow createWorkflow( WorkflowExecutionInfo info, boolean closed )
    {
        Workflow workflow = new Workflow();
        workflow.setDomainName( swfDomain );
        workflow.setRunId( info.getExecution().getRunId() );
        workflow.setStartDate( new DateTime( info.getStartTimestamp() ) );
        if ( closed )
        {
            workflow.setTerminateDate( new DateTime( info.getCloseTimestamp() ) );
        }
        workflow.setWorkflowId( info.getExecution().getWorkflowId() );
        workflow.setWorkflowStatus( closed ? info.getCloseStatus()
                        : info.getExecutionStatus() );
        workflow.setWorkflowType( info.getWorkflowType().getName() );
        workflow.setWorkflowVersion( info.getWorkflowType().getVersion() );
        return workflow;
    }

    /**
     * @inheritDoc
     */
    @Override
    public ExportHandler exportInstance( String exportId,
                                         ExportInstanceRequest request )
    {
        StartWorkflowOptions options = new StartWorkflowOptions();
        options.setTagList( Arrays.asList( "job", "access-"
            + request.getIdentity().getAwsAccessKeyId() ) );
        instanceWorkflowFactory.getClient( exportId ).export( request, options );
        return ExportHandler.of( exportId, null );
    }

    /**
     * @inheritDoc
     */
    @Override
    public ExportHandler exportSnapshot( String exportId,
                                         ExportSnapshotRequest request )
    {
        StartWorkflowOptions options = new StartWorkflowOptions();
        options.setTagList( Arrays.asList( "job", "access-"
            + request.getIdentity().getAwsAccessKeyId() ) );
        snapshotWorkflowFactory.getClient( exportId ).export( request, options );
        return ExportHandler.of( exportId, null );
    }

    /**
     * @inheritDoc
     */
    @Override
    public WorkflowDetail getWorkflow( String workflowId, String runId )
    {
        WorkflowExecution exec =
            new WorkflowExecution().withWorkflowId( workflowId ).withRunId( runId );

        WorkflowExecutionDetail detail =
            swfService.describeWorkflowExecution( new DescribeWorkflowExecutionRequest().withDomain( swfDomain ).withExecution( exec ) );
        Workflow workflow =
            createWorkflow( detail.getExecutionInfo(),
                            detail.getExecutionInfo().getExecutionStatus().equalsIgnoreCase( "CLOSED" ) );
        WorkflowDetail result = new WorkflowDetail();
        result.setWorkflow( workflow );

        Map<String, WorkflowActivity> activityMap =
            new HashMap<String, WorkflowActivity>();
        traverseHistory( exec, activityMap );

        List<WorkflowActivity> history =
            new ArrayList<WorkflowActivity>( activityMap.values() );
        Collections.sort( history,
                          Collections.reverseOrder( new ActivityComparator() ) );
        result.setHistory( history );
        return result;
    }

    /**
     * @inheritDoc
     */
    @Override
    public WorkflowList listWorkflows( boolean closed )
    {
        DateTime now = new DateTime();
        ExecutionTimeFilter last8Hours =
            new ExecutionTimeFilter().withLatestDate( now.toDate() ).withOldestDate( now.minusHours( 8 ).toDate() );
        TagFilter tagFilter = new TagFilter().withTag( "job" );

        List<WorkflowExecutionInfo> infos;
        if ( closed )
        {
            infos =
                swfService.listClosedWorkflowExecutions( new ListClosedWorkflowExecutionsRequest().withDomain( swfDomain ).withStartTimeFilter( last8Hours ).withTagFilter( tagFilter ) ).getExecutionInfos();
        }
        else
        {
            infos =
                swfService.listOpenWorkflowExecutions( new ListOpenWorkflowExecutionsRequest().withDomain( swfDomain ).withStartTimeFilter( last8Hours ).withTagFilter( tagFilter ) ).getExecutionInfos();
        }
        List<Workflow> workflows = new ArrayList<Workflow>( infos.size() );
        for ( WorkflowExecutionInfo info : infos )
        {
            workflows.add( createWorkflow( info, closed ) );
        }
        return WorkflowList.of( workflows );
    }

    private void traverseHistory( WorkflowExecution exec,
                                  Map<String, WorkflowActivity> activityMap )
    {
        String nextToken = null;
        do
        {
            History history =
                swfService.getWorkflowExecutionHistory( new GetWorkflowExecutionHistoryRequest().withDomain( swfDomain ).withExecution( exec ).withNextPageToken( nextToken ).withMaximumPageSize( 100 ) );
            nextToken = history.getNextPageToken();

            for ( HistoryEvent event : history.getEvents() )
            {
                switch ( EventType.fromValue( event.getEventType() ) )
                {
                    case ChildWorkflowExecutionStarted:
                        traverseHistory( event.getChildWorkflowExecutionStartedEventAttributes().getWorkflowExecution(),
                                         activityMap );
                        break;
                    case ActivityTaskScheduled:
                        String activityName =
                            event.getActivityTaskScheduledEventAttributes().getActivityType().getName();
                        if ( !activityDescriber.hasActivity( activityName ) )
                        {
                            break;
                        }
                        String activityId =
                            exec.getWorkflowId() + "/" + event.getEventId();
                        WorkflowActivity activity = new WorkflowActivity();
                        activity.setActivityName( activityName );
                        activity.setActivityId( activityId );
                        activity.setActivityStatus( WorkflowActivity.Status.OPEN );
                        activity.setStartDate( new DateTime(
                                                             event.getEventTimestamp() ) );
                        Object[] params =
                            converter.fromData( event.getActivityTaskScheduledEventAttributes().getInput(),
                                                Object[].class );
                        activity.setTitle( activityDescriber.describeInvocation( activityName,
                                                                                 Arrays.asList( params ) ) );
                        activityMap.put( activityId, activity );
                        break;
                    case ActivityTaskStarted:
                        updateActivityEvent( event.getActivityTaskStartedEventAttributes().getScheduledEventId(),
                                             exec.getWorkflowId(),
                                             WorkflowActivity.Status.RUNNING,
                                             event, activityMap );
                        break;
                    case ActivityTaskCompleted:
                        activity =
                            updateActivityEvent( event.getActivityTaskCompletedEventAttributes().getScheduledEventId(),
                                                 exec.getWorkflowId(),
                                                 WorkflowActivity.Status.COMPLETED,
                                                 event, activityMap );
                        if ( activity != null )
                        {
                            activityName = activity.getActivityName();
                            Class<?> resultType =
                                activityDescriber.getActivityResultType( activityName );
                            if ( resultType == Void.class
                                || resultType == Void.TYPE )
                            {
                                activity.setResult( "void" );
                                break;
                            }
                            Object result =
                                converter.fromData( event.getActivityTaskCompletedEventAttributes().getResult(),
                                                    resultType );
                            activity.setResult( activityDescriber.describeResult( activityName,
                                                                                  result ) );
                        }
                        break;
                    case ActivityTaskFailed:
                        updateActivityEvent( event.getActivityTaskFailedEventAttributes().getScheduledEventId(),
                                             exec.getWorkflowId(),
                                             WorkflowActivity.Status.FAILED,
                                             event, activityMap );
                        break;
                    case ActivityTaskTimedOut:
                        updateActivityEvent( event.getActivityTaskTimedOutEventAttributes().getScheduledEventId(),
                                             exec.getWorkflowId(),
                                             WorkflowActivity.Status.TIMEOUT,
                                             event, activityMap );
                        break;
                    case ActivityTaskCanceled:
                        updateActivityEvent( event.getActivityTaskCanceledEventAttributes().getScheduledEventId(),
                                             exec.getWorkflowId(),
                                             WorkflowActivity.Status.CANCELED,
                                             event, activityMap );
                        break;
                    default:
                        // Nothing, ignore result
                }
            }
        }
        while ( nextToken != null );
    }
}
