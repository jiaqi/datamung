package org.cyclopsgroup.datamung.swf.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.InstanceNetwork;
import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

@XmlRootElement( name = "RunJobRequest" )
public class RunJobRequest
    extends BaseComparableBean
{
    private static final long DEFAULT_JOB_TIMEOUT_SECONDS = 120L;

    private Identity identity;

    private Job job;

    private long jobTimeoutSeconds = DEFAULT_JOB_TIMEOUT_SECONDS;

    private String keyPairName;

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

    public void setIdentity( Identity identity )
    {
        this.identity = identity;
    }

    public void setJob( Job job )
    {
        this.job = job;
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
