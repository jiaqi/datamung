package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

@XmlType
public class WorkerOptions
    extends BaseComparableBean
{
    private long jobTimeoutSeconds;

    private String keyPairName;

    private InstanceNetwork network;

    @XmlElement
    public long getJobTimeoutSeconds()
    {
        return jobTimeoutSeconds;
    }

    @XmlElement
    public String getKeyPairName()
    {
        return keyPairName;
    }

    @XmlElement
    public InstanceNetwork getNetwork()
    {
        return network;
    }

    public void setJobTimeoutSeconds( long jobTimeoutSeconds )
    {
        this.jobTimeoutSeconds = jobTimeoutSeconds;
    }

    public void setKeyPairName( String keyPairName )
    {
        this.keyPairName = keyPairName;
    }

    public void setNetwork( InstanceNetwork network )
    {
        this.network = network;
    }
}
