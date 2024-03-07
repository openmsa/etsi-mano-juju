/**
 *     Copyright (C) 2019-2023 Ubiqube.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.ubiqube.etsi.mano.service.juju.cli;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import com.ubiqube.etsi.mano.service.juju.entities.JujuCloud;
import com.ubiqube.etsi.mano.service.juju.entities.JujuMetadata;

import jakarta.validation.constraints.NotNull;

@Service
@HttpExchange(url = "/juju", accept = "application/json", contentType = "application/json")
public interface JujuRemoteService {

	@GetExchange("/cloud")
	ResponseEntity<String> clouds();

	@PostExchange("/cloud")
	ResponseEntity<String> addCloud(@RequestBody final JujuCloud cloud);

	@GetExchange("/cloud/{cloudname}")
	ResponseEntity<String> cloudDetail(@PathVariable("cloudname") final String cloudname);


	@DeleteExchange("/cloud/{cloudname}")
	ResponseEntity<String> removeCloud(@PathVariable("cloudname") final String cloudname);

	@GetExchange("/credential")
	ResponseEntity<String> credentials();

	@PostExchange("/credential")
	ResponseEntity<String> addCredential(@RequestBody final JujuCloud cloud);

	@PutExchange("/credential")
	ResponseEntity<String> updateCredential(@RequestBody final JujuCloud cloud);

	@GetExchange("/credential/{cloudname}/{credname}")
	ResponseEntity<String> credentialDetails(@PathVariable("cloudname") final String cloudname, @PathVariable("credname") final String credname);


	@DeleteExchange("/credential/{cloudname}/{name}")
	ResponseEntity<String> removeCredential(@PathVariable("cloudname") final String cloudname, @PathVariable("name") final String name);

//	@PostExchange("/metadata")
//	public ResponseEntity<String> genMetadata(@RequestParam("path")  final String path,
//			@RequestParam("imageId")  final String imageId, @RequestParam("osSeries")  final String osSeries,
//			@RequestParam("region")  final String region, @RequestParam("osAuthUrl")  final String osAuthUrl);

	@PostExchange("/metadata")
	ResponseEntity<String> genMetadata(@RequestBody final JujuMetadata meta);

//	@PostExchange("/controller")
//	public ResponseEntity<String> addController(@RequestParam("imageId")  final String imageId,
//			@RequestParam("osSeries")  final String osSeries, @RequestParam("constraints")  final String constraints,
//			@RequestParam("cloudname")  final String cloudname, @RequestParam("controllername")  final String controllername,
//			@RequestParam("region")  final String region);

	@PostExchange("/controller/{cloudname}")
	ResponseEntity<String> addController(@PathVariable("cloudname") final String cloudname, @RequestBody final JujuMetadata controller);

	@GetExchange("/controller")
	ResponseEntity<String> controllers();

	@GetExchange("/controller/{controllername}")
	ResponseEntity<String> controllerDetail(@PathVariable("controllername") final String controllername);

	@DeleteExchange("/controller/{controllername}")
	ResponseEntity<String> removeController(@PathVariable("controllername") final String controllername);

	@PostExchange("/model/{name}")
	ResponseEntity<String> addModel(@PathVariable("name") final String name);

	@GetExchange("/model")
	ResponseEntity<String> model();

	@GetExchange("/model/{name}")
	ResponseEntity<String> modelDetail(@PathVariable("name") final String name);


	@DeleteExchange("/model/{name}")
	ResponseEntity<String> removeModel(@PathVariable("name") final String name);

	@PostExchange("/application/{charm}/{name}")
	ResponseEntity<String> deployApp(@PathVariable("charm") final String charm, @PathVariable("name") final String name);

	@GetExchange("/application/{name}")
	ResponseEntity<String> application(@PathVariable("name") final String name);

	@DeleteExchange("/application/{name}")
	ResponseEntity<String> removeApplication(@PathVariable("name") final String name);

	@GetExchange("/status")
	ResponseEntity<String> status();
	
	@GetExchange("/isk8sready")
	ResponseEntity<Boolean> isK8sReady();
	
    @PostExchange("/kubeconfig")
	ResponseEntity<String> addKubeConfig(@RequestBody @NotNull final String filename);
    
    @PostExchange(value = "/helminstall/{helmName}" , contentType= "multipart/form-data")
	ResponseEntity<String> helmInstall(@PathVariable("helmName") @NotNull final String helmName, @RequestParam("tgzfile") MultipartFile tgzfile);
    
    @PostExchange("/helminstall2/{helmName}")
	ResponseEntity<String> helmInstall2(@PathVariable("helmName") @NotNull final String helmName, @RequestBody @NotNull final String filename);
 
	@GetExchange("/helmlist")
	ResponseEntity<String> helmList();
	
	@DeleteExchange("/helmuninstall/{helmName}")
	ResponseEntity<String> helmUninstall(@PathVariable("helmName") @NotNull final String helmName);


    

}