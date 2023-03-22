package com.ubiqube.juju;

public class JujuException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public JujuException(final Throwable e) {
		super(e);
	}

	public JujuException(final String string) {
		super(string);
	}

}
