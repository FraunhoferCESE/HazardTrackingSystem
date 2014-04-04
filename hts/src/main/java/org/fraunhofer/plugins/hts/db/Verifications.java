package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

/**
 * The table definition for the Verifications table. It extends Entity which
 * provides the ID field and getID method.
 * 
 * @author ASkulason
 * 
 */
// TODO
@Table("Verifications")
public interface Verifications extends Entity {
	String getTitle();

	void setTitle(String title);

	@StringLength(value = StringLength.UNLIMITED)
	String getVerificationDesc();

	void setVerificationDesc(String description);

}
