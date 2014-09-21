package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Table("Transfers")
public interface Transfers extends Entity {
	@NotNull
	int getOriginID();

	void setOriginID(int originID);

	@NotNull
	String getOriginType();

	void setOriginType(String originType);

	@NotNull
	int getTargetID();

	void setTargetID(int targetID);

	@NotNull
	String getTargetType();

	void setTargetType(String targetType);
	
	@NotNull
	boolean getActive();

	void setActive(boolean active);
	
}
