package org.cyclopsgroup.datamung.api.types;

public class ExportInstanceRequest extends IdentityAwareObject {
	private DataArchive destinationArchive;
	private String instanceName;

	public DataArchive getDestinationArchive() {
		return destinationArchive;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setDestinationArchive(DataArchive destination) {
		this.destinationArchive = destination;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
}
