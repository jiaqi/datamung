package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.BaseType;
import org.cyclopsgroup.datamung.api.types.InstanceNetwork;

public class CreateInstanceOptions
    extends BaseType
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
