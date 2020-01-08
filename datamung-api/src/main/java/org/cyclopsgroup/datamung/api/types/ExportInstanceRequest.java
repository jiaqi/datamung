package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ExportInstanceRequest")
public class ExportInstanceRequest extends ExportRequest {
  private static final long DEFAULT_SNAPSHOT_CREATION_TIMEOUT = 1800L;

  private String instanceName;

  private boolean liveInstanceTouched;

  private long snapshotCreationTimeoutSeconds = DEFAULT_SNAPSHOT_CREATION_TIMEOUT;

  @XmlElement
  public String getInstanceName() {
    return instanceName;
  }

  @XmlElement
  public long getSnapshotCreationTimeoutSeconds() {
    return snapshotCreationTimeoutSeconds;
  }

  @XmlElement
  public boolean isLiveInstanceTouched() {
    return liveInstanceTouched;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  public void setLiveInstanceTouched(boolean liveInstanceTouched) {
    this.liveInstanceTouched = liveInstanceTouched;
  }

  public void setSnapshotCreationTimeoutSeconds(long snapshotCreationTimeoutSeconds) {
    this.snapshotCreationTimeoutSeconds = snapshotCreationTimeoutSeconds;
  }
}
