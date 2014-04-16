package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.OneToOne;
import net.java.ao.schema.Table;

@Table("Mission_Payload")
public interface Mission_Payload extends Entity {
	String getName();

	void setName(String name);
	
	@OneToOne
	Hazards getHazards();
}
