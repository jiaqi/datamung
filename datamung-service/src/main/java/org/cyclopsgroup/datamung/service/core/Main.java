package org.cyclopsgroup.datamung.service.core;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.InstanceNetwork;
import org.cyclopsgroup.datamung.swf.interfaces.CommandJobWorkflowClientExternalFactory;
import org.cyclopsgroup.datamung.swf.interfaces.CommandJobWorkflowClientExternalFactoryImpl;
import org.cyclopsgroup.datamung.swf.types.CommandLineJob;
import org.cyclopsgroup.datamung.swf.types.RunJobRequest;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;

public class Main
{
    public static void main( String[] args )
        throws Exception
    {
        runTestFlow();
    }

    private static void runTestFlow()
        throws IOException
    {

        RunJobRequest request = new RunJobRequest();
        CommandLineJob job = new CommandLineJob();
        job.setCommand( "echo 1 2 3 4 5 6" );

        AWSCredentials caseCreds =
            new PropertiesCredentials(
                                       new File(
                                                 "/Users/jguo/Dropbox/laogong/grpn/jguo-grpn-aws-creds.properties" ) );
        request.setIdentity( Identity.of( caseCreds.getAWSAccessKeyId(),
                                          caseCreds.getAWSSecretKey(), null ) );
        request.setJob( job );
        request.setKeyPairName( "timecrook" );
        request.setNetwork( InstanceNetwork.ofPublic( Arrays.asList( "sg-56e9453d" ) ) );

        AWSCredentials serviceCreds =
            new PropertiesCredentials(
                                       new File(
                                                 "/Users/jguo/Dropbox/laogong/cg/jiaqi-root-aws-creds.properties" ) );

        AmazonSimpleWorkflow swf =
            new AmazonSimpleWorkflowClient( serviceCreds );
        swf.setEndpoint( "https://swf.us-east-1.amazonaws.com" );
        CommandJobWorkflowClientExternalFactory fac =
            new CommandJobWorkflowClientExternalFactoryImpl( swf,
                                                             "datamung-test" );
        fac.getClient( "test" ).executeCommand( request );
    }
}
