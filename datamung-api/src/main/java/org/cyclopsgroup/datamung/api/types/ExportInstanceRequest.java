package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ExportInstanceRequest")
public class ExportInstanceRequest extends IdentityAwareObject {
	private DataArchive destinationArchive;
	private String instanceName;

	@XmlElement
	public DataArchive getDestinationArchive() {
		return destinationArchive;
	}

	@XmlElement
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
