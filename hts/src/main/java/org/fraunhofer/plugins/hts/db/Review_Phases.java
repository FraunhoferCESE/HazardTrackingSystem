package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.OneToOne;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

@Preload
@Table("Review_Phases")
public interface Review_Phases extends Entity {

	String getLabel();
	void setLabel(String label);

	String getDescription();
	void setDescription(String description);
	
	//TODO maybe fix the relation to the database(oneToMany)
	@OneToOne
	Hazards getHazards();
}
