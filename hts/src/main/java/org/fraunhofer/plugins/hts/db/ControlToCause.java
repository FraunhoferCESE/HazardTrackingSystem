package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table("ControlToCause")
public interface ControlToCause extends Entity {
	void setCause(Hazard_Causes cause);
	Hazard_Causes getCause();
	
	void setControl(Hazard_Controls control);
	Hazard_Controls getControl();
}
