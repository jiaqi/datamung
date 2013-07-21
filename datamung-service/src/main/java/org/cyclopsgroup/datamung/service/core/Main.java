package org.cyclopsgroup.datamung.service.core;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.cyclopsgroup.datamung.api.types.ExportInstanceRequest;
import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.S3DataArchive;
import org.cyclopsgroup.datamung.api.types.WorkerOptions;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflowClientExternalFactory;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflowClientExternalFactoryImpl;

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
        AWSCredentials caseCreds =
            new PropertiesCredentials(
                                       new File(
                                                 "/Users/jguo/Dropbox/laogong/grpn/jguo-grpn-aws-creds.properties" ) );

        ExportInstanceRequest request = new ExportInstanceRequest();
        request.setIdentity( Identity.of( caseCreds.getAWSAccessKeyId(),
                                          caseCreds.getAWSSecretKey() ) );
        WorkerOptions options = new WorkerOptions();
        options.setKeyPairName( "timecrook" );
        options.setSubnetId( "subnet-ee530383" );
        options.setSecurityGroupIds( Arrays.asList( "sg-4cd62923",
                                                    "sg-60d6290f" ) );
        request.setWorkerOptions( options );

        request.setDestinationArchive( S3DataArchive.of( "superuser-test",
                                                         "for-real.gz" ) );
        request.setDatabaseMasterPassword( "600wchicago" );
        request.setInstanceName( "lifeguard-uat" );
        request.setLiveInstanceTouched( false );

        AWSCredentials serviceCreds =
            new PropertiesCredentials(
                                       new File(
                                                 "/Users/jguo/Dropbox/laogong/cg/jiaqi-root-aws-creds.properties" ) );

        AmazonSimpleWorkflow swf =
            new AmazonSimpleWorkflowClient( serviceCreds );
        swf.setEndpoint( "https://swf.us-east-1.amazonaws.com" );
        ExportInstanceWorkflowClientExternalFactory fac =
            new ExportInstanceWorkflowClientExternalFactoryImpl( swf,
                                                                 "datamung-test" );
        fac.getClient( "test-2" ).export( request );
    }
}
