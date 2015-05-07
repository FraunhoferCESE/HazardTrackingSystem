package org.fraunhofer.plugins.hts.model;

import net.java.ao.Entity;
import net.java.ao.ManyToMany;
import net.java.ao.schema.Table;

@Table("Mission_Phase")
public interface Mission_Phase extends Entity {

	String getLabel();

	void setLabel(String label);

	@ManyToMany(value = GroupToHazard.class)
	Hazards[] getHazards();
}
