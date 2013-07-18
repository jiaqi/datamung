package org.cyclopsgroup.datamung.api.types;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

@XmlType
public class WorkerOptions
    extends BaseComparableBean
{
    private static final long DEFAULT_JOB_TIMEOUT_SECONDS = 120L;

    private static final long DEFAULT_LAUNCH_TIMEOUT_SECONDS = 300L;

    private long jobTimeoutSeconds = DEFAULT_JOB_TIMEOUT_SECONDS;

    private String keyPairName;

    private long launchTimeoutSeconds = DEFAULT_LAUNCH_TIMEOUT_SECONDS;

    private List<String> securityGroupIds;

    private String subnetId;

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
    public long getLaunchTimeoutSeconds()
    {
        return launchTimeoutSeconds;
    }

    @XmlElementWrapper
    @XmlElement( name = "groupId" )
    public List<String> getSecurityGroupIds()
    {
        return securityGroupIds;
    }

    @XmlElement
    public String getSubnetId()
    {
        return subnetId;
    }

    public void setJobTimeoutSeconds( long jobTimeoutSeconds )
    {
        this.jobTimeoutSeconds = jobTimeoutSeconds;
    }

    public void setKeyPairName( String keyPairName )
    {
        this.keyPairName = keyPairName;
    }

    public void setLaunchTimeoutSeconds( long launchTimeoutSeconds )
    {
        this.launchTimeoutSeconds = launchTimeoutSeconds;
    }

    public void setSecurityGroupIds( List<String> securityGroupIds )
    {
        this.securityGroupIds = securityGroupIds;
    }

    public void setSubnetId( String subnetId )
    {
        this.subnetId = subnetId;
    }
}
