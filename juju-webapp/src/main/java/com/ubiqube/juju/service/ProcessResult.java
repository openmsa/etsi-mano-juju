package com.ubiqube.juju.service;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProcessResult {

	private int exitCode;

	private String stdout;

	private String errout;
}
