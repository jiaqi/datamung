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
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.s3.AmazonS3Client;

@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
@Documented
@Constraint( validatedBy = ValidAwsCredential.Validator.class )
public @interface ValidAwsCredential
{
    public static class Validator
        implements ConstraintValidator<ValidAwsCredential, AwsCredentialAware>
    {
        @Override
        public void initialize( ValidAwsCredential constraintAnnotation )
        {
        }

        @Override
        public boolean isValid( AwsCredentialAware value,
                                ConstraintValidatorContext context )
        {
            AWSCredentials creds = value.toAwsCredential();
            if ( StringUtils.isBlank( creds.getAWSAccessKeyId() )
                || StringUtils.isBlank( creds.getAWSSecretKey() ) )
            {
                context.buildConstraintViolationWithTemplate( "Access key id and secret key must not be empty" ).addConstraintViolation();
                return false;
            }
            try
            {
                new AmazonS3Client( creds ).listBuckets();
                AmazonRDS rds = new AmazonRDSClient( creds );
                rds.describeDBInstances();
                rds.describeDBSnapshots();
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
