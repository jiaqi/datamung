package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "ExportInstanceRequest" )
public class ExportInstanceRequest
    extends ExportRequest
{
    private String instanceName;

    @XmlElement
    public String getInstanceName()
    {
        return instanceName;
    }

    public void setInstanceName( String instanceName )
    {
        this.instanceName = instanceName;
    }
}
