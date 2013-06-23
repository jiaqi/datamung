package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.Identity;
import org.cyclopsgroup.datamung.api.types.InstanceNetwork;

public class CreateInstanceOptions
{
    private Identity identity;

    private InstanceNetwork network;

    private InstanceProfile profile;

    private String userData;

    public Identity getIdentity()
    {
        return identity;
    }

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

    public void setIdentity( Identity identity )
    {
        this.identity = identity;
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
