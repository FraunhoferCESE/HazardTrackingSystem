package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.OneToOne;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

/**
 * The table definition for the Risk_Categories table.
 * It extends Entity which provides the ID field and getID method.
 * @author ASkulason
 *
 */
@Preload
@Table("Risk_Categories")
public interface Risk_Categories extends Entity {
	
	String getValue();
	void setValue(String value);
	
	//TODO see if needed
	String getRiskDesc();
	void setRiskDesc(String riskDesc);
	
	@OneToOne
	Hazards getHazards();
}
