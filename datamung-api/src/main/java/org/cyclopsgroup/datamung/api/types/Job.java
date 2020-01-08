package org.cyclopsgroup.datamung.api.types;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class Job extends BaseComparableBean {
  private String command;

  private int timeoutSeconds = 900;

  public String getCommand() {
    return command;
  }

  public int getTimeoutSeconds() {
    return timeoutSeconds;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public void setTimeoutSeconds(int timeoutSeconds) {
    this.timeoutSeconds = timeoutSeconds;
  }
}
