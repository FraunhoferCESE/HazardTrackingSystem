package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table("VerifcToControl")
public interface VerifcToControl extends Entity {
	void setVerification(Verifications verification);
	Verifications getVerifications();
	
	void setControl(Hazard_Controls control);
	Hazard_Controls getControl();
}