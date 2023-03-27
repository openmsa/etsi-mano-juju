package com.ubiqube.juju.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import com.ubiqube.juju.JujuException;

import io.micrometer.common.util.StringUtils;

public class WorkspaceService implements AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(WorkspaceService.class);

	private static final String WORKSPACE_ROOT = "/tmp/workspace";
	private File wsRoot;

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
		final List<String> list = new ArrayList<>();
		list.addAll(List.of("juju", "add-cloud", "--local", "openstack-cloud", filename));
//		list.addAll(List.of("cat", filename)); // for local testing
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public String clouds() {
//		Command: juju clouds --local
		final ProcessBuilder builder = new ProcessBuilder(List.of("juju", "clouds", "--local", "--format=json"));
//		final ProcessBuilder builder = new ProcessBuilder(List.of("ls", "-alrt")); //for local testing
		builder.directory(wsRoot);
		final ProcessResult res = run(builder);
		return res.getStdout();
	}

	public String removeCloud(final String cloudname) {
//		Command: juju remove-cloud --local <openstack-name> 
		final ProcessBuilder builder = new ProcessBuilder(List.of("juju", "remove-cloud", "--local", cloudname));
		builder.directory(wsRoot);
		final ProcessResult res = run(builder);
		return res.getStdout();
	}
	
	public ProcessResult addCredential(final String cloudname, final String filename) {
//		Command: juju add-credential <cloudname> -f <cred-file>
		final List<String> list = new ArrayList<>();
		list.addAll(List.of("juju", "add-credential", cloudname, "-f", filename));
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public String credentials() {
//		Command: juju credentials --local
		final ProcessBuilder builder = new ProcessBuilder(List.of("juju", "credentials", "--local", "--format=json"));
		builder.directory(wsRoot);
		final ProcessResult res = run(builder);
		return res.getStdout();
	}

	public ProcessResult updateCredential(final String cloudname, final String filename) {
//		Command: juju update-credential <cloudname> -f <cred-file> 
		final List<String> list = new ArrayList<>();
		list.addAll(List.of("juju", "update-credential", cloudname, "-f", filename));
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public String removeCredential(final String cloudname, final String username) {
//		Command: juju remove-credential <cloudname> <username>
		final ProcessBuilder builder = new ProcessBuilder(List.of("juju", "remove-credential", cloudname, username));
		builder.directory(wsRoot);
		final ProcessResult res = run(builder);
		return res.getStdout();
	}

	public String genMetadata(String path, String imageId, String osSeries, String region, String osAuthUrl) {
//		Command: juju metadata generate-image -d ~/simplestreams -i <IMAGE_ID> -s <OS_SERIES> -r <REGION> -u <OS_AUTH_URL>
		final ProcessBuilder builder = new ProcessBuilder(List.of("juju", "metadata", "generate-image", "-d", path, "-i", imageId, "-s", osSeries, "-r", region, "-u", osAuthUrl));
		builder.directory(wsRoot);
		final ProcessResult res = run(builder);
		return res.getStdout();
	}

	public String addController(String imageId, String osSeries, String constraints, String cloudname, String controllername, String region) {
//		Command: juju bootstrap --bootstrap-image=0bc65ba0-6f27-4128-b596-79e6788e8574 --bootstrap-series=jammy --bootstrap-constraints="arch=amd64" openstack-inari-108 openstack-inari-108-controller --model-default network=82dbcdf4-82d3-4e95-9244-550673250dad --debug --verbose
		final List<String> list = new ArrayList<>();
		if (StringUtils.isNotBlank(imageId))
			list.add("--bootstrap-image="+imageId);
		if (StringUtils.isNotBlank(osSeries))
			list.add("--bootstrap-series="+osSeries);
		if (StringUtils.isNotBlank(constraints))
			list.add("--bootstrap-constraints=\""+constraints+"\"");
		if (StringUtils.isNotBlank(cloudname)) {
			list.add(cloudname);
			if (StringUtils.isNotBlank(region))
				list.add("/"+region);
		}
		if (StringUtils.isNotBlank(controllername))
			list.add(controllername);
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		final ProcessResult res = run(builder);
		return res.getStdout();
	}

	public String controllers() {
//		Command: juju controllers --format=json
		final ProcessBuilder builder = new ProcessBuilder(List.of("juju", "controllers", "--format=json"));
		builder.directory(wsRoot);
		final ProcessResult res = run(builder);
		return res.getStdout();
	}

	public String removeController(final String cloudname, final String username) {
//		Command: juju destroy-controller openstack-inari-108-controller --destroy-all-models
		final ProcessBuilder builder = new ProcessBuilder(List.of("juju", "destroy-controller", cloudname, "--destroy-all-models"));
		builder.directory(wsRoot);
		final ProcessResult res = run(builder);
		return res.getStdout();
	}

	public void pushPayload(final InputStream is, final String filename) {
		final File file = new File(wsRoot, filename);
		try (OutputStream fos = new FileOutputStream(file)) {
			is.transferTo(fos);
		} catch (final IOException e) {
			throw new JujuException(e);
		}
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
}
