package org.cyclopsgroup.datamung.web.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SourceDestinationForm
{
    @NotNull
    private CredentialsAndAction.ActionType actionType;

    @Size( min = 1, message = "Bucket name can't be empty" )
    private String archiveBucketName;

    @Size( min = 1, message = "Object key can't be empty" )
    private String archiveObjectKey;

    private String databaseInstanceId;

    @NotNull
    @Size( min = 1, message = "Password can't be empty" )
    private String databaseMasterPassword;

    private String databaseSnapshotId;

    public CredentialsAndAction.ActionType getActionType()
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

    public void setActionType( CredentialsAndAction.ActionType actionType )
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
}
