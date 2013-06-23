package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "RunJobRequest" )
public class RunJobRequest
{
    private Identity identity;

    private Job job;

    private InstanceNetwork network;

    @XmlElement
    public Identity getIdentity()
    {
        return identity;
    }

    @XmlElement
    public Job getJob()
    {
        return job;
    }

    @XmlElement
    public InstanceNetwork getNetwork()
    {
        return network;
    }

    public void setIdentity( Identity identity )
    {
        this.identity = identity;
    }

    public void setJob( Job job )
    {
        this.job = job;
    }

    public void setNetwork( InstanceNetwork network )
    {
        this.network = network;
    }
}
