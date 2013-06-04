package org.cyclopsgroup.datamung.web.form;

import com.amazonaws.auth.AWSCredentials;

public interface AwsCredentialAware
{
    AWSCredentials toAwsCredential();
}
