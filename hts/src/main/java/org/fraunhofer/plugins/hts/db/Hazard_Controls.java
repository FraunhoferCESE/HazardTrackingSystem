package org.fraunhofer.plugins.hts.db;


import java.util.Date;

import net.java.ao.Entity;
import net.java.ao.ManyToMany;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

/**
 * The table definition for the Hazard_Controls table. It extends Entity which
 * provides the ID field and getID method.
 * 
 * @author ASkulason
 * 
 */
@Table("Hazard_Controls")
public interface Hazard_Controls extends Entity {
	
	@NotNull
	@StringLength(value = StringLength.UNLIMITED)
	String getDescription();

	void setDescription(String description);
	
	ControlGroups getControlGroup();
	
	void setControlGroup(ControlGroups group);
	
	Date getOriginalDate();

	void setOriginalDate(Date originalDate);
	
	@ManyToMany(value = ControlToCause.class)
	Hazard_Causes[] getCauses();
	
	@ManyToMany(value = ControlToHazard.class)
	Hazards[] getHazard();
}
