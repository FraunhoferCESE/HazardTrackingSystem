package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table("Causes_to_Hazards")
public interface Causes_to_Hazards extends Entity {
	void setHazard(Hazards hazard);

	Hazards getHazard();

	void setHazardCause(Hazard_Causes hazardCause);

	Hazard_Causes getHazardCause();
}
