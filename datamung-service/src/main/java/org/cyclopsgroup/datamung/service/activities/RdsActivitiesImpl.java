package org.cyclopsgroup.datamung.service.activities;

import java.io.IOException;

import org.cyclopsgroup.datamung.api.types.DataArchive;
import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.swf.interfaces.RdsActivities;
import org.cyclopsgroup.datamung.swf.types.InstanceDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.CreateDBSnapshotRequest;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest;
import com.amazonaws.services.rds.model.DeleteDBSnapshotRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.services.rds.model.DescribeDBSnapshotsRequest;
import com.amazonaws.services.rds.model.DescribeDBSnapshotsResult;
import com.amazonaws.services.rds.model.RestoreDBInstanceFromDBSnapshotRequest;

@Component( "workflow.RdsActivities" )
public class RdsActivitiesImpl
    implements RdsActivities
{
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

    @Autowired
    private AmazonRDS rds;

    /**
     * @inheritDoc
     */
    @Override
    public void createSnapshot( String snapshotName, String instanceName,
                                Identity identity )
    {

        rds.createDBSnapshot( decorate( new CreateDBSnapshotRequest(
                                                                     snapshotName,
                                                                     instanceName ),
                                        identity ) );
    }

    /**
     * @inheritDoc
     */
    @Override
    public void deleteSnapshot( String snapshotName, Identity identity )
    {
        rds.deleteDBSnapshot( decorate( new DeleteDBSnapshotRequest(
                                                                     snapshotName ),
                                        identity ) );
    }

    /**
     * @inheritDoc
     */
    @Override
    public void dumpAndArchive( String instanceName, DataArchive archive,
                                Identity identity )
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /**
     * @inheritDoc
     */
    @Override
    public String getInstanceStatus( String instanceName, Identity identity )
    {
        DescribeDBInstancesResult result =
            rds.describeDBInstances( decorate( new DescribeDBInstancesRequest().withDBInstanceIdentifier( instanceName ),
                                               identity ) );
        if ( result.getDBInstances().isEmpty() )
        {
            return null;
        }
        return result.getDBInstances().get( 0 ).getDBInstanceStatus();
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSnapshotStatus( String snapshotName, Identity identity )
    {
        DescribeDBSnapshotsResult result =
            rds.describeDBSnapshots( decorate( new DescribeDBSnapshotsRequest().withDBSnapshotIdentifier( snapshotName ),
                                               identity ) );
        if ( result.getDBSnapshots().isEmpty() )
        {
            return null;
        }
        return result.getDBSnapshots().get( 0 ).getStatus();
    }

    /**
     * @inheritDoc
     */
    @Override
    public InstanceDescription restoreSnapshot( String snapshotName,
                                                String instanceName,
                                                Identity identity )
    {
        // TODO Set security group properly
        DBInstance ins =
            rds.restoreDBInstanceFromDBSnapshot( decorate( new RestoreDBInstanceFromDBSnapshotRequest(
                                                                                                       instanceName,
                                                                                                       snapshotName ).withPubliclyAccessible( true ),
                                                           identity ) );
        InstanceDescription desc = new InstanceDescription();
        desc.setAllocatedStorage( ins.getAllocatedStorage() );
        desc.setAvailabilityZone( ins.getAvailabilityZone() );
        desc.setInstanceId( ins.getDBInstanceIdentifier() );
        desc.setInstanceType( ins.getDBInstanceClass() );
        desc.setPublicHostName( ins.getEndpoint().getAddress() );
        desc.setPort( ins.getEndpoint().getPort() );
        desc.setMasterUser( ins.getMasterUsername() );
        return desc;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void terminateInstance( String instanceName, Identity identity )
    {
        rds.deleteDBInstance( decorate(
                                        new DeleteDBInstanceRequest(
                                                                     instanceName ),
                                        identity ).withSkipFinalSnapshot( true ) );
    }
}
