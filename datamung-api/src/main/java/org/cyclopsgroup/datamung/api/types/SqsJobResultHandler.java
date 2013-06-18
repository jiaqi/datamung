package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class SqsJobResultHandler
    extends JobResultHandler
{
    private String resultQueueUrl;

    public SqsJobResultHandler()
    {
        super( Type.SQS );
    }

    @XmlElement
    public String getResultQueueUrl()
    {
        return resultQueueUrl;
    }

    public void setResultQueueUrl( String resultQueueUrl )
    {
        this.resultQueueUrl = resultQueueUrl;
    }
}
