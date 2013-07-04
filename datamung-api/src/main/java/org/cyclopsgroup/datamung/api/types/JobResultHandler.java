package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlTransient;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public abstract class JobResultHandler
    extends BaseComparableBean
{
    public static enum Type
    {
        S3, SQS;
    }

    private final Type handlerType;

    JobResultHandler( Type type )
    {
        this.handlerType = type;
    }

    public JobResultHandler()
    {
        throw new IllegalStateException( "Not supposed to be called" );
    }

    @XmlTransient
    public Type getHandlerType()
    {
        return handlerType;
    }
}
