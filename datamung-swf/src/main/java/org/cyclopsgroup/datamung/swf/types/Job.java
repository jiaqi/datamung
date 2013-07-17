package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.kaufman.interfaces.BaseComparableBean;

public abstract class Job
    extends BaseComparableBean
{
    public static enum Type
    {
        COMMAND_LINE, MYSQLDUMP;
    }

    private final Type jobType;

    Job( Type jobType )
    {
        this.jobType = jobType;
    }

    public Job()
    {
        throw new IllegalStateException(
                                         "This constructor is not supposed to be called" );
    }

    public Type getJobType()
    {
        return jobType;
    }
}
