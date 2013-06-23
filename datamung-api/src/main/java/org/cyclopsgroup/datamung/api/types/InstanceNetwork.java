package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class InstanceNetwork
{
    private String subnetId;

    private String vpcId;

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
