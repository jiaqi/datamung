package org.cyclopsgroup.datamung.web.module;

import java.io.IOException;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.cyclopsgroup.datamung.web.form.CredentialsAndAction;
import org.cyclopsgroup.datamung.web.form.JobInput;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.s3.AmazonS3Client;

@RequestMapping( "" )
@Controller
public class HomePages
{
    @RequestMapping( value = "/do_get_started.html", method = RequestMethod.POST )
    public ModelAndView doGetStarted( @Valid CredentialsAndAction form,
                                      @RequestParam( value = "inputData" ) String inputData )
        throws IOException
    {
        JobInput input = JobInput.deserializeFrom( inputData );
        input.setActionType( form.getActionType() );
        input.setAwsRegion( form.getAwsRegion() );
        input.setAwsAccessKeyId( form.getAwsAccessKeyId() );
        input.setAwsSecretKey( form.getAwsSecretKey() );

        AWSCredentialsProvider creds =
            new StaticCredentialsProvider( form.toAwsCredential() );
        ModelAndView mav =
            new ModelAndView().addObject( "input", input ).addObject( "inputData",
                                                                      input.serializeTo() );

        ClientConfiguration config = new ClientConfiguration();
        Region region = Region.getRegion( form.getAwsRegion() );
        switch ( form.getActionType() )
        {
            case BACKUP_INSTANCE:
                mav.addObject( "allInstances",
                               region.createClient( AmazonRDSClient.class,
                                                    creds, config ).describeDBInstances().getDBInstances() );
            case CONVERT_SNAPSHOT:
                if ( form.getActionType() == CredentialsAndAction.ActionType.CONVERT_SNAPSHOT )
                {
                    mav.addObject( "allSnapshots",
                                   region.createClient( AmazonRDSClient.class,
                                                        creds, config ).describeDBSnapshots().getDBSnapshots() );
                }
                mav.setViewName( "backup_details.vm" );
                mav.addObject( "allBuckets",
                               region.createClient( AmazonS3Client.class,
                                                    creds, config ).listBuckets() );
                return mav;
            default:
                throw new AssertionError( "Unexpected action type "
                    + form.getActionType() );
        }
    }

    @RequestMapping( "/get_started.html" )
    public ModelAndView showGetStarted( @RequestParam( value = "inputData", required = false ) String inputData )
        throws IOException
    {
        ModelAndView mav =
            new ModelAndView( "get_started.vm" ).addObject( "allActionTypes",
                                                            CredentialsAndAction.ActionType.values() ).addObject( "allRegions",
                                                                                                                  Regions.values() );
        JobInput input;
        if ( StringUtils.isBlank( inputData ) )
        {
            input = new JobInput();
            input.setAwsRegion( Regions.US_EAST_1 );
        }
        else
        {
            input = JobInput.deserializeFrom( inputData );
        }
        return mav.addObject( "input", input ).addObject( "inputData",
                                                          input.serializeTo() );
    }

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
