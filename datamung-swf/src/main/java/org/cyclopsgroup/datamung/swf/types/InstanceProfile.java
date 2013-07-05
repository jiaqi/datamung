package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class InstanceProfile
    extends BaseComparableBean
{
    public static InstanceProfile of( String name )
    {
        InstanceProfile p = new InstanceProfile();
        p.name = name;
        return p;
    }

    private String name;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }
}
