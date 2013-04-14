package org.cyclopsgroup.datamung.api.types;

public abstract class IdentityAwareObject {
	private Identity identity;

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
}
