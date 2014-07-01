package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table("ControlToHazard")
public interface ControlToHazard extends Entity {
	void setHazard(Hazards hazard);
	Hazards getHazard();
	
	void setControl(Hazard_Controls control);
	Hazard_Controls getControl();
}
