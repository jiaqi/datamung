package org.cyclopsgroup.datamung.api.types;


public class S3JobResultHandler
    extends JobResultHandler
{
    private String bucketName;

    private String objectKey;

    public S3JobResultHandler()
    {
        super( Type.S3 );
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public String getObjectKey()
    {
        return objectKey;
    }

    public void setBucketName( String bucketName )
    {
        this.bucketName = bucketName;
    }

    public void setObjectKey( String objectKey )
    {
        this.objectKey = objectKey;
    }
}
