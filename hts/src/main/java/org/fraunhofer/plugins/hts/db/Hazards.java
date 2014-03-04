package org.fraunhofer.plugins.hts.db;

import java.util.Date;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Table;
import net.java.ao.schema.Unique;
import net.java.ao.schema.NotNull;
import java.sql.Types;

//TODO look into if skipping preload is better writing javadoc
/**
 * The table definition for the Hazards table.
 * It extends Entity which provides the ID field and getID method.
 * @author ASkulason
 *
 */
@Preload
@Table("Hazards")
//TODO restrictions
public interface Hazards extends Entity {
	@NotNull
	String getTitle();
	void setTitle(String title);
	
	String getHazardDesc();
	void setHazardDesc(String description);
	
	String getPreparer();
	void setPreparer(String preparer);
	
	@NotNull
	@Unique
	String getHazardNum();
	void setHazardNum(String number);
	
	String getInitiationDate();
	void setInitiationDate(String created);
	
	String getCompletionDate();
	void setCompletionDate(String completed);
		
	Date getRevisionDate();
	void setRevisionDate(Date lastEdit);
	
	//TODO FOREIGN KEYS
		
	void setHazardGroup(Hazard_Group group);
	Hazard_Group getHazardGroup();
	
	void setRiskCategory(Risk_Categories risk);
	Risk_Categories getRiskCategory();
	
	void setRiskLikelihood(Risk_Likelihoods likelihood);
	Risk_Likelihoods getRiskLikelihood();
	
	void setReviewPhase(Review_Phases phase);
	Review_Phases getReviewPhase();

}

