package org.fraunhofer.plugins.hts.model;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table("VerifcToHazard")
public interface VerifcToHazard extends Entity {
	void setHazard(Hazards hazard);
	Hazards getHazard();
	
	void setVerification(Verifications verification);
	Verifications getVerification();
}