package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
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
public interface Hazard_Controls extends Entity {
	String getTitle();
	void setTitle(String title);

	@StringLength(value = StringLength.UNLIMITED)
	String getDescription();
	void setDescription(String description);
}
