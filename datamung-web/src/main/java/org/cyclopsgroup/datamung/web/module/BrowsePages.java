package org.cyclopsgroup.datamung.web.module;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.cyclopsgroup.datamung.api.DataMungService;
import org.cyclopsgroup.datamung.api.types.Workflow;
import org.cyclopsgroup.datamung.api.types.WorkflowDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping( "/browse" )
@Controller
public class BrowsePages
{
    @Autowired
    private DataMungService dataMungService;

    @RequestMapping( "/{workflowId}" )
    public ModelAndView showJobDetail( @PathVariable( "workflowId" )
    String workflowId, @RequestParam( "runId" )
    String runId )
    {
        ModelAndView mav = new ModelAndView( "browse/job_detail.vm" );
        WorkflowDetail job = dataMungService.getWorkflow( workflowId, runId );
        mav.addObject( "job", job );
        if ( job.getWorkflow().getWorkflowStatus().equals( "OPEN" ) )
        {
            mav.addObject( "refreshPage", Boolean.TRUE );
        }
        return mav;
    }

    @RequestMapping( value = { "", "/index.html", "/list_jobs.html" } )
    public ModelAndView showListJobs( @RequestParam( value = "highlight", required = false )
                                      String highlight )
    {
        ModelAndView mav =
            new ModelAndView( "browse/list_jobs.vm" ).addObject( "highlight",
                                                                 highlight );

        List<Workflow> openFlows =
            dataMungService.listWorkflows( false ).getList();
        mav.addObject( "openWorkflows", openFlows );

        List<Workflow> closedFlows =
            dataMungService.listWorkflows( true ).getList();
        mav.addObject( "closedWorkflows", closedFlows );

        if ( StringUtils.isNotBlank( highlight ) )
        {
            boolean highlightFound = false;
            if ( openFlows != null )
            {
                for ( Workflow flow : openFlows )
                {
                    if ( flow.getWorkflowId().equals( highlight ) )
                    {
                        highlightFound = true;
                        break;
                    }
                }
            }
            if ( !highlightFound && closedFlows != null )
            {
                for ( Workflow flow : closedFlows )
                {
                    if ( flow.getWorkflowId().equals( highlight ) )
                    {
                        highlightFound = true;
                        break;
                    }
                }
            }
            if ( !highlightFound )
            {
                mav.addObject( "refreshPage", Boolean.TRUE );
            }
        }
        return mav;
    }
}
