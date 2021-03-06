package org.cyclopsgroup.datamung.web.form;

import java.util.List;

public class WorkerInstanceOptions {
  private static final long DEFAULT_LAUNCH_TIMEOUT = 300;

  private String keypairName;

  private long launchTimeoutSeconds = DEFAULT_LAUNCH_TIMEOUT;

  private List<String> securityGroupIds;

  private String subnetId;

  public String getKeypairName() {
    return keypairName;
  }

  public long getLaunchTimeoutSeconds() {
    return launchTimeoutSeconds;
  }

  public List<String> getSecurityGroupIds() {
    return securityGroupIds;
  }

  public String getSubnetId() {
    return subnetId;
  }

  public void setKeypairName(String keypairName) {
    this.keypairName = keypairName;
  }

  public void setLaunchTimeoutSeconds(long launchTimeoutSeconds) {
    this.launchTimeoutSeconds = launchTimeoutSeconds;
  }

  public void setSecurityGroupIds(List<String> securityGroupIds) {
    this.securityGroupIds = securityGroupIds;
  }

  public void setSubnetId(String subnetId) {
    this.subnetId = subnetId;
  }
}
