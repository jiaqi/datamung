package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ExportInstanceRequest")
public class ExportSnapshotRequest extends ExportRequest {
  private static final long DEFAULT_SNAPSHOT_RESTORE_TIMEOUT = 1800L;

  private String snapshotName;

  private long snapshotRestoreTimeoutSeconds = DEFAULT_SNAPSHOT_RESTORE_TIMEOUT;

  private String subnetGroupName;

  @XmlElement
  public String getSnapshotName() {
    return snapshotName;
  }

  @XmlElement
  public long getSnapshotRestoreTimeoutSeconds() {
    return snapshotRestoreTimeoutSeconds;
  }

  @XmlElement
  public String getSubnetGroupName() {
    return subnetGroupName;
  }

  public void setSnapshotName(String snapshotName) {
    this.snapshotName = snapshotName;
  }

  public void setSnapshotRestoreTimeoutSeconds(long snapshotRestoreTimeoutSeconds) {
    this.snapshotRestoreTimeoutSeconds = snapshotRestoreTimeoutSeconds;
  }

  public void setSubnetGroupName(String subnetGroupName) {
    this.subnetGroupName = subnetGroupName;
  }
}
