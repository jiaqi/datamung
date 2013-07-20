package org.cyclopsgroup.datamung.web.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SourceAndDestination
{
    @NotNull
    private ActionType actionType;

    @Size( min = 1, message = "Bucket name can't be empty" )
    private String archiveBucketName;

    @Size( min = 1, message = "Object key can't be empty" )
    private String archiveObjectKey;

    private String databaseInstanceId;

    @NotNull
    @Size( min = 1, message = "Password can't be empty" )
    private String databaseMasterPassword;

    private String databaseSnapshotId;

    private boolean liveInstanceTouched;

    public ActionType getActionType()
    {
        return actionType;
    }

    public String getArchiveBucketName()
    {
        return archiveBucketName;
    }

    public String getArchiveObjectKey()
    {
        return archiveObjectKey;
    }

    public String getDatabaseInstanceId()
    {
        return databaseInstanceId;
    }

    public String getDatabaseMasterPassword()
    {
        return databaseMasterPassword;
    }

    public String getDatabaseSnapshotId()
    {
        return databaseSnapshotId;
    }

    public boolean isLiveInstanceTouched()
    {
        return liveInstanceTouched;
    }

    public void setActionType( ActionType actionType )
    {
        this.actionType = actionType;
    }

    public void setArchiveBucketName( String archiveBucketName )
    {
        this.archiveBucketName = archiveBucketName;
    }

    public void setArchiveObjectKey( String archiveObjectKey )
    {
        this.archiveObjectKey = archiveObjectKey;
    }

    public void setDatabaseInstanceId( String databaseInstanceId )
    {
        this.databaseInstanceId = databaseInstanceId;
    }

    public void setDatabaseMasterPassword( String databaseMasterPassword )
    {
        this.databaseMasterPassword = databaseMasterPassword;
    }

    public void setDatabaseSnapshotId( String databaseSnapshotId )
    {
        this.databaseSnapshotId = databaseSnapshotId;
    }

    public void setLiveInstanceTouched( boolean liveInstanceTouched )
    {
        this.liveInstanceTouched = liveInstanceTouched;
    }
}
