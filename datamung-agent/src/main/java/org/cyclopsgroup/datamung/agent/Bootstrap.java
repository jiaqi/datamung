package org.cyclopsgroup.datamung.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.datamung.api.types.AgentConfig;
import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.Job;
import org.cyclopsgroup.datamung.api.types.JobResult;
import org.cyclopsgroup.datamung.api.types.S3JobResultHandler;
import org.cyclopsgroup.datamung.api.types.SqsJobResultHandler;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class Bootstrap
    extends AutoStartedWorker
{
    private class Check
        implements Runnable
    {
        public void run()
        {
            try
            {
                doCheck();
            }
            catch ( Throwable e )
            {
                LOG.warn( "Check call failed " + e.getMessage(), e );
            }
        }
    }

    private static final Log LOG = LogFactory.getLog( Bootstrap.class );

    private static <T extends AmazonWebServiceRequest> T decorate( T request,
                                                                   Identity id )
    {
        AWSCredentials creds;
        if ( id.getAwsAccessToken() == null )
        {
            creds =
                new BasicAWSCredentials( id.getAwsAccessKeyId(),
                                         id.getAwsSecretKey() );
        }
        else
        {
            creds =
                new BasicSessionCredentials( id.getAwsAccessKeyId(),
                                             id.getAwsSecretKey(),
                                             id.getAwsAccessToken() );
        }
        request.setRequestCredentials( creds );
        return request;
    }

    private final AgentConfig config;

    private final JsonDataConverter converter = new JsonDataConverter();

    private volatile long jobIntervalMillis = 500L;

    private final AmazonS3 s3;

    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();

    private final AmazonSQS sqs;

    public Bootstrap( AmazonSQS sqs, AgentConfig config )
    {
        this( sqs, null, config );
    }

    public Bootstrap( AmazonSQS sqs, AmazonS3 s3, AgentConfig config )
    {
        LOG.info( "Start agent with config "
            + ToStringBuilder.reflectionToString( config ) );
        this.sqs = sqs;
        this.config = config;
        this.s3 = s3;
    }

    private void doCheck()
    {
        ReceiveMessageResult result =
            sqs.receiveMessage( new ReceiveMessageRequest(
                                                           config.getJobQueueUrl() ) );
        for ( Message message : result.getMessages() )
        {
            try
            {
                Job job = converter.fromData( message.getBody(), Job.class );
                runJob( job );
                sqs.deleteMessage( new DeleteMessageRequest(
                                                             config.getJobQueueUrl(),
                                                             message.getReceiptHandle() ) );
            }
            catch ( Throwable e )
            {
                LOG.error( "Failed to handle message " + message.getBody(), e );
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    void doStart()
    {
        scheduler.scheduleWithFixedDelay( new Check(), jobIntervalMillis,
                                          jobIntervalMillis,
                                          TimeUnit.MILLISECONDS );
    }

    /**
     * @inheritDoc
     */
    @Override
    void doStop()
    {
        scheduler.shutdownNow();
    }

    private void runJob( Job job )
        throws IOException, InterruptedException
    {
        JobResult result = new JobResult();
        result.setJob( job );
        result.setStarted( System.currentTimeMillis() );
        Process proc =
            Runtime.getRuntime().exec( job.getCommand(),
                                       job.getArguments().toArray( ArrayUtils.EMPTY_STRING_ARRAY ) );

        result.setExitCode( proc.waitFor() );
        result.setElapsedMillis( System.currentTimeMillis()
            - result.getStarted() );
        String output = converter.toData( result );
        LOG.info( "Handling job result " + output + " with "
            + job.getResultHandler() );

        switch ( job.getResultHandler().getHandlerType() )
        {
            case S3:
                S3JobResultHandler s3Handler =
                    (S3JobResultHandler) job.getResultHandler();
                byte[] bytes = output.getBytes();

                ObjectMetadata meta = new ObjectMetadata();
                meta.setContentLength( bytes.length );
                s3.putObject( decorate( new PutObjectRequest(
                                                              s3Handler.getBucketName(),
                                                              s3Handler.getObjectKey(),
                                                              new ByteArrayInputStream(
                                                                                        bytes ),
                                                              meta ),
                                        job.getIdentity() ) );
                break;
            case SQS:
                SqsJobResultHandler sqsHandler =
                    (SqsJobResultHandler) job.getResultHandler();
                sqs.sendMessage( decorate( new SendMessageRequest(
                                                                   sqsHandler.getResultQueueUrl(),
                                                                   output ),
                                           job.getIdentity() ) );
                break;
            default:
                throw new AssertionError( "Unexpected result handler "
                    + job.getResultHandler().getHandlerType() );
        }
    }

    public void setJobIntervalMillis( long jobIntervalMillis )
    {
        Validate.isTrue( jobIntervalMillis >= 0,
                         "Invalid interval millis value " + jobIntervalMillis );
        this.jobIntervalMillis = jobIntervalMillis;
    }
}
