package org.cyclopsgroup.datamung.web.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

@ValidAwsCredential( message = "AWS credentials has problem, perhaps it doesn't have required permissions" )
public class CredentialsAndAction
{
    @NotNull( message = "An action type must be selected" )
    private ActionType actionType;

    @Size( min = 1, message = "AWS access key Id must be specified" )
    private String awsAccessKeyId;

    @Size( min = 1, message = "AWS secret key must be specified" )
    private String awsSecretKey;

    public ActionType getActionType()
    {
        return actionType;
    }

    public String getAwsAccessKeyId()
    {
        return awsAccessKeyId;
    }

    public String getAwsSecretKey()
    {
        return awsSecretKey;
    }

    public void setActionType( ActionType actionType )
    {
        this.actionType = actionType;
    }

    public void setAwsAccessKeyId( String awsAccessKeyId )
    {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public void setAwsSecretKey( String awsSecretKey )
    {
        this.awsSecretKey = awsSecretKey;
    }

    public AWSCredentials toAwsCredential()
    {
        return new BasicAWSCredentials( awsAccessKeyId, awsSecretKey );
    }
}
