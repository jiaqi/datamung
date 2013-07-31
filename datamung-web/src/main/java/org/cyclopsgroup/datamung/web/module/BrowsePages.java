package org.cyclopsgroup.datamung.web.module;

import org.cyclopsgroup.datamung.api.DataMungService;
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
        mav.addObject( "job", dataMungService.getWorkflow( workflowId, runId ) );
        return mav;
    }

    @RequestMapping( value = { "", "/index.html", "/list_jobs.html" } )
    public ModelAndView showListJobs( @RequestParam( value = "highlight", required = false )
                                      String highlight )
    {
        ModelAndView mav =
            new ModelAndView( "browse/list_jobs.vm" ).addObject( "highlight",
                                                                 highlight );
        mav.addObject( "openWorkflows",
                       dataMungService.listWorkflows( false ).getList() );
        mav.addObject( "closedWorkflows",
                       dataMungService.listWorkflows( true ).getList() );
        return mav;
    }
}
