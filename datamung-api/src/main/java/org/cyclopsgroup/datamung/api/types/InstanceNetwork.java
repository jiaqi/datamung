package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class InstanceNetwork
    extends BaseType
{
    public static InstanceNetwork ofVpc( String subnetId, String vpcId )
    {
        InstanceNetwork network = new InstanceNetwork();
        network.subnetId = subnetId;
        network.vpcId = vpcId;
        return network;
    }

    private String subnetId;

    private String vpcId;

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

    public void setSubnetId( String subnetId )
    {
        this.subnetId = subnetId;
    }

    public void setVpcId( String vpcId )
    {
        this.vpcId = vpcId;
    }
}
