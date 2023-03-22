package com.ubiqube.juju.controller;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ubiqube.juju.service.ProcessResult;
import com.ubiqube.juju.service.WorkspaceService;

import jakarta.validation.constraints.NotNull;

@SuppressWarnings("static-method")
@Validated
@RestController("/")
public class JujuController {

	private static final Logger LOG = LoggerFactory.getLogger(JujuController.class);

	@PostMapping(value = "/cloud", consumes = { "multipart/form-data" }, produces = "application/json")
	public ResponseEntity<String> addCloud(@RequestParam("file") @NotNull final MultipartFile file) throws IOException {
		LOG.info("calling /add-cloud.");
		try (final WorkspaceService ws = new WorkspaceService();
				InputStream is = file.getInputStream()) {
			LOG.info("{}: Deploying payload.", ws.getId());
			String filename = "openstack-cloud.yaml";
			ws.pushPayload(is, filename);
			LOG.info("{}: Call Install ", ws.getId());
			final ProcessResult res = ws.addCloud(filename);
			LOG.info("{}: add-cloud done.", ws.getId());
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@GetMapping(value = "/cloud", produces = "application/json")
	public ResponseEntity<String> clouds() {
		LOG.info("calling /clouds");
		try (final WorkspaceService ws = new WorkspaceService()) {
			return ResponseEntity.ok(ws.clouds());
		}
	}

	@DeleteMapping(value = "/cloud/{name}", produces = "application/json")
	public ResponseEntity<String> removeCloud(@PathVariable("name") final String name) {
		LOG.info("calling /remove-cloud/{}",name);
		try (final WorkspaceService ws = new WorkspaceService()) {
			return ResponseEntity.ok(ws.removeCloud(name));
		}
	}

	@PostMapping(value = "/credential", consumes = { "multipart/form-data" }, produces = "application/json")
	public ResponseEntity<String> addCredential(@RequestParam("file") @NotNull final MultipartFile file) throws IOException {
		LOG.info("calling /add-credential");
		try (final WorkspaceService ws = new WorkspaceService();
				InputStream is = file.getInputStream()) {
			String filename = "mycreds.yaml";
			ws.pushPayload(is, filename);
			final ProcessResult res = ws.addCredential(filename);
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@GetMapping(value = "/credential", produces = "application/json")
	public ResponseEntity<String> credentials() {
		LOG.info("calling /credentials");
		try (final WorkspaceService ws = new WorkspaceService()) {
			return ResponseEntity.ok(ws.credentials());
		}
	}

	@PutMapping(value = "/credential", consumes = { "multipart/form-data" }, produces = "application/json")
	public ResponseEntity<String> updateCredential(@RequestParam("file") @NotNull final MultipartFile file) throws IOException {
		LOG.info("calling /update-credential");
		try (final WorkspaceService ws = new WorkspaceService();
				InputStream is = file.getInputStream()) {
			String filename = "mycreds.yaml";
			ws.pushPayload(is, filename);
			final ProcessResult res = ws.updateCredential(filename);
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@DeleteMapping(value = "/credential/{name}", produces = "application/json")
	public ResponseEntity<String> removeCredential(@PathVariable("name") final String name) {
		LOG.info("calling /remove-credential/{}",name);
		try (final WorkspaceService ws = new WorkspaceService()) {
			return ResponseEntity.ok(ws.removeCredential(name));
		}
	}

}
