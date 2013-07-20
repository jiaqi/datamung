package org.cyclopsgroup.datamung.web.module;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping( "" )
@Controller
public class HomePages
{
    /**
     * This is called by health checker of load balancer
     *
     * @return A hard-coded string
     */
    @RequestMapping( "/ping" )
    public @ResponseBody
    String showPing()
    {
        return "shazoooooo!";
    }

    /**
     * The default home page
     */
    @RequestMapping( { "", "index.html", "welcome.html" } )
    public ModelAndView showWelcome()
    {
        return new ModelAndView( "welcome.vm" );
    }
}
