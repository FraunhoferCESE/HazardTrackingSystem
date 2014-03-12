package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.OneToOne;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

/**
 * The table definition for the Hazard_Group table.
 * It extends Entity which provides the ID field and getID method.
 * @author ASkulason
 *
 */
@Preload
@Table("Hazard_Group")
public interface Hazard_Group extends Entity{

	String getLabel();
	void setLabel(String label);
	
	@OneToOne
	Hazards getHazards();
}
