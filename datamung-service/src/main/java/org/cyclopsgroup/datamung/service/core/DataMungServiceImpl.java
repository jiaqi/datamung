package org.cyclopsgroup.datamung.service.core;

import java.util.ArrayList;
import java.util.List;

import org.cyclopsgroup.datamung.api.DataMungService;
import org.cyclopsgroup.datamung.api.types.ExportHandler;
import org.cyclopsgroup.datamung.api.types.ExportInstanceRequest;
import org.cyclopsgroup.datamung.api.types.ExportSnapshotRequest;
import org.cyclopsgroup.datamung.api.types.Workflow;
import org.cyclopsgroup.datamung.api.types.WorkflowDetail;
import org.cyclopsgroup.datamung.api.types.WorkflowList;
import org.cyclopsgroup.datamung.service.ServiceConfig;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflow;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflowClientExternalFactory;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflowClientExternalFactoryImpl;
import org.cyclopsgroup.datamung.swf.interfaces.ExportSnapshotWorkflow;
import org.cyclopsgroup.datamung.swf.interfaces.ExportSnapshotWorkflowClientExternalFactory;
import org.cyclopsgroup.datamung.swf.interfaces.ExportSnapshotWorkflowClientExternalFactoryImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.DescribeWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.ExecutionTimeFilter;
import com.amazonaws.services.simpleworkflow.model.ListClosedWorkflowExecutionsRequest;
import com.amazonaws.services.simpleworkflow.model.ListOpenWorkflowExecutionsRequest;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecutionDetail;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecutionInfo;
import com.amazonaws.services.simpleworkflow.model.WorkflowTypeFilter;

@Component( "dataMungService" )
public class DataMungServiceImpl
    implements DataMungService
{
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
        instanceWorkflowFactory.getClient( exportId ).export( request );
        return ExportHandler.of( exportId, null );
    }

    /**
     * @inheritDoc
     */
    @Override
    public ExportHandler exportSnapshot( String exportId,
                                         ExportSnapshotRequest request )
    {
        snapshotWorkflowFactory.getClient( exportId ).export( request );
        return ExportHandler.of( exportId, null );
    }

    /**
     * @inheritDoc
     */
    @Override
    public WorkflowDetail getWorkflow( String workflowId, String runId )
    {
        WorkflowExecutionDetail detail =
            swfService.describeWorkflowExecution( new DescribeWorkflowExecutionRequest().withDomain( swfDomain ).withExecution( new WorkflowExecution().withWorkflowId( workflowId ).withRunId( runId ) ) );
        Workflow workflow =
            createWorkflow( detail.getExecutionInfo(),
                            detail.getExecutionInfo().getExecutionStatus().equalsIgnoreCase( "CLOSED" ) );
        WorkflowDetail result = new WorkflowDetail();
        result.setWorkflow( workflow );
        return result;
    }

    /**
     * @inheritDoc
     */
    @Override
    public WorkflowList listWorkflows( boolean closed )
    {
        WorkflowTypeFilter instanceTypeFilter =
            new WorkflowTypeFilter().withName( ExportInstanceWorkflow.WORKFLOW_TYPE ).withVersion( ExportInstanceWorkflow.WORKFLOW_VERSION );
        WorkflowTypeFilter snapshotTypeFilter =
            new WorkflowTypeFilter().withName( ExportSnapshotWorkflow.WORKFLOW_TYPE ).withVersion( ExportSnapshotWorkflow.WORKFLOW_VERSION );

        DateTime now = new DateTime();
        ExecutionTimeFilter last8Hours =
            new ExecutionTimeFilter().withLatestDate( now.toDate() ).withOldestDate( now.minusHours( 8 ).toDate() );

        List<WorkflowExecutionInfo> infos =
            new ArrayList<WorkflowExecutionInfo>();
        if ( closed )
        {
            infos.addAll( swfService.listClosedWorkflowExecutions( new ListClosedWorkflowExecutionsRequest().withDomain( swfDomain ).withTypeFilter( instanceTypeFilter ).withStartTimeFilter( last8Hours ) ).getExecutionInfos() );
            infos.addAll( swfService.listClosedWorkflowExecutions( new ListClosedWorkflowExecutionsRequest().withDomain( swfDomain ).withTypeFilter( snapshotTypeFilter ).withStartTimeFilter( last8Hours ) ).getExecutionInfos() );
        }
        else
        {
            infos.addAll( swfService.listOpenWorkflowExecutions( new ListOpenWorkflowExecutionsRequest().withDomain( swfDomain ).withTypeFilter( instanceTypeFilter ).withStartTimeFilter( last8Hours ) ).getExecutionInfos() );
            infos.addAll( swfService.listOpenWorkflowExecutions( new ListOpenWorkflowExecutionsRequest().withDomain( swfDomain ).withTypeFilter( snapshotTypeFilter ).withStartTimeFilter( last8Hours ) ).getExecutionInfos() );
        }

        List<Workflow> workflows = new ArrayList<Workflow>( infos.size() );
        for ( WorkflowExecutionInfo info : infos )
        {
            workflows.add( createWorkflow( info, closed ) );
        }
        return WorkflowList.of( workflows );
    }
}
