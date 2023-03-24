package com.ubiqube.juju.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InstallMessage {
	@NotNull
	private String name;

}
