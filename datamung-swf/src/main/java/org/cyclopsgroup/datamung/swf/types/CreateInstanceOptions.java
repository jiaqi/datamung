package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.WorkerOptions;
import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class CreateInstanceOptions extends BaseComparableBean {
  private String instanceProfileName;

  private String userData;

  private WorkerOptions workerOptions;

  public String getInstanceProfileName() {
    return instanceProfileName;
  }

  public String getUserData() {
    return userData;
  }

  public WorkerOptions getWorkerOptions() {
    return workerOptions;
  }

  public void setInstanceProfileName(String instanceProfileName) {
    this.instanceProfileName = instanceProfileName;
  }

  public void setUserData(String userData) {
    this.userData = userData;
  }

  public void setWorkerOptions(WorkerOptions workerOptions) {
    this.workerOptions = workerOptions;
  }
}
