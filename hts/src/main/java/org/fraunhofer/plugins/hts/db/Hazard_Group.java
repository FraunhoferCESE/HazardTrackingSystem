package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.OneToOne;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

@Preload
@Table("Hazard_Group")
public interface Hazard_Group extends Entity{

	String getLabel();
	void setLabel(String label);
	
	@OneToOne
	Hazards getHazards();
}
