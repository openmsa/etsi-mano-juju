package com.ubiqube.juju;

import org.junit.jupiter.api.Test;

import com.ubiqube.juju.service.WorkspaceService;

class JujuTest {

	@Test
	void testName() throws Exception {
		try (final WorkspaceService ws = new WorkspaceService()) {
			ws.credentials();
		}
	}

}
