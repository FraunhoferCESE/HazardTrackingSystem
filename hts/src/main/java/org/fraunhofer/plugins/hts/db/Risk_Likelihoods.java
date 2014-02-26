package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.OneToOne;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

@Preload
@Table("Risk_Likelihoods")
public interface Risk_Likelihoods extends Entity {
	
	String getValue();
	void setValue(String value);
	
	String getLikelihoodDesc();
	void setLikelihoodDesc(String likeliHoodDesc);
	
	@OneToOne
	Hazards getHazards();
}
