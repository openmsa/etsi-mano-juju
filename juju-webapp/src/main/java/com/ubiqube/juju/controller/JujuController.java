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
	public ResponseEntity<String> addCloud(@RequestParam("cloudname") @NotNull final String cloudname,
			@RequestParam("file") @NotNull final MultipartFile file) throws IOException {
		LOG.info("calling POST /cloud with cloudname={}",cloudname);
		try (final WorkspaceService ws = new WorkspaceService();
				InputStream is = file.getInputStream()) {
			LOG.info("{}: Deploying payload.", ws.getId());
			String filename = "openstack-cloud.yaml";
			ws.pushPayload(is, filename);
			LOG.info("{}: Call Install ", ws.getId());
			final ProcessResult res = ws.addCloud(cloudname, filename);
			LOG.info("{}: add-cloud done.", ws.getId());
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@GetMapping(value = "/cloud", produces = "application/json")
	public ResponseEntity<String> clouds() {
		LOG.info("calling GET /clouds");
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
	public ResponseEntity<String> addCredential(@RequestParam("cloudname") @NotNull final String cloudname,
			@RequestParam("file") @NotNull final MultipartFile file) throws IOException {
		LOG.info("calling /add-credential");
		try (final WorkspaceService ws = new WorkspaceService();
				InputStream is = file.getInputStream()) {
			String filename = "mycreds.yaml";
			ws.pushPayload(is, filename);
			final ProcessResult res = ws.addCredential(cloudname, filename);
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
	public ResponseEntity<String> updateCredential(@RequestParam("cloudname") @NotNull final String cloudname,
			@RequestParam("file") @NotNull final MultipartFile file) throws IOException {
		LOG.info("calling /update-credential");
		try (final WorkspaceService ws = new WorkspaceService();
				InputStream is = file.getInputStream()) {
			String filename = "mycreds.yaml";
			ws.pushPayload(is, filename);
			final ProcessResult res = ws.updateCredential(cloudname, filename);
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@DeleteMapping(value = "/credential/{name}", produces = "application/json")
	public ResponseEntity<String> removeCredential(@RequestParam("cloudname") @NotNull final String cloudname, @PathVariable("name") final String name) {
		LOG.info("calling /remove-credential/{}",name);
		try (final WorkspaceService ws = new WorkspaceService()) {
			return ResponseEntity.ok(ws.removeCredential(cloudname, name));
		}
	}

	@PostMapping(value = "/metadata", produces = "application/json")
	public ResponseEntity<String> genMetadata(@RequestParam("path") @NotNull final String path,
			@RequestParam("imageId") @NotNull final String imageId, @RequestParam("osSeries") @NotNull final String osSeries,
			@RequestParam("region") @NotNull final String region, @RequestParam("osAuthUrl") @NotNull final String osAuthUrl) {
		LOG.info("calling /metadata");
		try (final WorkspaceService ws = new WorkspaceService()) {
			return ResponseEntity.ok(ws.genMetadata(path, imageId, osSeries, region, osAuthUrl));
		}
	}

	@PostMapping(value = "/controller", produces = "application/json")
	public ResponseEntity<String> addController(@RequestParam("imageId") @NotNull final String imageId,
			@RequestParam("osSeries") @NotNull final String osSeries, @RequestParam("constraints") @NotNull final String constraints,
			@RequestParam("cloudname") @NotNull final String cloudname, @RequestParam("controllername") @NotNull final String controllername,
			@RequestParam("region") @NotNull final String region) {
		LOG.info("calling /metadata");
		try (final WorkspaceService ws = new WorkspaceService()) {
			return ResponseEntity.ok(ws.addController(imageId, osSeries, constraints, cloudname, controllername, region));
		}
	}
	
	@GetMapping(value = "/controller", produces = "application/json")
	public ResponseEntity<String> controllers() {
		LOG.info("calling /controllers");
		try (final WorkspaceService ws = new WorkspaceService()) {
			return ResponseEntity.ok(ws.controllers());
		}
	}

	@DeleteMapping(value = "/controller/{cloudname}/{name}", produces = "application/json")
	public ResponseEntity<String> removeController(@PathVariable("cloudname") final String cloudname, @PathVariable("name") final String name) {
		LOG.info("calling /remove-credential/{}",name);
		try (final WorkspaceService ws = new WorkspaceService()) {
			return ResponseEntity.ok(ws.removeController(cloudname, name));
		}
	}
	
}
