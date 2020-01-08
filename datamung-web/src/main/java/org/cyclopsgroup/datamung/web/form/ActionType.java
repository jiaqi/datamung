package org.cyclopsgroup.datamung.web.form;

public enum ActionType {
  BACKUP_INSTANCE("Backup an RDS MySQL instance as file in S3"),
  CONVERT_SNAPSHOT("Convert an RDS MySQL snapshot to file in S3");

  private final String description;

  private ActionType(String description) {
    this.description = description;
  }

  public final String getDescription() {
    return description;
  }
}
