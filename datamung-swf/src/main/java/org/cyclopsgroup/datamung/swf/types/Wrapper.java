package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.BaseType;

public class Wrapper<T>
    extends BaseType
{
    public static <T> Wrapper<T> of( T object )
    {
        Wrapper<T> w = new Wrapper<T>();
        w.object = object;
        return w;
    }

    private T object;

    public T getObject()
    {
        return object;
    }

    public void setObject( T object )
    {
        this.object = object;
    }
}
