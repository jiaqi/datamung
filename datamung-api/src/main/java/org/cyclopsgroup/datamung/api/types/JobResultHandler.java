package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlTransient;

public abstract class JobResultHandler
    extends BaseType
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
