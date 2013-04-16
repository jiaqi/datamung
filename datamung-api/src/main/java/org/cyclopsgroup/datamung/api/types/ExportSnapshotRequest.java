package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ExportInstanceRequest")
public class ExportSnapshotRequest extends IdentityAwareObject {
	private DataArchive destinationArchive;
	private String snapshotName;

	@XmlElement
	public DataArchive getDestinationArchive() {
		return destinationArchive;
	}

	@XmlElement
	public String getSnapshotName() {
		return snapshotName;
	}

	public void setDestinationArchive(DataArchive destination) {
		this.destinationArchive = destination;
	}

	public void setSnapshotName(String snapshotName) {
		this.snapshotName = snapshotName;
	}
}
