package org.cyclopsgroup.datamung.service.activities;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.swf.interfaces.RdsActivities;
import org.cyclopsgroup.datamung.swf.types.DatabaseInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.rds.AmazonRDS;
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

    @Autowired
    private AmazonRDS rds;

    /**
     * @inheritDoc
     */
    @Override
    public void createSnapshot( String snapshotName, String instanceName,
                                Identity identity )
    {
        try
        {
            rds.describeDBSnapshots( ActivityUtils.decorate( new DescribeDBSnapshotsRequest().withDBSnapshotIdentifier( snapshotName ),
                                                             identity ) );
            return;
        }
        catch ( DBSnapshotNotFoundException e )
        {
        }
        rds.createDBSnapshot( ActivityUtils.decorate( new CreateDBSnapshotRequest(
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
        try
        {
            rds.deleteDBSnapshot( ActivityUtils.decorate( new DeleteDBSnapshotRequest(
                                                                                       snapshotName ),
                                                          identity ) );
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
        DescribeDBInstancesResult results =
            rds.describeDBInstances( ActivityUtils.decorate( new DescribeDBInstancesRequest().withDBInstanceIdentifier( instanceName ),
                                                             identity ) );
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
        try
        {
            DescribeDBSnapshotsResult result =
                rds.describeDBSnapshots( ActivityUtils.decorate( new DescribeDBSnapshotsRequest().withDBSnapshotIdentifier( snapshotName ),
                                                                 identity ) );
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
        try
        {
            DescribeDBInstancesResult results =
                rds.describeDBInstances( ActivityUtils.decorate( new DescribeDBInstancesRequest().withDBInstanceIdentifier( instanceName ),
                                                                 identity ) );
            return toDatabaseInstance( results.getDBInstances().get( 0 ) );
        }
        catch ( DBInstanceNotFoundException e )
        {
        }

        DescribeDBSnapshotsResult result =
            rds.describeDBSnapshots( ActivityUtils.decorate( new DescribeDBSnapshotsRequest().withDBSnapshotIdentifier( snapshotName ),
                                                             identity ) );
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

        DBInstance ins =
            rds.restoreDBInstanceFromDBSnapshot( ActivityUtils.decorate( request,
                                                                         identity ) );
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
        try
        {
            rds.deleteDBInstance( ActivityUtils.decorate( new DeleteDBInstanceRequest(
                                                                                       instanceName ),
                                                          identity ).withSkipFinalSnapshot( true ) );
        }
        catch ( DBInstanceNotFoundException e )
        {
        }
    }
}
