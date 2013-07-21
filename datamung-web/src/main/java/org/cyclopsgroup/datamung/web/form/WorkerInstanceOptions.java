package org.cyclopsgroup.datamung.web.form;

import java.util.List;

public class WorkerInstanceOptions
{
    private static final long DEFAULT_LAUNCH_TIMEOUT = 300;

    private String keypairName;

    private long launchTimeoutSeconds = DEFAULT_LAUNCH_TIMEOUT;

    private List<String> securityGroupIds;

    private String subsetId;

    public String getKeypairName()
    {
        return keypairName;
    }

    public long getLaunchTimeoutSeconds()
    {
        return launchTimeoutSeconds;
    }

    public List<String> getSecurityGroupIds()
    {
        return securityGroupIds;
    }

    public String getSubsetId()
    {
        return subsetId;
    }

    public void setKeypairName( String keypairName )
    {
        this.keypairName = keypairName;
    }

    public void setLaunchTimeoutSeconds( long launchTimeoutSeconds )
    {
        this.launchTimeoutSeconds = launchTimeoutSeconds;
    }

    public void setSecurityGroupIds( List<String> securityGroupIds )
    {
        this.securityGroupIds = securityGroupIds;
    }

    public void setSubsetId( String subsetId )
    {
        this.subsetId = subsetId;
    }
}
