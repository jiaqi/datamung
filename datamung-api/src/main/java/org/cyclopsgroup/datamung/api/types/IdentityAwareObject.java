package org.cyclopsgroup.datamung.api.types;

import javax.xml.bind.annotation.XmlElement;

public abstract class IdentityAwareObject {
	private Identity identity;

	public IdentityAwareObject() {
	}

	@XmlElement
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
}
