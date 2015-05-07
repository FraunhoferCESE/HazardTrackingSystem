package org.fraunhofer.plugins.hts.model;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table("GroupToHazard")
public interface GroupToHazard extends Entity {
	void setHazard(Hazards hazard);

	Hazards getHazard();

	void setHazardGroup(Hazard_Group hazardGroup);

	Hazard_Group getHazardGroup();
}
