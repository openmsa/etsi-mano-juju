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

	public ProcessResult addCloud(String filename) {
//		Command: juju add-cloud --local openstack-cloud openstack-cloud.yaml
		final List<String> list = new ArrayList<>();
		list.addAll(List.of("juju", "add-cloud", "--local", "openstack-cloud", filename));
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public String clouds() {
//		Command: juju clouds --local
		final ProcessBuilder builder = new ProcessBuilder(List.of("juju", "clouds", "--local", "--output json"));
		builder.directory(wsRoot);
		final ProcessResult res = run(builder);
		return res.getStdout();
	}

	public String removeCloud(final String name) {
//		Command: juju remove-cloud --local <openstack-name> 
		final ProcessBuilder builder = new ProcessBuilder(List.of("juju", "remove-cloud", "--local", name));
		builder.directory(wsRoot);
		final ProcessResult res = run(builder);
		return res.getStdout();
	}
	
	public ProcessResult addCredential(String filename) {
//		Command: juju add-credential openstack-cloud -f <cred-file>
		final List<String> list = new ArrayList<>();
		list.addAll(List.of("juju", "add-credential", "openstack-cloud", "-f", filename));
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public String credentials() {
//		Command: juju credentials --local
		final ProcessBuilder builder = new ProcessBuilder(List.of("juju", "credentials", "--local", "--output json"));
		builder.directory(wsRoot);
		final ProcessResult res = run(builder);
		return res.getStdout();
	}

	public ProcessResult updateCredential(String filename) {
//		Command: juju update-credential openstack -f <cred-file> 
		final List<String> list = new ArrayList<>();
		list.addAll(List.of("juju", "update-credential", "openstack", "-f", filename));
		final ProcessBuilder builder = new ProcessBuilder(list);
		builder.directory(wsRoot);
		return run(builder);
	}

	public String removeCredential(final String username) {
//		Command: juju remove-credential openstack <username>
		final ProcessBuilder builder = new ProcessBuilder(List.of("juju", "remove-credential", "openstack", username));
		builder.directory(wsRoot);
		final ProcessResult res = run(builder);
		return res.getStdout();
	}

	public String metadataGenImg() {
//		Command: juju metadata generate-image -d ~/simplestreams -i <IMAGE_ID> -s <OS_SERIES> -r <REGION> -u <OS_AUTH_URL>
		final ProcessBuilder builder = new ProcessBuilder(List.of("juju", "metadata", "generate-image", "-d ~/simplestreams -i <IMAGE_ID> -s <OS_SERIES> -r <REGION> -u <OS_AUTH_URL>"));
		builder.directory(wsRoot);
		final ProcessResult res = run(builder);
		return res.getStdout();
	}

	public void pushPayload(final InputStream is, String filename) {
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
