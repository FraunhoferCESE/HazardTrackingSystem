package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

@Preload
@Table("Mission_Payload")
public interface Mission_Payload extends Entity{
	String getName();
	void setName(String name);

	Hazards getHazard();
	void setHazard(Hazards hazard);
}
