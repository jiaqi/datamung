package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.DataArchive;
import org.cyclopsgroup.datamung.api.types.Identity;

public class MySQLDumpJob extends Job {
  private DataArchive dataArchive;

  private DatabaseInstance databaseInstance;

  private Identity identity;

  private String masterPassword;

  public MySQLDumpJob() {
    super(Type.MYSQLDUMP);
  }

  public DataArchive getDataArchive() {
    return dataArchive;
  }

  public DatabaseInstance getDatabaseInstance() {
    return databaseInstance;
  }

  public Identity getIdentity() {
    return identity;
  }

  public String getMasterPassword() {
    return masterPassword;
  }

  public void setDataArchive(DataArchive dataArchive) {
    this.dataArchive = dataArchive;
  }

  public void setDatabaseInstance(DatabaseInstance databaseInstance) {
    this.databaseInstance = databaseInstance;
  }

  public void setIdentity(Identity identity) {
    this.identity = identity;
  }

  public void setMasterPassword(String masterPassword) {
    this.masterPassword = masterPassword;
  }
}
