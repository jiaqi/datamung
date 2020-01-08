package org.cyclopsgroup.datamung.web.form;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SourceAndDestination {
  private static final long DEFAULT_SNAPSHOT_TIMEOUT_SECONDS = 1800L;

  @Size(min = 1, message = "Bucket name can't be empty")
  private String archiveBucketName;

  @Size(min = 1, message = "Object key can't be empty")
  private String archiveObjectKey;

  private String databaseInstanceId;

  @NotNull
  @Size(min = 1, message = "Password can't be empty")
  private String databaseMasterPassword;

  private String databaseSnapshotId;

  private boolean liveInstanceTouched;

  @Min(value = 30, message = "Snapshot timeout must be at least 30 seconds")
  private long snapshotTimeoutSeconds = DEFAULT_SNAPSHOT_TIMEOUT_SECONDS;

  public String getArchiveBucketName() {
    return archiveBucketName;
  }

  public String getArchiveObjectKey() {
    return archiveObjectKey;
  }

  public String getDatabaseInstanceId() {
    return databaseInstanceId;
  }

  public String getDatabaseMasterPassword() {
    return databaseMasterPassword;
  }

  public String getDatabaseSnapshotId() {
    return databaseSnapshotId;
  }

  public long getSnapshotTimeoutSeconds() {
    return snapshotTimeoutSeconds;
  }

  public boolean isLiveInstanceTouched() {
    return liveInstanceTouched;
  }

  public void setArchiveBucketName(String archiveBucketName) {
    this.archiveBucketName = archiveBucketName;
  }

  public void setArchiveObjectKey(String archiveObjectKey) {
    this.archiveObjectKey = archiveObjectKey;
  }

  public void setDatabaseInstanceId(String databaseInstanceId) {
    this.databaseInstanceId = databaseInstanceId;
  }

  public void setDatabaseMasterPassword(String databaseMasterPassword) {
    this.databaseMasterPassword = databaseMasterPassword;
  }

  public void setDatabaseSnapshotId(String databaseSnapshotId) {
    this.databaseSnapshotId = databaseSnapshotId;
  }

  public void setLiveInstanceTouched(boolean liveInstanceTouched) {
    this.liveInstanceTouched = liveInstanceTouched;
  }

  public void setSnapshotTimeoutSeconds(long snapshotTimeoutSeconds) {
    this.snapshotTimeoutSeconds = snapshotTimeoutSeconds;
  }
}
