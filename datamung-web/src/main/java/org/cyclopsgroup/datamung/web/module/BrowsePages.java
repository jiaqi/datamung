package org.cyclopsgroup.datamung.web.module;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping( "/create" )
@Controller
public class BrowsePages
{
    @RequestMapping( value = { "", "/index.html", "/list_jobs.html" } )
    ModelAndView showListJobs( @RequestParam( value = "highlight", required = false )
                               String highlight )
    {
        ModelAndView mav = new ModelAndView( "browse/list_jobs.vm" );
        return mav;
    }
}
