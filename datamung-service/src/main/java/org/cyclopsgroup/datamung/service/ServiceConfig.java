package org.cyclopsgroup.datamung.service;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class ServiceConfig
    extends BaseComparableBean
{
    private String agentAmiId;

    private String swfDomainName;

    public String getAgentAmiId()
    {
        return agentAmiId;
    }

    public String getSwfDomainName()
    {
        return swfDomainName;
    }

    public void setAgentAmiId( String agentAmiId )
    {
        this.agentAmiId = agentAmiId;
    }

    public void setSwfDomainName( String swfDomainName )
    {
        this.swfDomainName = swfDomainName;
    }
}
