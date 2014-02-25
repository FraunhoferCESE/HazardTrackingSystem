package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

@Preload
@Table("Verifications")
public interface Verifications extends Entity {
	String getTitle();
	void setTitle(String title);
	
	String getVerificationDesc();
	void setVerificationDesc(String description);
	
	
}
