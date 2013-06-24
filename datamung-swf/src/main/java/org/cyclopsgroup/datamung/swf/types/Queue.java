package org.cyclopsgroup.datamung.swf.types;

import org.cyclopsgroup.datamung.api.types.BaseType;

public class Queue
    extends BaseType
{
    private String arn;

    private String queueName;

    private String queueUrl;

    public String getArn()
    {
        return arn;
    }

    public String getQueueName()
    {
        return queueName;
    }

    public String getQueueUrl()
    {
        return queueUrl;
    }

    public void setArn( String arn )
    {
        this.arn = arn;
    }

    public void setQueueName( String queueName )
    {
        this.queueName = queueName;
    }

    public void setQueueUrl( String queueUrl )
    {
        this.queueUrl = queueUrl;
    }
}
