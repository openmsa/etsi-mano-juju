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
package com.ubiqube.etsi.mano.service.juju.entities;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "juju_metadata")
public class JujuMetadata implements Serializable {
	private static final long serialVersionUID = 1L;

	public JujuMetadata(String name, String imageId, String path, String osSeries, String osAuthUrl,
						List<String> constraints, String networkId, String regionName, List<JujuModel> models) {
		super();
		this.name = name;
		this.imageId = imageId;
		this.path = path;
		this.osSeries = osSeries;
		this.osAuthUrl = osAuthUrl;
		this.constraints = constraints;
		this.networkId = networkId;
		this.regionName = regionName;
		this.models = models;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	private String name;
	private String imageId;
	private String path;
	private String osSeries;
	private String osAuthUrl;
	private List<String> constraints;
	private String networkId;
	private String regionName;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "models_id", referencedColumnName = "id")
	private List<JujuModel> models;
}