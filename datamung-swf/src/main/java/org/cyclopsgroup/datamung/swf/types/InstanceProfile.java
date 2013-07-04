package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public class InstanceProfile
    extends BaseComparableBean
{
    private String arn;

    private String name;

    public String getArn()
    {
        return arn;
    }

    public String getName()
    {
        return name;
    }

    public void setArn( String arn )
    {
        this.arn = arn;
    }

    public void setName( String name )
    {
        this.name = name;
    }
}
