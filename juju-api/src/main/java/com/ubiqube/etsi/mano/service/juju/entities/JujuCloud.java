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

import org.springframework.lang.Nullable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "juju_clouds")
public class JujuCloud implements Serializable {
	private static final long serialVersionUID = 1L;

	public JujuCloud(final String name, final String type, final String authTypes, final List<JujuRegion> regions, final JujuCredential credential,
			final JujuMetadata metadata) {
		this.name = name;
		this.type = type;
		this.authTypes = authTypes;
		this.regions = regions;
		this.credential = credential;
		this.metadata = metadata;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Nullable
	@Column(name = "name")
	private String name;

	@Column(name = "type")
	private String type;

	@Column(name = "authTypes")
	private String authTypes;

	@Column(name = "status")
	private String status;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "region_id")
	private List<JujuRegion> regions;

	@ManyToOne
	@JoinColumn(name = "credentail_id")
	private JujuCredential credential;

	@ManyToOne
	@JoinColumn(name = "metadata_id")
	private JujuMetadata metadata;
}