package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

/**
 * The table definition for the Risk_Categories table. It extends Entity which
 * provides the ID field and getID method.
 * 
 * @author ASkulason
 * 
 */
@Table("Risk_Categories")
public interface Risk_Categories extends Entity {

	String getValue();

	void setValue(String value);

	@StringLength(value = StringLength.UNLIMITED)
	String getRiskDesc();

	void setRiskDesc(String riskDesc);
}
