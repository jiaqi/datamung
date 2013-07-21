package org.cyclopsgroup.datamung.web.module;

import java.io.IOException;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.cyclopsgroup.datamung.web.form.ActionType;
import org.cyclopsgroup.datamung.web.form.CredentialsAndAction;
import org.cyclopsgroup.datamung.web.form.JobInput;
import org.cyclopsgroup.datamung.web.form.SourceAndDestination;
import org.cyclopsgroup.datamung.web.form.WorkerInstanceOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBSnapshotsRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListBucketsRequest;

@RequestMapping( "/create" )
@Controller
public class CreateJobPages
{
    private static <T extends AmazonWebServiceRequest> T decorate( T request,
                                                                   AWSCredentials creds )
    {
        request.setRequestCredentials( creds );
        return request;
    }

    @Autowired
    private AmazonEC2 ec2;

    @Autowired
    private AmazonRDS rds;

    @Autowired
    private AmazonS3 s3;

    @RequestMapping( value = "/do_get_started.html", method = RequestMethod.POST )
    public ModelAndView doGetStarted( @Valid
    CredentialsAndAction form, @RequestParam( value = "inputData" )
    String inputData )
        throws IOException
    {
        JobInput input = JobInput.deserializeFrom( inputData );
        input.setActionType( form.getActionType() );
        input.setAwsAccessKeyId( form.getAwsAccessKeyId() );
        input.setAwsSecretKey( form.getAwsSecretKey() );
        return showBackupDetails( input );
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

    private ModelAndView showBackupDetails( JobInput input )
        throws IOException
    {
        AWSCredentials creds =
            new BasicAWSCredentials( input.getAwsAccessKeyId(),
                                     input.getAwsSecretKey() );
        ModelAndView mav =
            new ModelAndView().addObject( "input", input ).addObject( "inputData",
                                                                      input.serializeTo() );
        if ( input.getSourceAndDestination() == null )
        {
            SourceAndDestination defaultDetails = new SourceAndDestination();
            input.setSourceAndDestination( defaultDetails );
        }
        switch ( input.getActionType() )
        {
            case BACKUP_INSTANCE:
                mav.addObject( "allInstances",
                               rds.describeDBInstances( decorate( new DescribeDBInstancesRequest(),
                                                                  creds ) ).getDBInstances() );
            case CONVERT_SNAPSHOT:
                if ( input.getActionType() == ActionType.CONVERT_SNAPSHOT )
                {
                    mav.addObject( "allSnapshots",
                                   rds.describeDBSnapshots( decorate( new DescribeDBSnapshotsRequest(),
                                                                      creds ) ).getDBSnapshots() );
                }
                mav.setViewName( "create/backup_details.vm" );
                mav.addObject( "allBuckets",
                               s3.listBuckets( decorate( new ListBucketsRequest(),
                                                         creds ) ) );
                mav.addObject( "sourceAndDestination",
                               input.getSourceAndDestination() );
                return mav;
            default:
                throw new AssertionError( "Unexpected action type "
                    + input.getActionType() );
        }
    }

    @RequestMapping( value = "/backup_details.html", method = RequestMethod.POST )
    public ModelAndView showBackupDetails( @RequestParam( value = "inputData" )
    String inputData )
        throws IOException
    {
        return showBackupDetails( JobInput.deserializeFrom( inputData ) );
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

    @RequestMapping( value = "/worker_options.html", method = RequestMethod.POST )
    public ModelAndView showWorkerInstanceOptions( @RequestParam( value = "inputData" )
                                                   String inputData )
        throws IOException
    {
        JobInput input = JobInput.deserializeFrom( inputData );

        if ( input.getWorkerInstanceOptions() == null )
        {
            WorkerInstanceOptions defaultOptions = new WorkerInstanceOptions();

            input.setWorkerInstanceOptions( defaultOptions );
        }
        ModelAndView mav =
            new ModelAndView( "create/worker_options.vm" ).addObject( "input",
                                                                      input ).addObject( "inputData",
                                                                                         input.serializeTo() );
        AWSCredentials creds =
            new BasicAWSCredentials( input.getAwsAccessKeyId(),
                                     input.getAwsSecretKey() );
        mav.addObject( "allKeyPairs",
                       ec2.describeKeyPairs( decorate( new DescribeKeyPairsRequest(),
                                                       creds ) ).getKeyPairs() );
        mav.addObject( "workerOptions", input.getWorkerInstanceOptions() );
        return mav;
    }
}
