package org.cyclopsgroup.datamung.web.form;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.apache.commons.lang.StringUtils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBSnapshotsRequest;
import com.amazonaws.services.s3.AmazonS3Client;

@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
@Documented
@Constraint( validatedBy = ValidAwsCredential.Validator.class )
public @interface ValidAwsCredential
{
    public static class Validator
        implements
        ConstraintValidator<ValidAwsCredential, CredentialsAndAction>
    {
        @Override
        public void initialize( ValidAwsCredential constraintAnnotation )
        {
        }

        @Override
        public boolean isValid( CredentialsAndAction value,
                                ConstraintValidatorContext context )
        {
            AWSCredentials creds =
                new BasicAWSCredentials( value.getAwsAccessKeyId(),
                                         value.getAwsSecretKey() );
            if ( StringUtils.isBlank( creds.getAWSAccessKeyId() )
                || StringUtils.isBlank( creds.getAWSSecretKey() ) )
            {
                context.buildConstraintViolationWithTemplate( "Access key id and secret key must not be empty" ).addConstraintViolation();
                return false;
            }
            if ( value.getActionType() == null )
            {
                context.buildConstraintViolationWithTemplate( "Type of job is not selected" ).addConstraintViolation();
                return false;
            }
            try
            {
                new AmazonS3Client( creds ).listBuckets();
                AmazonRDS rds = new AmazonRDSClient( creds );
                switch ( value.getActionType() )
                {
                    case BACKUP_INSTANCE:
                        if ( rds.describeDBInstances( new DescribeDBInstancesRequest().withMaxRecords( 20 ) ).getDBInstances().isEmpty() )
                        {
                            context.buildConstraintViolationWithTemplate( "There is not database instance to backup" ).addConstraintViolation();
                            return false;
                        }
                        break;
                    case CONVERT_SNAPSHOT:
                        if ( rds.describeDBSnapshots( new DescribeDBSnapshotsRequest().withMaxRecords( 20 ) ).getDBSnapshots().isEmpty() )
                        {
                            context.buildConstraintViolationWithTemplate( "There is not database snapshot to convert" ).addConstraintViolation();
                            return false;
                        }
                        break;
                    default:
                }
                return true;
            }
            catch ( AmazonServiceException e )
            {
                context.buildConstraintViolationWithTemplate( e.getMessage() ).addConstraintViolation();
                return false;
            }
        }
    }

    Class<?>[] groups() default {};

    String message();

    Class<? extends Payload>[] payload() default {};
}
