package org.cyclopsgroup.datamung.web.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

@ValidAwsCredential( message = "AWS credentials has problem, perhaps it doesn't have required permissions." )
public class CredentialsAndAction
    implements AwsCredentialAware
{
    public static enum ActionType
    {
        BACKUP_INSTANCE( "Backup an RDS MySQL instance as file in S3" ),
        CONVERT_SNAPSHOT( "Convert an RDS MySQL snapshot to file in S3" ),
        RESTORE_INSTANCE( "Restore an RDS MySQL instance from file in S3" ),
        RESTORE_SNAPSHOT( "Convert a file in S3 into RDS MySQL snapshot" );

        private final String description;

        private ActionType( String description )
        {
            this.description = description;
        }

        public final String getDescription()
        {
            return description;
        }
    }

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

    /**
     * @return
     */
    @Override
    public AWSCredentials toAwsCredential()
    {
        return new BasicAWSCredentials( awsAccessKeyId, awsSecretKey );
    }
}
