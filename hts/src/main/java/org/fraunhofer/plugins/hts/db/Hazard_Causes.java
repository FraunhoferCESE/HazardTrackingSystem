package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.ManyToMany;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

/**
 * The table definition for the Hazard_Cause table. It extends Entity which
 * provides the ID field and getID method.
 * 
 * @author ASkulason
 * 
 */
@Table("Hazard_Causes")
public interface Hazard_Causes extends Entity {
	String getCauseID();
	void setCauseID(String id);
	
	String getTitle();
	void setTitle(String title);

	@StringLength(value = StringLength.UNLIMITED)
	String getDescription();
	void setDescription(String description);
	
	String getOwner();
	void setOwner(String owner);
	
	String getEffects();
	void setEffects(String effects);
	
	@ManyToMany(value = Causes_to_Hazards.class)
	Hazards[] getHazards();
}
