package org.cyclopsgroup.datamung.api.types;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

@XmlType
public class InstanceNetwork
    extends BaseComparableBean
{
    public static InstanceNetwork ofPublic( Collection<String> securityGroupIds )
    {
        InstanceNetwork network = new InstanceNetwork();
        network.securityGroupIds = new HashSet<String>( securityGroupIds );
        return network;
    }

    public static InstanceNetwork ofVpc( String subnetId, String vpcId,
                                         Collection<String> securityGroupIds )
    {
        InstanceNetwork network = new InstanceNetwork();
        network.subnetId = subnetId;
        network.vpcId = vpcId;
        network.securityGroupIds = new HashSet<String>( securityGroupIds );
        return network;
    }

    private Set<String> securityGroupIds;

    private String subnetId;

    private String vpcId;

    public Set<String> getSecurityGroupIds()
    {
        return securityGroupIds;
    }

    @XmlElement
    public String getSubnetId()
    {
        return subnetId;
    }

    @XmlElement
    public String getVpcId()
    {
        return vpcId;
    }

    public void setSecurityGroupIds( Set<String> securityGroupIds )
    {
        this.securityGroupIds = new HashSet<String>( securityGroupIds );
    }

    public void setSubnetId( String subnetId )
    {
        this.subnetId = subnetId;
    }

    public void setVpcId( String vpcId )
    {
        this.vpcId = vpcId;
    }
}
