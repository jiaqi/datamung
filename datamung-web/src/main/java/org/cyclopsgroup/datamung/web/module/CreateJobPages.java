package org.cyclopsgroup.datamung.web.module;

import java.io.IOException;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.cyclopsgroup.datamung.web.form.ActionType;
import org.cyclopsgroup.datamung.web.form.CredentialsAndAction;
import org.cyclopsgroup.datamung.web.form.JobInput;
import org.cyclopsgroup.datamung.web.form.SourceAndDestination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBSnapshotsRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListBucketsRequest;

@RequestMapping( "/create" )
@Controller
public class CreateJobPages
{
    @Autowired
    private AmazonS3 s3;

    @Autowired
    private AmazonRDS rds;

    private static <T extends AmazonWebServiceRequest> T decorate( T request,
                                                                   AWSCredentialsProvider creds )
    {
        request.setRequestCredentials( creds.getCredentials() );
        return request;
    }

    @RequestMapping( value = "/do_save_backup_details.html", method = RequestMethod.POST )
    public ModelAndView doSaveBackupDetails( @Valid
    SourceAndDestination form, @RequestParam( value = "inputData" )
    String inputData )
        throws IOException
    {
        JobInput input = JobInput.deserializeFrom( inputData );
        input.setSourceAndDestination( form );
        ModelAndView mav =
            new ModelAndView( "create/worker_options.vm" ).addObject( "input",
                                                                      input ).addObject( "inputData",
                                                                                         input.serializeTo() );
        return mav;
    }

    @RequestMapping( value = "/do_get_started.html", method = RequestMethod.POST )
    public ModelAndView doGetStarted( @Valid
    CredentialsAndAction form, @RequestParam( value = "inputData" )
    String inputData )
        throws IOException
    {
        JobInput input = JobInput.deserializeFrom( inputData );
        input.setActionType( form.getActionType() );
        input.setCredsAndAction( form );

        AWSCredentialsProvider creds =
            new StaticCredentialsProvider( form.toAwsCredential() );
        ModelAndView mav =
            new ModelAndView().addObject( "input", input ).addObject( "inputData",
                                                                      input.serializeTo() );

        switch ( form.getActionType() )
        {
            case BACKUP_INSTANCE:
                mav.addObject( "allInstances",
                               rds.describeDBInstances( decorate( new DescribeDBInstancesRequest(),
                                                                  creds ) ).getDBInstances() );
            case CONVERT_SNAPSHOT:
                if ( form.getActionType() == ActionType.CONVERT_SNAPSHOT )
                {
                    mav.addObject( "allSnapshots",
                                   rds.describeDBSnapshots( decorate( new DescribeDBSnapshotsRequest(),
                                                                      creds ) ).getDBSnapshots() );
                }
                mav.setViewName( "create/backup_details.vm" );
                mav.addObject( "allBuckets",
                               s3.listBuckets( decorate( new ListBucketsRequest(),
                                                         creds ) ) );
                return mav;
            default:
                throw new AssertionError( "Unexpected action type "
                    + form.getActionType() );
        }
    }

    @RequestMapping( value = { "", "/index.html", "/get_started.html" } )
    public ModelAndView showGetStarted( @RequestParam( value = "inputData", required = false )
                                        String inputData )
        throws IOException
    {
        ModelAndView mav =
            new ModelAndView( "create/get_started.vm" ).addObject( "allActionTypes",
                                                                   ActionType.values() );
        JobInput input;
        if ( StringUtils.isBlank( inputData ) )
        {
            input = new JobInput();
        }
        else
        {
            input = JobInput.deserializeFrom( inputData );
        }
        return mav.addObject( "input", input ).addObject( "inputData",
                                                          input.serializeTo() );
    }

}
