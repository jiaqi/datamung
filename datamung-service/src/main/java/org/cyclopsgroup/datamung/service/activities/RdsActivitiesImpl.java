package org.cyclopsgroup.datamung.service.activities;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.swf.interfaces.RdsActivities;
import org.cyclopsgroup.datamung.swf.types.DatabaseInstance;
import org.springframework.stereotype.Component;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.CreateDBSnapshotRequest;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DBInstanceNotFoundException;
import com.amazonaws.services.rds.model.DBSnapshot;
import com.amazonaws.services.rds.model.DBSnapshotNotFoundException;
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
    private static DatabaseInstance toDatabaseInstance( DBInstance dbInstance )
    {
        DatabaseInstance i = new DatabaseInstance();
        i.setAllocatedStorage( dbInstance.getAllocatedStorage() );
        i.setAvailabilityZone( dbInstance.getAvailabilityZone() );
        i.setInstanceId( dbInstance.getDBInstanceIdentifier() );
        i.setInstanceStatus( dbInstance.getDBInstanceStatus() );
        i.setInstanceType( dbInstance.getDBInstanceClass() );
        i.setMasterUser( dbInstance.getMasterUsername() );
        if ( dbInstance.getEndpoint() != null )
        {
            i.setPort( dbInstance.getEndpoint().getPort() );
            i.setPublicHostName( dbInstance.getEndpoint().getAddress() );
        }
        if ( dbInstance.getDBSubnetGroup() != null )
        {
            i.setSubnetGroupName( dbInstance.getDBSubnetGroup().getDBSubnetGroupName() );
        }
        return i;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void createSnapshot( String snapshotName, String instanceName,
                                Identity identity )
    {
        AmazonRDS rds =
            ActivityUtils.createClient( AmazonRDSClient.class, identity );
        try
        {
            rds.describeDBSnapshots( new DescribeDBSnapshotsRequest().withDBSnapshotIdentifier( snapshotName ) );
            return;
        }
        catch ( DBSnapshotNotFoundException e )
        {
        }
        rds.createDBSnapshot( new CreateDBSnapshotRequest( snapshotName,
                                                           instanceName ) );
    }

    /**
     * @inheritDoc
     */
    @Override
    public void deleteSnapshot( String snapshotName, Identity identity )
    {
        AmazonRDS rds =
            ActivityUtils.createClient( AmazonRDSClient.class, identity );
        try
        {
            rds.deleteDBSnapshot( new DeleteDBSnapshotRequest( snapshotName ) );
        }
        catch ( DBSnapshotNotFoundException e )
        {
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public DatabaseInstance describeInstance( String instanceName,
                                              Identity identity )
    {
        AmazonRDS rds =
            ActivityUtils.createClient( AmazonRDSClient.class, identity );
        DescribeDBInstancesResult results =
            rds.describeDBInstances( new DescribeDBInstancesRequest().withDBInstanceIdentifier( instanceName ) );
        if ( results.getDBInstances().isEmpty() )
        {
            return null;
        }
        DBInstance result = results.getDBInstances().get( 0 );
        return toDatabaseInstance( result );
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSnapshotStatus( String snapshotName, Identity identity )
    {
        AmazonRDS rds =
            ActivityUtils.createClient( AmazonRDSClient.class, identity );
        try
        {
            DescribeDBSnapshotsResult result =
                rds.describeDBSnapshots( new DescribeDBSnapshotsRequest().withDBSnapshotIdentifier( snapshotName ) );
            return result.getDBSnapshots().get( 0 ).getStatus();
        }
        catch ( DBSnapshotNotFoundException e )
        {
            return null;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public DatabaseInstance restoreSnapshot( String snapshotName,
                                             String instanceName,
                                             String subnetGroupName,
                                             Identity identity )
    {
        AmazonRDS rds =
            ActivityUtils.createClient( AmazonRDSClient.class, identity );
        try
        {
            DescribeDBInstancesResult results =
                rds.describeDBInstances( new DescribeDBInstancesRequest().withDBInstanceIdentifier( instanceName ) );
            return toDatabaseInstance( results.getDBInstances().get( 0 ) );
        }
        catch ( DBInstanceNotFoundException e )
        {
        }

        DescribeDBSnapshotsResult result =
            rds.describeDBSnapshots( new DescribeDBSnapshotsRequest().withDBSnapshotIdentifier( snapshotName ) );
        if ( result.getDBSnapshots().isEmpty() )
        {
            throw new IllegalArgumentException( "Snapshot  " + snapshotName
                + " is not found" );
        }
        DBSnapshot snapshot = result.getDBSnapshots().get( 0 );

        RestoreDBInstanceFromDBSnapshotRequest request =
            new RestoreDBInstanceFromDBSnapshotRequest( instanceName,
                                                        snapshotName );

        if ( snapshot.getVpcId() == null )
        {
            request.setPubliclyAccessible( true );
        }
        else
        {
            request.setDBSubnetGroupName( subnetGroupName );
        }

        DBInstance ins = rds.restoreDBInstanceFromDBSnapshot( request );
        DatabaseInstance desc = new DatabaseInstance();
        desc.setAllocatedStorage( ins.getAllocatedStorage() );
        desc.setAvailabilityZone( ins.getAvailabilityZone() );
        desc.setInstanceId( ins.getDBInstanceIdentifier() );
        desc.setInstanceType( ins.getDBInstanceClass() );
        if ( ins.getEndpoint() != null )
        {
            desc.setPublicHostName( ins.getEndpoint().getAddress() );
            desc.setPort( ins.getEndpoint().getPort() );
        }
        desc.setMasterUser( ins.getMasterUsername() );
        return desc;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void terminateInstance( String instanceName, Identity identity )
    {
        AmazonRDS rds =
            ActivityUtils.createClient( AmazonRDSClient.class, identity );
        try
        {
            rds.deleteDBInstance( new DeleteDBInstanceRequest( instanceName ).withSkipFinalSnapshot( true ) );
        }
        catch ( DBInstanceNotFoundException e )
        {
        }
    }
}
