package org.cyclopsgroup.datamung.api.types;

public class Identity {
	private String awsAccessKeyId;
	private String awsAccessToken;
	private String awsSecretKey;

	public String getAwsAccessKeyId() {
		return awsAccessKeyId;
	}

	public String getAwsAccessToken() {
		return awsAccessToken;
	}

	public String getAwsSecretKey() {
		return awsSecretKey;
	}

	public void setAwsAccessKeyId(String awsAccessKeyId) {
		this.awsAccessKeyId = awsAccessKeyId;
	}

	public void setAwsAccessToken(String awsAccessToken) {
		this.awsAccessToken = awsAccessToken;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}
}
