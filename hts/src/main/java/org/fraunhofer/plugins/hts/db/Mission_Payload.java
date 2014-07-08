package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.Table;
import net.java.ao.schema.Unique;

@Table("Mission_Payload")
public interface Mission_Payload extends Entity {
	@Unique
	String getName();

	void setName(String name);

	@OneToMany
	Hazards[] getHazards();
}
