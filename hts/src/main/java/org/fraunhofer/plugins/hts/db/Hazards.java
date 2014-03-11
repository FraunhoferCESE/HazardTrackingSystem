package org.fraunhofer.plugins.hts.db;

import java.util.Date;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.OneToOne;
import net.java.ao.Preload;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;
import net.java.ao.schema.Unique;
import net.java.ao.schema.NotNull;

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
	
	@StringLength(value=StringLength.UNLIMITED)
	String getHazardDesc();
	void setHazardDesc(String description);
	
	String getPreparer();
	void setPreparer(String preparer);
	
	String getEmail();
	void setEmail(String email);
	
	@NotNull
	@Unique
	String getHazardNum();
	void setHazardNum(String number);
	
	String getInitiationDate();
	void setInitiationDate(Date initationDate);
	
	String getCompletionDate();
	void setCompletionDate(Date completionDate);
		
	Date getRevisionDate();
	void setRevisionDate(Date revisionDate);
	
	//TODO FOREIGN KEYS
		
	void setHazardGroup(Hazard_Group group);
	Hazard_Group getHazardGroup();
	
	void setRiskCategory(Risk_Categories risk);
	Risk_Categories getRiskCategory();
	
	void setRiskLikelihood(Risk_Likelihoods likelihood);
	Risk_Likelihoods getRiskLikelihood();
	
	void setReviewPhase(Review_Phases phase);
	Review_Phases getReviewPhase();
	
	@OneToMany
	Subsystems[] getSubsystems();
	
	@OneToOne
	Mission_Payload getMissionPayload();

}

