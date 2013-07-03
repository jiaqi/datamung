package org.cyclopsgroup.datamung.service.activities;

import org.cyclopsgroup.datamung.api.types.Identity;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;

class ActivityUtils
{
    static <T extends AmazonWebServiceRequest> T decorate( T request,
                                                           Identity id )
    {
        AWSCredentials creds;
        if ( id.getAwsAccessToken() == null )
        {
            creds =
                new BasicAWSCredentials( id.getAwsAccessKeyId(),
                                         id.getAwsSecretKey() );
        }
        else
        {
            creds =
                new BasicSessionCredentials( id.getAwsAccessKeyId(),
                                             id.getAwsSecretKey(),
                                             id.getAwsAccessToken() );
        }
        request.setRequestCredentials( creds );
        return request;
    }
}
