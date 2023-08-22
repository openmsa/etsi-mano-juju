package com.ubiqube.juju.controller;

import java.io.ByteArrayInputStream;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ubiqube.etsi.mano.dao.mano.juju.JujuCloud;
import com.ubiqube.etsi.mano.dao.mano.juju.JujuCredential;
import com.ubiqube.etsi.mano.dao.mano.juju.JujuMetadata;
import com.ubiqube.etsi.mano.dao.mano.juju.JujuRegion;
import com.ubiqube.juju.service.ProcessResult;
import com.ubiqube.juju.service.WorkspaceService;

import jakarta.validation.constraints.NotNull;

@Validated
@RestController("/")
public class JujuController {

	private static final Logger LOG = LoggerFactory.getLogger(JujuController.class);

	@PostMapping(value = "/cloud", produces = "application/json")
	public ResponseEntity<String> addCloud(@RequestBody @NotNull final JujuCloud cloud) {
		LOG.info("calling POST /cloud with name={}, cloud type={}\n{}",cloud.getName(),cloud.getType(),cloud);
		try (final WorkspaceService ws = new WorkspaceService();) {
			String cloudString = genCloudYml(cloud);
		    InputStream is = new ByteArrayInputStream(cloudString.getBytes());
			String filename = "openstack-play.yaml";
			ws.pushPayload(is, filename);
			LOG.info("{}: Call Install ", ws.getId());
			final ProcessResult res = ws.addCloud(cloud.getName(), filename);
			LOG.info("{}: add-cloud done.", ws.getId());
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@GetMapping(value = "/cloud", produces = "application/json")
	public ResponseEntity<String> clouds() {
		LOG.info("calling GET /cloud");
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.clouds();
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@GetMapping(value = "/cloud/{name}", produces = "application/json")
	public ResponseEntity<String> cloudDetail(@PathVariable("name") @NotNull final String name) {
		LOG.info("calling GET /cloud");
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.cloudDetail(name);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@DeleteMapping(value = "/cloud/{name}", produces = "application/json")
	public ResponseEntity<String> removeCloud(@PathVariable("name") @NotNull final String name) {
		LOG.info("calling DELETE /cloud with name: {}",name);
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.removeCloud(name);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@PostMapping(value = "/credential", produces = "application/json")
	public ResponseEntity<String> addCredential(@RequestBody @NotNull final JujuCloud cloud) {
		LOG.info("calling POST /credential with object: {}", cloud);
		try (final WorkspaceService ws = new WorkspaceService();) {
			String credentialString = genCredentialYml(cloud);
		    InputStream is = new ByteArrayInputStream(credentialString.getBytes());
			LOG.info("{}: Deploying payload.", ws.getId());
			String filename = "mycreds.yaml";
			ws.pushPayload(is, filename);

			final ProcessResult res = ws.addCredential(cloud.getName(), filename);
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@GetMapping(value = "/credential", produces = "application/json")
	public ResponseEntity<String> credentials() {
		LOG.info("calling GET /credential");
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.credentials();
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@GetMapping(value = "/credential/{cloudname}/{name}", produces = "application/json")
	public ResponseEntity<String> credentialDetails(@PathVariable("cloudname") @NotNull final String cloudname, @PathVariable("name") final String name) {
		LOG.info("calling GET /credentialDetails with cloudname:{} and name=:{}",cloudname, name);
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.credentialDetail(cloudname, name);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@PutMapping(value = "/credential", produces = "application/json")
	public ResponseEntity<String> updateCredential(@RequestBody @NotNull final JujuCloud cloud) {
		LOG.info("calling PUT /credential with object: {}", cloud);
		try (final WorkspaceService ws = new WorkspaceService();) {
			String credentialString = genCredentialYml(cloud);
		    InputStream is = new ByteArrayInputStream(credentialString.getBytes());
			LOG.info("{}: Deploying payload.", ws.getId());
			String filename = "mycreds.yaml";
			ws.pushPayload(is, filename);
			final ProcessResult res = ws.updateCredential(cloud.getName(), filename);
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@DeleteMapping(value = "/credential/{cloudname}/{name}", produces = "application/json")
	public ResponseEntity<String> removeCredential(@PathVariable("cloudname") @NotNull final String cloudname, @PathVariable("name") final String name) {
		LOG.info("calling DELETE /credential with cloudname:{} and name=:{}",cloudname, name);
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.removeCredential(cloudname, name);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());

		}
	}

	public ResponseEntity<String> addMetadata(@RequestParam("path") @NotNull final String path,
			@RequestParam("imageId") @NotNull final String imageId, @RequestParam("osSeries") @NotNull final String osSeries,
			@RequestParam("region") @NotNull final String region, @RequestParam("osAuthUrl") @NotNull final String osAuthUrl) {
		LOG.info("calling POST /metadata");
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.genMetadata(path, imageId, osSeries, region, osAuthUrl);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@PostMapping(value = "/metadata", produces = "application/json")
	public ResponseEntity<String> genMetadata(@RequestBody @NotNull final JujuMetadata meta) {
		LOG.info("calling POST /metadata with object: {}", meta);
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.genMetadata(meta.getPath(), meta.getImageId(), meta.getOsSeries(), meta.getRegionName(), meta.getOsAuthUrl());
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	public ResponseEntity<String> addController2(@RequestParam("imageId") @NotNull final String imageId,
			@RequestParam("osSeries") @NotNull final String osSeries, @RequestParam("constraints") @NotNull final String constraints,
			@RequestParam("cloudname") @NotNull final String cloudname, @RequestParam("controllername") @NotNull final String controllername,
			@RequestParam("region") @NotNull final String region, @RequestParam("networkId") @NotNull final String networkId) {
		LOG.info("post /controller");
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.addController(imageId, osSeries, constraints, cloudname, controllername, region, networkId);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@PostMapping(value = "/controller/{cloudname}", produces = "application/json")
	public ResponseEntity<String> addController(@PathVariable("cloudname") @NotNull final String cloudname, @RequestBody @NotNull final JujuMetadata controller) {
		LOG.info("post /controller");
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.addController(cloudname, controller);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@GetMapping(value = "/controller", produces = "application/json")
	public ResponseEntity<String> controllers() {
		LOG.info("get /controllers");
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.controllers();
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@GetMapping(value = "/controller/{controllername}", produces = "application/json")
	public ResponseEntity<String> controllerDetail(@PathVariable("controllername") @NotNull final String controllername) {
		LOG.info("get /showController/{}",controllername);
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.showController(controllername);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@DeleteMapping(value = "/controller/{controllername}", produces = "application/json")
	public ResponseEntity<String> removeController(@PathVariable("controllername") @NotNull final String controllername) {
		LOG.info("delete /remove-controller/{}",controllername);
		try (final WorkspaceService ws = new WorkspaceService()) {
		    InputStream is = new ByteArrayInputStream(controllername.getBytes());
			ws.pushPayload(is, "answer");
			final ProcessResult res = ws.removeController(controllername);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@PostMapping(value = "/model/{name}", produces = "application/json")
	public ResponseEntity<String> addModel(@PathVariable("name") @NotNull final String name) {
		LOG.info("post /add-model");
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.addModel(name);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@GetMapping(value = "/model", produces = "application/json")
	public ResponseEntity<String> model() {
		LOG.info("get /models");
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.model();
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@DeleteMapping(value = "/model/{name}", produces = "application/json")
	public ResponseEntity<String> removeModel(@PathVariable("name") @NotNull final String name) {
		LOG.info("delete /destroy-model/{}",name);
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.removeModel(name);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@PostMapping(value = "/application/{charm}/{name}", produces = "application/json")
	public ResponseEntity<String> deployApp(@PathVariable("charm") @NotNull final String charm, @PathVariable("name") @NotNull final String name) {
		LOG.info("post /deploy/{}/{}",charm,name);
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.deployApp(charm, name);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@GetMapping(value = "/application/{name}", produces = "application/json")
	public ResponseEntity<String> application(@PathVariable("name") @NotNull final String name) {
		LOG.info("calling /show-application/{}",name);
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.application(name);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@DeleteMapping(value = "/application/{name}", produces = "application/json")
	public ResponseEntity<String> removeApplication(@PathVariable("name") @NotNull final String name) {
		LOG.info("calling /remove-application/{}",name);
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.removeApplication(name);
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@GetMapping(value = "/status", produces = "application/json")
	public ResponseEntity<String> status() {
		LOG.info("calling /status");
		try (final WorkspaceService ws = new WorkspaceService()) {
			final ProcessResult res = ws.status();
			LOG.info(res.getStdout());
			LOG.error(res.getErrout());
			return ResponseEntity.ok(res.getStdout());
		}
	}

	private String genCloudYml(JujuCloud cloud) {
/*
clouds:
    openstack-cloud-240:
      type: openstack
      auth-types: [userpass]
      regions:
        RegionOne:
          endpoint: http://10.31.1.240:5000/v3
*/
		StringBuilder str = new StringBuilder("clouds:\n");
		str.append("    "+cloud.getName()+":\n");
		str.append("      type: openstack\n");
		str.append("      auth-types: [userpass]\n");
		str.append("      regions:\n");
		if (cloud.getRegions()!=null && !cloud.getRegions().isEmpty()) {
			for (JujuRegion region: cloud.getRegions()) {
				str.append("        "+region.getName()+":\n");
				str.append("          endpoint: "+region.getEndPoint()+"\n");
			}
		}
		LOG.info("CloudYaml:\n{}",str.toString());
		return str.toString();
	}

	private String genCredentialYml(JujuCloud cloud) {
/*
credentials:
  openstack-cloud-240: # .240 Openstack instance
    admin:
      auth-type: userpass
      password: 13f83cb78a4f4213
      tenant-name: admin
      username: admin
	
 */
		JujuCredential credential = cloud.getCredential();
		StringBuilder str = new StringBuilder("credentials:\n");
		str.append("  "+cloud.getName()+":\n");
		str.append("    "+credential.getName()+":\n");
		str.append("      auth-type: "+credential.getAuthType()+"\n");
		str.append("      password: "+credential.getPassword()+"\n");
		str.append("      tenant-name: "+credential.getTenantName()+"\n");
		str.append("      username: "+credential.getUsername()+"\n");
		LOG.info("CredentialYaml:\n{}",str.toString());
		return str.toString();
	}

}
