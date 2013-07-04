package org.cyclopsgroup.datamung.api.types;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class AgentConfig
    extends BaseComparableBean
{
    private String jobQueueUrl;

    public String getJobQueueUrl()
    {
        return jobQueueUrl;
    }

    public void setJobQueueUrl( String jobQueueUrl )
    {
        this.jobQueueUrl = jobQueueUrl;
    }
}
