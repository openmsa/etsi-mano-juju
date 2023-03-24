package com.ubiqube.juju.dto;

import java.io.File;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Workspace {

	private File root;
}
