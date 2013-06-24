package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.BaseType;

public class InstanceProfile
    extends BaseType
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
