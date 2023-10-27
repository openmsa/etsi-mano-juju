package com.ubiqube.juju.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import com.ubiqube.etsi.mano.service.juju.entities.JujuMetadata;
import com.ubiqube.juju.JujuException;

import io.micrometer.common.util.StringUtils;

@Service
public class WorkspaceService implements AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(WorkspaceService.class);
	private static final String SWTH_JSON_FMT = "--format=json";

	private static final String WORKSPACE_ROOT = "/home/workspace";
	private File wsRoot;

//	private static final String WORKSPACE_ROOT2 = "/home/ubuntu/Workspace/playground";
//	private File wsRoot2 = new File();

	private UUID id;

	public WorkspaceService() {
		createWorkspace();
	}

	private void createWorkspace() {
		id = UUID.randomUUID();
		wsRoot = new File(WORKSPACE_ROOT, id.toString());
		wsRoot.mkdirs();
	}

	public UUID getId() {
		return id;
	}

	public ProcessResult addCloud(final String cloudname, final String filename) {
//		Command: juju add-cloud --local openstack-cloud openstack-cloud.yaml
		final List<String> list = List.of("juju", "add-cloud", "--client", cloudname, filename);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult clouds() {
//		Command: juju clouds --local
		final List<String> list = List.of("juju", "clouds");
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult cloudDetail(final String cloudname) {
//		Command: juju show-cloud <cloudname>
		final List<String> list = List.of("juju", "show-cloud", cloudname);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult removeCloud(final String cloudname) {
//		Command: juju remove-cloud --local <openstack-name>
		final List<String> list = List.of("juju", "remove-cloud", "--client", cloudname);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult addCredential(final String cloudname, final String filename) {
//		Command: juju add-credential --local <cloudname> -f <cred-file> i.e. juju add-credential --local openstack-inari-108 -f mycreds.yaml
		final List<String> list = List.of("juju", "add-credential", cloudname, "-f", filename, "--client");
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult credentials() {
//		Command: juju credentials --local
		final List<String> list = List.of("juju", "credentials");
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult credentialDetail(final String cloudname, final String name) {
//		Command: juju credentials --local
		final List<String> list = List.of("juju", "show-credential", cloudname, name);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult updateCredential(final String cloudname, final String filename) {
//		Command: juju update-credential <cloudname> -f <cred-file>
		final List<String> list = List.of("juju", "update-credential", cloudname, "-f", filename, "--client");
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult removeCredential(final String cloudname, final String name) {
//		Command: juju remove-credential <cloudname> <username>
		final List<String> list = List.of("juju", "remove-credential", cloudname, name, "--client");
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult genMetadata(final String path, final String imageId, final String osSeries, final String region, final String osAuthUrl) {
//		Command: juju metadata generate-image -d ~/simplestreams -i <IMAGE_ID> -s <OS_SERIES> -r <REGION> -u <OS_AUTH_URL>
//		mkdir -p ~/simplestreams/images
//		ie: juju metadata generate-image -d ~/simplestreams -i 0bc65ba0-6f27-4128-b596-79e6788e8574 -s jammy -r RegionOne -u  http://10.31.1.108:5000
		final List<String> list = List.of("juju", "metadata", "generate-image", "-d", path, "-i", imageId, "-s", osSeries, "-r", region, "-u", osAuthUrl);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult addController(final String cloudname, final JujuMetadata c) {
//		Command: juju bootstrap --bootstrap-image=0bc65ba0-6f27-4128-b596-79e6788e8574 --bootstrap-series=jammy --bootstrap-constraints="arch=amd64" openstack-inari-108 openstack-inari-108-controller --model-default network=82dbcdf4-82d3-4e95-9244-550673250dad --debug --verbose
		final List<String> list = new ArrayList<>();
		list.add("juju");
		list.add("bootstrap");	
		if (StringUtils.isNotBlank(c.getImageId())) {
			list.add("--bootstrap-image=" + c.getImageId());
		}
		if (StringUtils.isNotBlank(c.getOsSeries())) {
			list.add("--bootstrap-series=" + c.getOsSeries());
		}
		if (c.getConstraints() != null) {
			final StringBuilder sb = new StringBuilder();
			for (final String constraint : c.getConstraints()) {
				sb.append(constraint + " ");
			}
			list.add("--bootstrap-constraints="+sb.toString().trim());
		}
		list.add(cloudname);
		list.add(c.getName());
		list.add("--model-default");
		if (StringUtils.isNotBlank(c.getNetworkId())) {
			list.add("network=" + c.getNetworkId());
		}
		LOG.info("{}", list);		
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(new File(WORKSPACE_ROOT));
		return run(builder);
	}

	public ProcessResult addController(final String imageId, final String osSeries, final String constraints, final String cloudname, final String controllername, final String region, final String networkId) {
//		Command: juju bootstrap --bootstrap-image=0bc65ba0-6f27-4128-b596-79e6788e8574 --bootstrap-series=jammy --bootstrap-constraints="arch=amd64" openstack-inari-108 openstack-inari-108-controller --model-default network=82dbcdf4-82d3-4e95-9244-550673250dad --debug --verbose
		final List<String> list = new ArrayList<>();
		list.add("juju bootstrap");
		if (StringUtils.isNotBlank(imageId)) {
			list.add("--bootstrap-image=" + imageId);
		}
		if (StringUtils.isNotBlank(osSeries)) {
			list.add("--bootstrap-series=" + osSeries);
		}
		if (StringUtils.isNotBlank(constraints)) {
			list.add("--bootstrap-constraints=\"" + constraints + "\"");
		}
		if (StringUtils.isNotBlank(cloudname)) {
			list.add(cloudname);
			if (StringUtils.isNotBlank(region)) {
				list.add("/" + region);
			}
		}
		if (StringUtils.isNotBlank(controllername)) {
			list.add(controllername);
		}
		list.add("--model-default");
		if (StringUtils.isNotBlank(networkId)) {
			list.add("network=" + networkId);
		}

		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult controllers() {
//		Command: juju controllers --format=json
		final List<String> list = List.of("juju", "controllers", SWTH_JSON_FMT);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult showController(final String controllername) {
//		Command: juju show-controller [options] [<controller name> ...]
		final List<String> list = List.of("juju", "show-controller", controllername);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		return run(builder);
	}

	public ProcessResult removeController(final String controllername) {
//		Command: juju destroy-controller --destroy-all-models --force --no-prompt openstack-inari-108-controller 
		final List<String> list = List.of("juju", "destroy-controller", "--destroy-all-models", "--force", "--no-prompt", controllername);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		return run(builder);
	}

	public ProcessResult addModel(final String name) {
//		Command: juju add-model k8s-ubi-model-kt
		final List<String> list = List.of("juju", "add-model", name);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult model() {
//		Command: juju models
		final List<String> list = List.of("juju", "models", SWTH_JSON_FMT);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult showModel(final String modelname) {
//		Command: juju show-model [options] [<model name> ...]
		final List<String> list = List.of("juju", "show-model", modelname);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		return run(builder);
	}
	
	public ProcessResult removeModel(final String name) {
//		Command: juju destroy-model <model>
		final List<String> list = List.of("juju", "destroy-model", "--no-prompt",name);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult deployApp(final String charm, final String username) {
//		Command: juju deploy kubernetes-core ubi-k8s-cluster
		final List<String> list = List.of("juju", "deploy", charm, username);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult application(final String name) {
//		Command: juju show-application [options] <application name or alias>
		final List<String> list = List.of("juju", "show-application", SWTH_JSON_FMT, name);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult removeApplication(final String name) {
//		Command: juju remove-application <application-name>
		final List<String> list = List.of("juju", "remove-application","--force","--no-wait","--no-prompt", name);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public ProcessResult status() {
//		Command: juju status --format=json
		final List<String> list = List.of("juju", "status", SWTH_JSON_FMT);
		LOG.info("{}", list);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public void pushPayload(final InputStream is, final String filename) {
		final File file = new File(wsRoot, filename);
		try (OutputStream fos = new FileOutputStream(file)) {
			is.transferTo(fos);
		} catch (final IOException e) {
			throw new JujuException(e);
		}
	}

	public void pushPayload(final String path, final String filename) {
		final File file = new File(path, filename);
		final File desc = new File(wsRoot, filename);
		copyFile(file, desc);
	}

	private static ProcessResult run(final ProcessBuilder builder) {
		final ExecutorService tp = Executors.newFixedThreadPool(2);
		try {
			final Process process = builder.start();
			try (InputStream stdIn = process.getInputStream();
					InputStream stdErr = process.getErrorStream()) {
				final Future<String> out = tp.submit(new StreamGobbler(stdIn));
				final Future<String> err = tp.submit(new StreamGobbler(stdErr));
				final int exitCode = process.waitFor();
				tp.shutdown();
				return ProcessResult.builder()
						.exitCode(exitCode)
						.errout(err.get())
						.stdout(out.get())
						.build();
			}
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new JujuException(e);
		} catch (final IOException | ExecutionException e) {
			throw new JujuException(e);
		}
	}

	@Override
	public void close() {
		FileSystemUtils.deleteRecursively(wsRoot);
	}

	private static void copyFile(final File source, final File dest) {
		try (FileInputStream fis = new FileInputStream(source);
				FileOutputStream fos = new FileOutputStream(dest);
				FileChannel sourceChannel = fis.getChannel();
				FileChannel destChannel = fos.getChannel()) {
			destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
		} catch (final IOException e) {
			throw new JujuException(e);
		}
	}
}
