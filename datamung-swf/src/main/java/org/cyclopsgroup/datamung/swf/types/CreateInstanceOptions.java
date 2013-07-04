package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.InstanceNetwork;
import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class CreateInstanceOptions
    extends BaseComparableBean
{
    private InstanceNetwork network;

    private InstanceProfile profile;

    private String userData;

    public InstanceNetwork getNetwork()
    {
        return network;
    }

    public InstanceProfile getProfile()
    {
        return profile;
    }

    public String getUserData()
    {
        return userData;
    }

    public void setNetwork( InstanceNetwork network )
    {
        this.network = network;
    }

    public void setProfile( InstanceProfile profile )
    {
        this.profile = profile;
    }

    public void setUserData( String userData )
    {
        this.userData = userData;
    }
}
