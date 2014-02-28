package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.OneToOne;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

/**
 * The table definition for the Risk_Likelihoods table.
 * It extends Entity which provides the ID field and getID method.
 * @author ASkulason
 *
 */
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
