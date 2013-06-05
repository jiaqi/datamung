package org.cyclopsgroup.datamung.web.form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationUtils;

import com.amazonaws.regions.Regions;

public class JobInput
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static JobInput deserializeFrom( String input )
        throws IOException
    {
        InputStream in =
            new ByteArrayInputStream( Base64.decodeBase64( input.getBytes() ) );
        GZIPInputStream zip = new GZIPInputStream( in );
        try
        {
            return (JobInput) SerializationUtils.deserialize( zip );
        }
        finally
        {
            IOUtils.closeQuietly( zip );
        }
    }

    private CredentialsAndAction.ActionType actionType;

    private String archiveBucketName;

    private String archiveObjectKey;

    private String awsAccessKeyId;

    private Regions awsRegion;

    private String awsSecretKey;

    private String databaseInstanceId;

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

    public String getAwsAccessKeyId()
    {
        return awsAccessKeyId;
    }

    public Regions getAwsRegion()
    {
        return awsRegion;
    }

    public String getAwsSecretKey()
    {
        return awsSecretKey;
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

    public String serializeTo()
        throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream zip = new GZIPOutputStream( out );
        try
        {
            SerializationUtils.serialize( this, zip );
            zip.flush();
            zip.close();
            return new String( Base64.encodeBase64( out.toByteArray() ) );
        }
        finally
        {
            IOUtils.closeQuietly( zip );
        }
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

    public void setAwsAccessKeyId( String accessKeyId )
    {
        this.awsAccessKeyId = accessKeyId;
    }

    public void setAwsRegion( Regions awsRegion )
    {
        this.awsRegion = awsRegion;
    }

    public void setAwsSecretKey( String secretKey )
    {
        this.awsSecretKey = secretKey;
    }

    public void setDatabaseInstanceId( String databaseInstanceId )
    {
        this.databaseInstanceId = databaseInstanceId;
    }

    public void setDatabaseMasterPassword( String masterPassword )
    {
        this.databaseMasterPassword = masterPassword;
    }

    public void setDatabaseSnapshotId( String databaseSnapshotId )
    {
        this.databaseSnapshotId = databaseSnapshotId;
    }
}
