package org.cyclopsgroup.datamung.service.core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.SystemUtils;
import org.cyclopsgroup.datamung.api.DataMungService;
import org.cyclopsgroup.datamung.api.types.ExportHandler;
import org.cyclopsgroup.datamung.api.types.ExportInstanceRequest;
import org.cyclopsgroup.datamung.api.types.ExportSnapshotRequest;
import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.S3DataArchive;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflowClientExternal;
import org.cyclopsgroup.datamung.swf.interfaces.ExportInstanceWorkflowClientExternalFactoryImpl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.STSSessionCredentialsProvider;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;

public class DataMungServiceImpl
    implements DataMungService
{

    @Override
    public ExportHandler exportInstance( String exportId,
                                         ExportInstanceRequest request )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExportHandler exportSnapshot( String exportId,
                                         ExportSnapshotRequest request )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public static void main( String[] args )
        throws IOException
    {
        String instanceName = args[0];

        AWSCredentials creds =
            new PropertiesCredentials( new File( SystemUtils.USER_HOME
                + "/Dropbox/laogong/grpn/jguo-grpn-aws-creds.properties" ) );
        AmazonSimpleWorkflow swf = new AmazonSimpleWorkflowClient( creds );
        ExportInstanceWorkflowClientExternal client =
            new ExportInstanceWorkflowClientExternalFactoryImpl( swf,
                                                                 "eng-sandbox-development" ).getClient( "test" );
        ExportInstanceRequest request = new ExportInstanceRequest();
        request.setDestinationArchive( S3DataArchive.of( "superuser-test",
                                                         "testdata" ) );

        AWSSessionCredentials session =
            (AWSSessionCredentials) new STSSessionCredentialsProvider( creds ).getCredentials();
        request.setIdentity( Identity.of( session.getAWSAccessKeyId(),
                                          session.getAWSSecretKey(),
                                          session.getSessionToken() ) );
        request.setInstanceName( instanceName );
        client.export( request );
    }
}
