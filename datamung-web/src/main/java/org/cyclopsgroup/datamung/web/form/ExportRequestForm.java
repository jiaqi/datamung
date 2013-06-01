package org.cyclopsgroup.datamung.web.form;

import javax.validation.constraints.NotNull;

public class ExportRequestForm
{
    public static enum SourceObjectType
    {
        INSTANCE, SNAPSHOT;
    }

    @NotNull
    private String awsAccessKeyId;

    @NotNull
    private String awsSecretKey;

    @NotNull
    private String destinationBucket;

    @NotNull
    private String destinationObject;

    @NotNull
    private String masterPassword;

    @NotNull
    private String sourceName;

    @NotNull
    private SourceObjectType sourceType;

    public String getAwsAccessKeyId()
    {
        return awsAccessKeyId;
    }

    public String getAwsSecretKey()
    {
        return awsSecretKey;
    }

    public String getDestinationBucket()
    {
        return destinationBucket;
    }

    public String getDestinationObject()
    {
        return destinationObject;
    }

    public String getMasterPassword()
    {
        return masterPassword;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public SourceObjectType getSourceType()
    {
        return sourceType;
    }

    public void setAwsAccessKeyId( String accessKeyId )
    {
        this.awsAccessKeyId = accessKeyId;
    }

    public void setAwsSecretKey( String secretKey )
    {
        this.awsSecretKey = secretKey;
    }

    public void setDestinationBucket( String destinationBucket )
    {
        this.destinationBucket = destinationBucket;
    }

    public void setDestinationObject( String destinationObject )
    {
        this.destinationObject = destinationObject;
    }

    public void setMasterPassword( String masterPassword )
    {
        this.masterPassword = masterPassword;
    }

    public void setSourceName( String sourceName )
    {
        this.sourceName = sourceName;
    }

    public void setSourceType( SourceObjectType sourceType )
    {
        this.sourceType = sourceType;
    }
}
