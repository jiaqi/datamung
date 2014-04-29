package org.cyclopsgroup.datamung.web.module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.cyclopsgroup.datamung.api.DataMungService;
import org.cyclopsgroup.datamung.api.types.ExportInstanceRequest;
import org.cyclopsgroup.datamung.api.types.ExportSnapshotRequest;
import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.S3DataArchive;
import org.cyclopsgroup.datamung.api.types.WorkerOptions;
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
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DBSnapshot;
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
    private DataMungService dataMungService;

    @Autowired
    private AmazonEC2 ec2;

    @Autowired
    private AmazonRDS rds;

    @Autowired
    private AmazonS3 s3;

    @RequestMapping( value = "/do_get_started.html", method = RequestMethod.POST )
    public ModelAndView doGetStarted( @Valid CredentialsAndAction form,
                                      @RequestParam( "inputData" ) String inputData )
        throws IOException
    {
        JobInput input = JobInput.deserializeFrom( inputData );
        input.setActionType( form.getActionType() );
        input.setAwsAccessKeyId( form.getAwsAccessKeyId() );
        input.setAwsSecretKey( form.getAwsSecretKey() );
        return showBackupDetails( input );
    }

    @RequestMapping( value = "/do_save_backup_details.html", method = RequestMethod.POST )
    public ModelAndView doSaveBackupDetails( @Valid SourceAndDestination form,
                                             @RequestParam( "inputData" ) String inputData )
        throws IOException
    {
        JobInput input = JobInput.deserializeFrom( inputData );
        input.setSourceAndDestination( form );
        return showWorkerInstanceOptions( input );
    }

    @RequestMapping( value = "/do_save_worker_options.html", method = RequestMethod.POST )
    public ModelAndView doSaveWorkerOptions( @Valid WorkerInstanceOptions form,
                                             @RequestParam( "inputData" ) String inputData )
        throws IOException
    {
        JobInput input = JobInput.deserializeFrom( inputData );
        input.setWorkerInstanceOptions( form );
        return showConfirm( input );
    }

    @RequestMapping( value = "/do_start_job.html", method = RequestMethod.POST )
    public String doStartJob( @RequestParam( "inputData" ) String inputData )
        throws IOException
    {
        JobInput input = JobInput.deserializeFrom( inputData );
        String workflowId = RandomStringUtils.randomAlphanumeric( 8 );

        S3DataArchive dest =
            S3DataArchive.of( input.getSourceAndDestination().getArchiveBucketName(),
                              input.getSourceAndDestination().getArchiveObjectKey() );
        Identity identity =
            Identity.of( input.getAwsAccessKeyId(), input.getAwsSecretKey(),
                         Regions.US_EAST_1.getName() );

        WorkerOptions options = new WorkerOptions();
        options.setKeyPairName( input.getWorkerInstanceOptions().getKeypairName() );
        options.setLaunchTimeoutSeconds( input.getWorkerInstanceOptions().getLaunchTimeoutSeconds() );
        options.setSecurityGroupIds( input.getWorkerInstanceOptions().getSecurityGroupIds() );
        options.setSubnetId( input.getWorkerInstanceOptions().getSubnetId() );

        switch ( input.getActionType() )
        {
            case BACKUP_INSTANCE:
                ExportInstanceRequest exportInstance =
                    new ExportInstanceRequest();
                exportInstance.setDatabaseMasterPassword( input.getSourceAndDestination().getDatabaseMasterPassword() );
                exportInstance.setDestinationArchive( dest );
                exportInstance.setIdentity( identity );
                exportInstance.setLiveInstanceTouched( input.getSourceAndDestination().isLiveInstanceTouched() );
                exportInstance.setSnapshotCreationTimeoutSeconds( input.getSourceAndDestination().getSnapshotTimeoutSeconds() );
                exportInstance.setInstanceName( input.getSourceAndDestination().getDatabaseInstanceId() );
                exportInstance.setWorkerOptions( options );
                dataMungService.exportInstance( workflowId, exportInstance );
                break;
            case CONVERT_SNAPSHOT:
                ExportSnapshotRequest exportSnapshot =
                    new ExportSnapshotRequest();
                exportSnapshot.setDatabaseMasterPassword( input.getSourceAndDestination().getDatabaseMasterPassword() );
                exportSnapshot.setDestinationArchive( dest );
                exportSnapshot.setIdentity( identity );
                exportSnapshot.setSnapshotRestoreTimeoutSeconds( input.getSourceAndDestination().getSnapshotTimeoutSeconds() );
                exportSnapshot.setSnapshotName( input.getSourceAndDestination().getDatabaseSnapshotId() );
                exportSnapshot.setWorkerOptions( options );
                dataMungService.exportSnapshot( workflowId, exportSnapshot );
                break;
            default:
                throw new IllegalStateException( "Unexpected action type "
                    + input.getActionType() );
        }

        return "redirect:/browse?highlight=" + workflowId;
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

    @RequestMapping( "/backup_details.html" )
    public ModelAndView showBackupDetails( @RequestParam( "inputData" ) String inputData )
        throws IOException
    {
        return showBackupDetails( JobInput.deserializeFrom( inputData ) );
    }

    private ModelAndView showConfirm( JobInput input )
        throws IOException
    {
        ModelAndView mav =
            new ModelAndView( "create/confirm.vm" ).addObject( "input", input ).addObject( "inputData",
                                                                                           input.serializeTo() );
        return mav;
    }

    @RequestMapping( "/confirm.html" )
    public ModelAndView showConfirm( @RequestParam( "inputData" ) String inputData )
        throws IOException
    {
        return showConfirm( JobInput.deserializeFrom( inputData ) );
    }

    @RequestMapping( value = { "", "/index.html", "/get_started.html" } )
    public ModelAndView showGetStarted( @RequestParam( value = "inputData", required = false ) String inputData )
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

    private ModelAndView showWorkerInstanceOptions( JobInput input )
        throws IOException
    {
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

        // Fetch all keypairs
        mav.addObject( "allKeyPairs",
                       ec2.describeKeyPairs( decorate( new DescribeKeyPairsRequest(),
                                                       creds ) ).getKeyPairs() );

        // Fetch all security groups
        String vpcId = null;
        switch ( input.getActionType() )
        {
            case BACKUP_INSTANCE:
                DBInstance instance =
                    rds.describeDBInstances( decorate( new DescribeDBInstancesRequest().withDBInstanceIdentifier( input.getSourceAndDestination().getDatabaseInstanceId() ),
                                                       creds ) ).getDBInstances().get( 0 );
                mav.addObject( "sourceDatabaseInstance", instance );
                if ( instance.getDBSubnetGroup() != null )
                {
                    vpcId = instance.getDBSubnetGroup().getVpcId();
                }
                break;
            case CONVERT_SNAPSHOT:
                DBSnapshot snapshot =
                    rds.describeDBSnapshots( decorate( new DescribeDBSnapshotsRequest().withDBSnapshotIdentifier( input.getSourceAndDestination().getDatabaseSnapshotId() ),
                                                       creds ) ).getDBSnapshots().get( 0 );
                mav.addObject( "sourceDatabaseSnapshot", snapshot );
                vpcId = snapshot.getVpcId();
                break;
            default:
                throw new IllegalStateException( "Action type "
                    + input.getActionType() + " is not expected" );
        }
        mav.addObject( "vpcId", vpcId );

        List<SecurityGroup> availableGroups = new ArrayList<SecurityGroup>();
        for ( SecurityGroup group : ec2.describeSecurityGroups( decorate( new DescribeSecurityGroupsRequest(),
                                                                          creds ) ).getSecurityGroups() )
        {
            if ( StringUtils.equals( vpcId, group.getVpcId() )
                && !group.getGroupName().startsWith( "awseb-e-" ) )
            {
                availableGroups.add( group );
            }
        }
        mav.addObject( "allSecurityGroups", availableGroups );

        if ( vpcId != null )
        {
            List<Subnet> availableSubnets = new ArrayList<Subnet>();
            for ( Subnet subnet : ec2.describeSubnets( decorate( new DescribeSubnetsRequest(),
                                                                 creds ) ).getSubnets() )
            {
                if ( StringUtils.equals( subnet.getVpcId(), vpcId ) )
                {
                    availableSubnets.add( subnet );
                }
            }
            mav.addObject( "allSubnets", availableSubnets );
        }

        mav.addObject( "workerOptions", input.getWorkerInstanceOptions() );
        return mav;
    }

    @RequestMapping( "/worker_options.html" )
    public ModelAndView showWorkerInstanceOptions( @RequestParam( "inputData" ) String inputData )
        throws IOException
    {
        return showWorkerInstanceOptions( JobInput.deserializeFrom( inputData ) );
    }
}
