package org.cyclopsgroup.datamung.swf.types;

public class CommandLineJob extends Job {
  public CommandLineJob() {
    super(Type.COMMAND_LINE);
  }

  private String command;

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }
}
