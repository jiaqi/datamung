package org.cyclopsgroup.datamung.service.activities;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.swf.interfaces.RdsActivities;
import org.cyclopsgroup.datamung.swf.types.DatabaseInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    @Autowired
    private AmazonRDS rds;

    /**
     * @inheritDoc
     */
    @Override
    public void createSnapshot( String snapshotName, String instanceName,
                                Identity identity )
    {

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
        rds.deleteDBSnapshot( ActivityUtils.decorate( new DeleteDBSnapshotRequest(
                                                                                   snapshotName ),
                                                      identity ) );
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

        DatabaseInstance i = new DatabaseInstance();
        i.setAllocatedStorage( result.getAllocatedStorage() );
        i.setAvailabilityZone( result.getAvailabilityZone() );
        i.setInstanceId( result.getDBInstanceIdentifier() );
        i.setInstanceStatus( result.getDBInstanceStatus() );
        i.setInstanceType( result.getDBInstanceClass() );
        i.setMasterUser( result.getMasterUsername() );
        i.setPort( result.getEndpoint().getPort() );
        i.setPublicHostName( result.getEndpoint().getAddress() );
        return i;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getSnapshotStatus( String snapshotName, Identity identity )
    {
        DescribeDBSnapshotsResult result =
            rds.describeDBSnapshots( ActivityUtils.decorate( new DescribeDBSnapshotsRequest().withDBSnapshotIdentifier( snapshotName ),
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
    public DatabaseInstance restoreSnapshot( String snapshotName,
                                             String instanceName,
                                             Identity identity )
    {
        // TODO Set security group properly
        DBInstance ins =
            rds.restoreDBInstanceFromDBSnapshot( ActivityUtils.decorate( new RestoreDBInstanceFromDBSnapshotRequest(
                                                                                                                     instanceName,
                                                                                                                     snapshotName ).withPubliclyAccessible( true ),
                                                                         identity ) );
        DatabaseInstance desc = new DatabaseInstance();
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
        rds.deleteDBInstance( ActivityUtils.decorate( new DeleteDBInstanceRequest(
                                                                                   instanceName ),
                                                      identity ).withSkipFinalSnapshot( true ) );
    }
}
