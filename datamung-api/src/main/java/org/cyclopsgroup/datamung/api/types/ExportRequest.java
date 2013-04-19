package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;

public abstract class ExportRequest
    extends IdentityAwareObject
{
    private DataArchive destinationArchive;

    @XmlElement
    public DataArchive getDestinationArchive()
    {
        return destinationArchive;
    }

    public void setDestinationArchive( DataArchive destinationArchive )
    {
        this.destinationArchive = destinationArchive;
    }
}
