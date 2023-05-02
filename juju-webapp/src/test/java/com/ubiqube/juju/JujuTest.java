package com.ubiqube.juju;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ubiqube.juju.service.WorkspaceService;

@SuppressWarnings("static-method")
class JujuTest {
	@Test
	void dummyTest() {
		assertTrue(true);
	}

	void testName() throws Exception {
		try (final WorkspaceService ws = new WorkspaceService()) {
			ws.credentials();
		}
	}

}
