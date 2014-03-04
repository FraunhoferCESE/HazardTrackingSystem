package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.OneToOne;

public interface Review_Phases extends Entity {

	String getLabel();
	void setLabel(String label);

	String getDescription();
	void setDescription(String description);
	
	//TODO maybe fix the relation to the database(oneToMany)
	@OneToOne
	Hazards getHazards();
}
