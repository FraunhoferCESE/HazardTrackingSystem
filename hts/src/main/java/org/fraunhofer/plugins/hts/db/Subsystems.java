package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.OneToOne;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

/**
 * The table definition for the Subsystems table.
 * It extends Entity which provides the ID field and getID method.
 * @author ASkulason
 *
 */

//TODO Connect to hazard for and create a service.
@Preload
@Table("Subsystems")
public interface Subsystems extends Entity {
	String getLabel();
	void setLabel(String label);

	String getDescription();
	void setDescription(String description);
	
	//TODO maybe fix the relation to the database(oneToMany)
	@OneToOne
	Hazards getHazards();
}
