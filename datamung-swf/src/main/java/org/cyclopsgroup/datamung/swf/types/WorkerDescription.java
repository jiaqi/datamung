package org.cyclopsgroup.datamung.swf.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "WorkerDescription" )
public class WorkerDescription
{
    private String publicIpAddress;

    private String workerId;

    @XmlElement
    public String getPublicIpAddress()
    {
        return publicIpAddress;
    }

    @XmlElement
    public String getWorkerId()
    {
        return workerId;
    }

    public void setPublicIpAddress( String publicIpAddress )
    {
        this.publicIpAddress = publicIpAddress;
    }

    public void setWorkerId( String workerId )
    {
        this.workerId = workerId;
    }
}
