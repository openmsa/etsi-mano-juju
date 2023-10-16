package com.ubiqube.juju.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.ubiqube.juju.JujuException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.ubiqube.etsi.mano.service.juju.entities.JujuCloud;
import com.ubiqube.etsi.mano.service.juju.entities.JujuCredential;
import com.ubiqube.etsi.mano.service.juju.entities.JujuMetadata;
import com.ubiqube.etsi.mano.service.juju.entities.JujuRegion;
import com.ubiqube.juju.service.ProcessResult;
import com.ubiqube.juju.service.WorkspaceService;

import jakarta.validation.constraints.NotNull;

@Validated
@RestController("/")
@SuppressWarnings("static-method")
public class JujuController {

	@Autowired
	WorkspaceService ws;
	private static final Logger LOG = LoggerFactory.getLogger(JujuController.class);

	@PostMapping(value = "/cloud", produces = "application/json")
	public ResponseEntity<String> addCloud(@RequestBody @NotNull final JujuCloud cloud) {
		LOG.info("calling POST /cloud with name={}, cloud type={}\n{}", cloud.getName(), cloud.getType(), cloud);
		final String cloudString = genCloudYml(cloud);
		final InputStream is = new ByteArrayInputStream(cloudString.getBytes());
		final String filename = "openstack-play.yaml";
		ws.pushPayload(is, filename);
		LOG.info("{}: Call Install ", ws.getId());
		final ProcessResult res = ws.addCloud(cloud.getName(), filename);
		LOG.info("{}: add-cloud done.", ws.getId());
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		if(res.getExitCode()==1){
			throw new JujuException(res.getErrout());
		}
		else {
			return ResponseEntity.ok(res.getErrout());
		}
	}

	@GetMapping(value = "/cloud", produces = "application/json")
	public ResponseEntity<String> clouds() {
		LOG.info("calling GET /cloud");
		final ProcessResult res = ws.clouds();
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		return ResponseEntity.ok(res.getStdout());
	}

