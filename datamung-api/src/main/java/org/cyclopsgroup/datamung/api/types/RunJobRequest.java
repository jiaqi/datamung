package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

@XmlRootElement( name = "RunJobRequest" )
public class RunJobRequest
    extends BaseComparableBean
{
    private int failAfterRetries = 5;

    private Job job;

    private InstanceNetwork network;

    @XmlElement
    public int getFailAfterRetries()
    {
        return failAfterRetries;
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

    public void setFailAfterRetries( int failAfterRetries )
    {
        this.failAfterRetries = failAfterRetries;
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
