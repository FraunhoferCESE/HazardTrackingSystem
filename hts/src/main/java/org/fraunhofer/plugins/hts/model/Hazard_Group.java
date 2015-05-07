package org.fraunhofer.plugins.hts.model;

import net.java.ao.Entity;
import net.java.ao.ManyToMany;
import net.java.ao.schema.Table;

/**
 * The table definition for the Hazard_Group table. It extends Entity which
 * provides the ID field and getID method.
 * 
 * @author ASkulason
 * 
 */
@Table("Hazard_Group")
public interface Hazard_Group extends Entity {

	String getLabel();

	void setLabel(String label);

	@ManyToMany(value = GroupToHazard.class)
	Hazards[] getHazards();
}
