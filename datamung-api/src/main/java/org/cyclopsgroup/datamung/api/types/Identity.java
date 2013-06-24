package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class Identity
    extends BaseType
{
    private String awsAccessKeyId;

    private String awsAccessToken;

    private String awsSecretKey;

    public static Identity of( String accessKeyId, String secretKey,
                               String token )
    {
        Identity id = new Identity();
        id.awsAccessKeyId = accessKeyId;
        id.awsSecretKey = secretKey;
        id.awsAccessToken = token;
        return id;
    }

    @XmlElement
    public String getAwsAccessKeyId()
    {
        return awsAccessKeyId;
    }

    @XmlElement
    public String getAwsAccessToken()
    {
        return awsAccessToken;
    }

    @XmlElement
    public String getAwsSecretKey()
    {
        return awsSecretKey;
    }

    public void setAwsAccessKeyId( String awsAccessKeyId )
    {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public void setAwsAccessToken( String awsAccessToken )
    {
        this.awsAccessToken = awsAccessToken;
    }

    public void setAwsSecretKey( String awsSecretKey )
    {
        this.awsSecretKey = awsSecretKey;
    }
}
