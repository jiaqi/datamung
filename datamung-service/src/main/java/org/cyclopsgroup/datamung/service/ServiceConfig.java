package org.cyclopsgroup.datamung.service;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class ServiceConfig extends BaseComparableBean {
  private String agentAmiId;

  private String awsAccountId;

  private String swfDomainName;

  public String getAgentAmiId() {
    return agentAmiId;
  }

  public String getAwsAccountId() {
    return awsAccountId;
  }

  public String getSwfDomainName() {
    return swfDomainName;
  }

  public void setAgentAmiId(String agentAmiId) {
    this.agentAmiId = agentAmiId;
  }

  public void setAwsAccountId(String awsAccountId) {
    this.awsAccountId = awsAccountId;
  }

  public void setSwfDomainName(String swfDomainName) {
    this.swfDomainName = swfDomainName;
  }
}
