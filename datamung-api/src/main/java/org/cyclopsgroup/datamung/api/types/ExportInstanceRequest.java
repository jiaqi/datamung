package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "ExportInstanceRequest" )
public class ExportInstanceRequest
    extends ExportRequest
{
    private String instanceName;

    private boolean liveInstanceTouched;

    @XmlElement
    public String getInstanceName()
    {
        return instanceName;
    }

    @XmlElement
    public boolean isLiveInstanceTouched()
    {
        return liveInstanceTouched;
    }

    public void setInstanceName( String instanceName )
    {
        this.instanceName = instanceName;
    }

    public void setLiveInstanceTouched( boolean liveInstanceTouched )
    {
        this.liveInstanceTouched = liveInstanceTouched;
    }
}
