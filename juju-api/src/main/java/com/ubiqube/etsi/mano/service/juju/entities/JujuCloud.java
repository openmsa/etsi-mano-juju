
package com.ubiqube.etsi.mano.service.juju.entities;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

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

	public JujuCloud(String name, String type, String authTypes, List<JujuRegion> regions, JujuCredential credential,
			JujuMetadata metadata) {
		super();
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
