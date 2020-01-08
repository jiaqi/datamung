package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

@XmlType
public class Identity extends BaseComparableBean {
  public static Identity of(String accessKeyId, String secretKey, String regionName) {
    Identity id = new Identity();
    id.awsAccessKeyId = accessKeyId;
    id.awsSecretKey = secretKey;
    id.awsRegionName = regionName;
    return id;
  }

  private String awsAccessKeyId;

  private String awsRegionName;

  private String awsSecretKey;

  @XmlElement
  public String getAwsAccessKeyId() {
    return awsAccessKeyId;
  }

  @XmlElement
  public String getAwsRegionName() {
    return awsRegionName;
  }

  @XmlElement
  public String getAwsSecretKey() {
    return awsSecretKey;
  }

  public void setAwsAccessKeyId(String awsAccessKeyId) {
    this.awsAccessKeyId = awsAccessKeyId;
  }

  public void setAwsRegionName(String awsRegionName) {
    this.awsRegionName = awsRegionName;
  }

  public void setAwsSecretKey(String awsSecretKey) {
    this.awsSecretKey = awsSecretKey;
  }
}
