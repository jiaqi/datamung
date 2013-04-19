package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "ExportInstanceRequest" )
public class ExportSnapshotRequest
    extends ExportRequest
{
    private String snapshotName;

    @XmlElement
    public String getSnapshotName()
    {
        return snapshotName;
    }

    public void setSnapshotName( String snapshotName )
    {
        this.snapshotName = snapshotName;
    }
}
