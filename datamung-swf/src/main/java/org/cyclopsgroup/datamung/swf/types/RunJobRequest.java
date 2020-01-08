package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.WorkerOptions;
import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class RunJobRequest extends BaseComparableBean {
  private Identity identity;

  private Job job;

  private WorkerOptions workerOptions;

  public Identity getIdentity() {
    return identity;
  }

  public Job getJob() {
    return job;
  }

  public WorkerOptions getWorkerOptions() {
    return workerOptions;
  }

  public void setIdentity(Identity identity) {
    this.identity = identity;
  }

  public void setJob(Job job) {
    this.job = job;
  }

  public void setWorkerOptions(WorkerOptions workerOptions) {
    this.workerOptions = workerOptions;
  }
}
