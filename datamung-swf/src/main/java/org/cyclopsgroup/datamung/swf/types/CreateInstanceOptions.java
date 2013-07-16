package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.InstanceNetwork;
import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class CreateInstanceOptions
    extends BaseComparableBean
{
    private String instanceProfileName;

    private String keyPairName;

    private InstanceNetwork network;

    private String userData;

    public String getInstanceProfileName()
    {
        return instanceProfileName;
    }

    public String getKeyPairName()
    {
        return keyPairName;
    }

    public InstanceNetwork getNetwork()
    {
        return network;
    }

    public String getUserData()
    {
        return userData;
    }

    public void setInstanceProfileName( String instanceProfileName )
    {
        this.instanceProfileName = instanceProfileName;
    }

    public void setKeyPairName( String keyPairName )
    {
        this.keyPairName = keyPairName;
    }

    public void setNetwork( InstanceNetwork network )
    {
        this.network = network;
    }

    public void setUserData( String userData )
    {
        this.userData = userData;
    }
}
