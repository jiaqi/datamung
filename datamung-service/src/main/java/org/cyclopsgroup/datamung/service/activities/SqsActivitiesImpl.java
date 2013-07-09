package org.cyclopsgroup.datamung.service.activities;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.Job;
import org.cyclopsgroup.datamung.api.types.JobResult;
import org.cyclopsgroup.datamung.api.types.S3JobResultHandler;
import org.cyclopsgroup.datamung.api.types.SqsJobResultHandler;
import org.cyclopsgroup.datamung.swf.interfaces.SqsActivities;
import org.cyclopsgroup.datamung.swf.types.Queue;
import org.cyclopsgroup.datamung.swf.types.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

@Component( "workflow.SqsActivities" )
public class SqsActivitiesImpl
    implements SqsActivities
{
    private static final String ATTRIBUTE_ARN = "QueueArn";

    private static final Log LOG = LogFactory.getLog( SqsActivitiesImpl.class );

    private final JsonDataConverter converter = new JsonDataConverter();

    @Autowired
    private AmazonS3 s3;

    @Autowired
    private AmazonSQS sqs;

    /**
     * @inheritDoc
     */
    @Override
    public Queue createQueue( String queueName, Identity identity )
    {
        CreateQueueResult createQueue =
            sqs.createQueue( ActivityUtils.decorate( new CreateQueueRequest(
                                                                             queueName ),
                                                     identity ) );

        GetQueueAttributesResult getAttributes =
            sqs.getQueueAttributes( ActivityUtils.decorate( new GetQueueAttributesRequest(
                                                                                           createQueue.getQueueUrl() ).withAttributeNames( ATTRIBUTE_ARN ),
                                                            identity ) );
        Queue queue = new Queue();
        queue.setQueueName( queueName );
        queue.setQueueUrl( createQueue.getQueueUrl() );
        queue.setArn( getAttributes.getAttributes().get( ATTRIBUTE_ARN ) );
        return queue;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void deleteQueue( String queueUrl, Identity identity )
    {
        sqs.deleteQueue( ActivityUtils.decorate( new DeleteQueueRequest(
                                                                         queueUrl ),
                                                 identity ) );
    }

    /**
     * @inheritDoc
     */
    @Override
    public Wrapper<JobResult> pollJobResult( Job job )
    {
        switch ( job.getResultHandler().getHandlerType() )
        {
            case S3:
                S3JobResultHandler s3Handler =
                    (S3JobResultHandler) job.getResultHandler();
                try
                {
                    S3Object object =
                        s3.getObject( ActivityUtils.decorate( new GetObjectRequest(
                                                                                    s3Handler.getBucketName(),
                                                                                    s3Handler.getObjectKey() ),
                                                              job.getIdentity() ) );
                    InputStream in = object.getObjectContent();
                    try
                    {
                        String content =
                            IOUtils.toString( object.getObjectContent() );
                        return Wrapper.of( converter.fromData( content,
                                                               JobResult.class ) );
                    }
                    finally
                    {
                        IOUtils.closeQuietly( in );
                    }
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( "Communication failure: "
                        + e.getMessage(), e );
                }
                catch ( AmazonS3Exception e )
                {
                    if ( e.getStatusCode() == 404 )
                    {
                        LOG.info( "Received 404: " + e.getMessage()
                            + ", return NULL" );
                        return Wrapper.of( null );
                    }
                    throw e;
                }
            case SQS:
                SqsJobResultHandler sqsHandler =
                    (SqsJobResultHandler) job.getResultHandler();
                ReceiveMessageResult msgs =
                    sqs.receiveMessage( ActivityUtils.decorate( new ReceiveMessageRequest(
                                                                                           sqsHandler.getResultQueueUrl() ),
                                                                job.getIdentity() ) );
                if ( msgs.getMessages().isEmpty() )
                {
                    return Wrapper.of( null );
                }
                JobResult result =
                    converter.fromData( msgs.getMessages().get( 0 ).getBody(),
                                        JobResult.class );
                sqs.deleteMessage( new DeleteMessageRequest(
                                                             sqsHandler.getResultQueueUrl(),
                                                             msgs.getMessages().get( 0 ).getReceiptHandle() ) );
                return Wrapper.of( result );
            default:
                throw new AssertionError( "Unexpected handler type "
                    + job.getResultHandler().getHandlerType() );
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void sendJobToQueue( Queue queue, Job job )
    {
        sqs.sendMessage( ActivityUtils.decorate( new SendMessageRequest(
                                                                         queue.getQueueUrl(),
                                                                         converter.toData( job ) ),
                                                 job.getIdentity() ) );
    }
}
