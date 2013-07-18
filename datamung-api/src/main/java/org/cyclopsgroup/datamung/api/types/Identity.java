package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

@XmlType
public class Identity
    extends BaseComparableBean
{
    private String awsAccessKeyId;

    private String awsSecretKey;

    public static Identity of( String accessKeyId, String secretKey )
    {
        Identity id = new Identity();
        id.awsAccessKeyId = accessKeyId;
        id.awsSecretKey = secretKey;
        return id;
    }

    @XmlElement
    public String getAwsAccessKeyId()
    {
        return awsAccessKeyId;
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

    public void setAwsSecretKey( String awsSecretKey )
    {
        this.awsSecretKey = awsSecretKey;
    }
}
