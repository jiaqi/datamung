package org.cyclopsgroup.datamung.service.core;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.Job;
import org.cyclopsgroup.datamung.api.types.RunJobRequest;
import org.cyclopsgroup.datamung.api.types.S3JobResultHandler;
import org.cyclopsgroup.datamung.swf.interfaces.CommandJobWorkflowClientExternalFactory;
import org.cyclopsgroup.datamung.swf.interfaces.CommandJobWorkflowClientExternalFactoryImpl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;

public class Main
{
    public static void main( String[] args )
        throws IOException
    {
        AWSCredentials creds = new PropertiesCredentials( new File( args[0] ) );
        runTestFlow( creds, args[1], args[2], "job-result" );
    }

    private static void runTestFlow( AWSCredentials creds, String domainName,
                                     String resultBucket, String resultKey )
    {
        AmazonSimpleWorkflow swf = new AmazonSimpleWorkflowClient( creds );
        swf.setEndpoint( "https://swf.us-east-1.amazonaws.com" );

        RunJobRequest request = new RunJobRequest();

        Job job = new Job();
        job.setCommand( "echo" );
        job.setArguments( Arrays.asList( "1", "2", "3", "4", "5" ) );
        job.setIdentity( Identity.of( creds.getAWSAccessKeyId(),
                                      creds.getAWSSecretKey(), null ) );

        job.setResultHandler( S3JobResultHandler.of( resultBucket, resultKey ) );
        request.setJob( job );

        CommandJobWorkflowClientExternalFactory fac =
            new CommandJobWorkflowClientExternalFactoryImpl( swf, domainName );
        fac.getClient( "test" ).executeCommand( request );
    }
}