	@GetMapping(value = "/cloud/{name}", produces = "application/json")
	public ResponseEntity<String> cloudDetail(@PathVariable("name") @NotNull final String name) {
		LOG.info("calling GET /cloud");
		final ProcessResult res = ws.cloudDetail(name);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		if(res.getExitCode()==1){
			throw new JujuException(res.getErrout());
		}
		else{
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@DeleteMapping(value = "/cloud/{name}", produces = "application/json")
	public ResponseEntity<String> removeCloud(@PathVariable("name") @NotNull final String name) {
		LOG.info("calling DELETE /cloud with name: {}", name);
		final ProcessResult res = ws.cloudDetail(name);
		final ProcessResult res2 = ws.removeCloud(name);
		LOG.info(res2.getStdout());
		LOG.error(res2.getErrout());
		if(res.getExitCode()==1){
			throw new JujuException(res.getErrout());
		}
		else{
			
			return ResponseEntity.ok(res2.getErrout());
		}
	}

	@PostMapping(value = "/credential", produces = "application/json")
	public ResponseEntity<String> addCredential(@RequestBody @NotNull final JujuCloud cloud) {
		LOG.info("calling POST /credential with object: {}", cloud);
		final String credentialString = genCredentialYml(cloud);
		final InputStream is = new ByteArrayInputStream(credentialString.getBytes());
		LOG.info("{}: Deploying payload.", ws.getId());
		final String filename = "mycreds.yaml";
		ws.pushPayload(is, filename);
		final ProcessResult res = ws.addCredential(cloud.getName(), filename);
		if(res.getExitCode()==1){
			throw new JujuException(res.getErrout());
		}
		return ResponseEntity.ok(res.getStdout());
	}

	@GetMapping(value = "/credential", produces = "application/json")
	public ResponseEntity<String> credentials() {
		LOG.info("calling GET /credential");
		final ProcessResult res = ws.credentials();
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		return ResponseEntity.ok(res.getStdout());
	}

	@GetMapping(value = "/credential/{cloudname}/{name}", produces = "application/json")
	public ResponseEntity<String> credentialDetails(@PathVariable("cloudname") @NotNull final String cloudname, @PathVariable("name") final String name) {
		LOG.info("calling GET /credentialDetails with cloudname:{} and name=:{}", cloudname, name);
		final ProcessResult res = ws.credentialDetail(cloudname, name);
		final ProcessResult res2 = ws.cloudDetail(cloudname);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		if(res2.getExitCode()==1){
			throw new JujuException(res2.getErrout());
		}
		else if (res2.getExitCode()==0 && res.getStdout().isEmpty()) {
			throw new JujuException(res.getErrout());
		}
		else{
			return ResponseEntity.ok(res.getStdout());
		}
	}

	@PutMapping(value = "/credential", produces = "application/json")
	public ResponseEntity<String> updateCredential(@RequestBody @NotNull final JujuCloud cloud) {
		LOG.info("calling PUT /credential with object: {}", cloud);
		final String credentialString = genCredentialYml(cloud);
		final InputStream is = new ByteArrayInputStream(credentialString.getBytes());
		LOG.info("{}: Deploying payload.", ws.getId());
		final String filename = "mycreds.yaml";
		ws.pushPayload(is, filename);
		final ProcessResult res = ws.updateCredential(cloud.getName(), filename);
		if(res.getExitCode()==1){
			throw new JujuException(res.getErrout());
		}
		return ResponseEntity.ok(res.getErrout());
	}

	@DeleteMapping(value = "/credential/{cloudname}/{name}", produces = "application/json")
	public ResponseEntity<String> removeCredential(@PathVariable("cloudname") @NotNull final String cloudname, @PathVariable("name") final String name) {
		LOG.info("calling DELETE /credential with cloudname:{} and name=:{}", cloudname, name);
		final ProcessResult res1 = ws.cloudDetail(cloudname);
		final ProcessResult res2 = ws.credentialDetail(cloudname, name);
		if(res1.getExitCode()==1){
			throw new JujuException(res1.getErrout());
		}
		else if (res2.getStdout().isEmpty()) {
			throw new JujuException(res2.getErrout());
		}
		else{
			final ProcessResult res3 = ws.removeCredential(cloudname, name);
			LOG.info(res3.getStdout());
			LOG.error(res3.getErrout());
			return ResponseEntity.ok(res3.getErrout());
		}
	}

	public ResponseEntity<String> addMetadata(@RequestParam("path") @NotNull final String path,
											  @RequestParam("imageId") @NotNull final String imageId, @RequestParam("osSeries") @NotNull final String osSeries,
											  @RequestParam("region") @NotNull final String region, @RequestParam("osAuthUrl") @NotNull final String osAuthUrl) {
		LOG.info("calling POST /metadata");
		final ProcessResult res = ws.genMetadata(path, imageId, osSeries, region, osAuthUrl);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		return ResponseEntity.ok(res.getStdout());
	}

	@PostMapping(value = "/metadata", produces = "application/json")
	public ResponseEntity<String> genMetadata(@RequestBody @NotNull final JujuMetadata meta) {
		LOG.info("calling POST /metadata with object: {}", meta);
		final ProcessResult res = ws.genMetadata(meta.getPath(), meta.getImageId(), meta.getOsSeries(), meta.getRegionName(), meta.getOsAuthUrl());
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		return ResponseEntity.ok(res.getStdout());
	}

	public ResponseEntity<String> addController2(@RequestParam("imageId") @NotNull final String imageId,
												 @RequestParam("osSeries") @NotNull final String osSeries, @RequestParam("constraints") @NotNull final String constraints,
												 @RequestParam("cloudname") @NotNull final String cloudname, @RequestParam("controllername") @NotNull final String controllername,
												 @RequestParam("region") @NotNull final String region, @RequestParam("networkId") @NotNull final String networkId) {
		LOG.info("post /controller");
		final ProcessResult res = ws.addController(imageId, osSeries, constraints, cloudname, controllername, region, networkId);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		return ResponseEntity.ok(res.getStdout());
	}

	@PostMapping(value = "/controller/{cloudname}", produces = "application/json")
	public ResponseEntity<String> addController(@PathVariable("cloudname") @NotNull final String cloudname, @RequestBody @NotNull final JujuMetadata controller) {
		LOG.info("post /controller");
		final ProcessResult res = ws.addController(cloudname, controller);
		LOG.info("Proceess result"+res);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		return ResponseEntity.ok(res.getStdout());
	}

	@GetMapping(value = "/controller", produces = "application/json")
	public ResponseEntity<String> controllers() {
		LOG.info("get /controllers");
		final ProcessResult res = ws.controllers();
		LOG.info("Proceess result"+res);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		return ResponseEntity.ok(res.getStdout());
	}

	@GetMapping(value = "/controller/{controllername}", produces = "application/json")
	public ResponseEntity<String> controllerDetail(@PathVariable("controllername") @NotNull final String controllername) {
		LOG.info("get /showController/{}", controllername);
		final ProcessResult res = ws.showController(controllername);
		LOG.info("Proceess result"+res);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		if(res.getExitCode()==0) {
		return ResponseEntity.ok(res.getStdout());
		}
		else {
			throw new JujuException(res.getErrout()) ;
		}
	}

	@DeleteMapping(value = "/controller/{controllername}", produces = "application/json")
	public ResponseEntity<String> removeController(@PathVariable("controllername") @NotNull final String controllername) {
		LOG.info("delete /remove-controller/{}", controllername);
		final ProcessResult res = ws.removeController(controllername);
		LOG.info("Proceess result1 "+res);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		if(res.getExitCode()==0) {		
			return ResponseEntity.ok(res.getErrout());
			}
			else {
				throw new JujuException(res.getErrout()) ;
			}
	}

	@PostMapping(value = "/model/{name}", produces = "application/json")
	public ResponseEntity<String> addModel(@PathVariable("name") @NotNull final String name) {
		LOG.info("post /add-model");
		final ProcessResult res = ws.addModel(name);
		LOG.info("Proceess result1 "+res);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		if(res.getExitCode()==0) {
		return ResponseEntity.ok(res.getErrout());
		}
		else {
			throw new JujuException(res.getErrout()) ;
		}
	}

	@GetMapping(value = "/model", produces = "application/json")
	public ResponseEntity<String> model() {
		LOG.info("get /models");
		final ProcessResult res = ws.model();
		LOG.info("Proceess result1 "+res);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		return ResponseEntity.ok(res.getStdout());
	}

	@DeleteMapping(value = "/model/{name}", produces = "application/json")
	public ResponseEntity<String> removeModel(@PathVariable("name") @NotNull final String name) {
		LOG.info("delete /destroy-model/{}", name);
		final ProcessResult res = ws.removeModel(name);
		LOG.info("Proceess result1 "+res);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		if(res.getExitCode()==0) {
			return ResponseEntity.ok(res.getErrout());
			}
			else {
				throw new JujuException(res.getErrout()) ;
			}
	}

	@PostMapping(value = "/application/{charm}/{name}", produces = "application/json")
	public ResponseEntity<String> deployApp(@PathVariable("charm") @NotNull final String charm, @PathVariable("name") @NotNull final String name) {
		LOG.info("post /deploy/{}/{}", charm, name);
		final ProcessResult res = ws.deployApp(charm, name);
		LOG.info("Proceess result1 "+res);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		return ResponseEntity.ok(res.getStdout());
	}

	@GetMapping(value = "/application/{name}", produces = "application/json")
	public ResponseEntity<String> application(@PathVariable("name") @NotNull final String name) {
		LOG.info("calling /show-application/{}", name);
		final ProcessResult res = ws.application(name);
		LOG.info("Proceess result1 "+res);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		if(res.getExitCode()==0) {
			return ResponseEntity.ok(res.getStdout());
			}
			else {
				throw new JujuException(res.getErrout()) ;
			}
	}

	@DeleteMapping(value = "/application/{name}", produces = "application/json")
	public ResponseEntity<String> removeApplication(@PathVariable("name") @NotNull final String name) {
		LOG.info("calling /remove-application/{}", name);
		final ProcessResult res = ws.removeApplication(name);
		LOG.info("Proceess result1 "+res);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		if(res.getExitCode()==0) {
			return ResponseEntity.ok(res.getStdout());
			}
			else {
				throw new JujuException(res.getErrout()) ;
			}
	}

	@GetMapping(value = "/status", produces = "application/json")
	public ResponseEntity<String> status() {
		LOG.info("calling /status");
		final ProcessResult res = ws.status();
		LOG.info("Proceess result1 "+res);
		LOG.info(res.getStdout());
		LOG.error(res.getErrout());
		return ResponseEntity.ok(res.getStdout());
	}

	private String genCloudYml(final JujuCloud cloud) {
		/*
		 * clouds: openstack-cloud-240: type: openstack auth-types: [userpass] regions:
		 * RegionOne: endpoint: http://10.31.1.240:5000/v3
		 */
		final StringBuilder str = new StringBuilder("clouds:\n");
		str.append("    " + cloud.getName() + ":\n");
		str.append("      type: openstack\n");
		str.append("      auth-types: [userpass]\n");
		str.append("      regions:\n");
		if ((cloud.getRegions() != null) && !cloud.getRegions().isEmpty()) {
			for (final JujuRegion region : cloud.getRegions()) {
				str.append("        " + region.getName() + ":\n");
				str.append("          endpoint: " + region.getEndPoint() + "\n");
			}
		}
		LOG.info("CloudYaml:\n{}", str.toString());
		return str.toString();
	}

	private String genCredentialYml(final JujuCloud cloud) {
		/*
		 * credentials: openstack-cloud-240: # .240 Openstack instance admin: auth-type:
		 * userpass password: 13f83cb78a4f4213 tenant-name: admin username: admin
		 *
		 */
		final JujuCredential credential = cloud.getCredential();
		final StringBuilder str = new StringBuilder("credentials:\n");
		str.append("  " + cloud.getName() + ":\n");
		str.append("    " + credential.getName() + ":\n");
		str.append("      auth-type: " + credential.getAuthType() + "\n");
		str.append("      password: " + credential.getPassword() + "\n");
		str.append("      tenant-name: " + credential.getTenantName() + "\n");
		str.append("      username: " + credential.getUsername() + "\n");
		LOG.info("CredentialYaml:\n{}", str.toString());
		return str.toString();
	}
}