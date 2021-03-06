package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

public abstract class ExportRequest extends IdentityAwareObject {
  private String databaseMasterPassword;

  private DataArchive destinationArchive;

  private WorkerOptions workerOptions;

  @XmlElement
  public String getDatabaseMasterPassword() {
    return databaseMasterPassword;
  }

  @XmlElements({@XmlElement(name = "s3Archive", type = S3DataArchive.class)})
  public DataArchive getDestinationArchive() {
    return destinationArchive;
  }

  @XmlElement
  public WorkerOptions getWorkerOptions() {
    return workerOptions;
  }

  public void setDatabaseMasterPassword(String databasePassword) {
    this.databaseMasterPassword = databasePassword;
  }

  public void setDestinationArchive(DataArchive destinationArchive) {
    this.destinationArchive = destinationArchive;
  }

  public void setWorkerOptions(WorkerOptions workerOptions) {
    this.workerOptions = workerOptions;
  }
}
