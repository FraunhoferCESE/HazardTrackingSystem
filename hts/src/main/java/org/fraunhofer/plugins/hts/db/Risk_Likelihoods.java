package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

@Preload
@Table("Risk_Likelihoods")
public interface Risk_Likelihoods extends Entity {
	
	String getValue();
	void setValue(String value);
	
	//TODO see if needed
	String getRiskDesc();
	void setRiskDesc();
	
	Hazards getHazards();
	void setHazards(Hazards hazard);
}
