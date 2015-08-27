package org.fraunhofer.plugins.hts.model;


import java.util.Date;

import net.java.ao.Entity;
import net.java.ao.Implementation;
import net.java.ao.ManyToMany;
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
@Implementation(Hazard_ControlsImpl.class)
public interface Hazard_Controls extends Entity {
	
	@StringLength(value = StringLength.UNLIMITED)
	String getDescription();

	void setDescription(String description);
	
	ControlGroups getControlGroup();
	
	void setControlGroup(ControlGroups group);
	
	Date getOriginalDate();

	void setOriginalDate(Date originalDate);
	
	Date getLastUpdated();

	void setLastUpdated(Date lastEdit);
	
	@StringLength(value = StringLength.UNLIMITED)
	String getDeleteReason();

	void setDeleteReason(String reason);
	
	int getControlNumber();

	void setControlNumber(int controlNumber);
	
	int getTransfer();
	
	void setTransfer(int transferID);
	
	@ManyToMany(value = ControlToCause.class)
	Hazard_Causes[] getCauses();
	
	@ManyToMany(value = ControlToHazard.class)
	Hazards[] getHazard();
	
	@ManyToMany(value = VerifcToControl.class)
	Verifications[] getVerifications();
}
