package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.schema.Unique;
import net.java.ao.schema.NotNull;
import net.java.ao.Preload;

@Preload
public interface Hazard_Controls extends Entity {
	@NotNull
	@Unique
	String getTitle();
	void setTitle(String title);
	
	String getDescription();
	void setDescription(String description);
	
}
