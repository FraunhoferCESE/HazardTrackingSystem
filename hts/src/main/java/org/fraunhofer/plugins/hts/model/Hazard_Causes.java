package org.fraunhofer.plugins.hts.model;

import java.util.Date;

import net.java.ao.Entity;
import net.java.ao.Implementation;
import net.java.ao.ManyToMany;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;
import net.java.ao.schema.NotNull;

/**
 * The table definition for the Hazard_Cause table. It extends Entity which
 * provides the ID field and getID method.
 * 
 * @author ASkulason
 * 
 */
@Table("Hazard_Causes")
@Implementation(Hazard_CausesImpl.class)
public interface Hazard_Causes extends Entity {
	int getCauseNumber();

	void setCauseNumber(int causeNumber);

	@NotNull
	@StringLength(value = 512)
	String getTitle();

	void setTitle(String title);

	@StringLength(value = StringLength.UNLIMITED)
	String getDescription();

	void setDescription(String description);
	
	@ManyToMany(value = ControlToCause.class)
	public Hazard_Controls[] getControls();

	String getOwner();
	
	void setRiskCategory(Risk_Categories risk);

	Risk_Categories getRiskCategory();

	void setRiskLikelihood(Risk_Likelihoods likelihood);

	Risk_Likelihoods getRiskLikelihood();

	void setOwner(String owner);

	@StringLength(value = StringLength.UNLIMITED)
	String getEffects();

	void setEffects(String effects);
	
	@StringLength(value = StringLength.UNLIMITED)
	String getAdditionalSafetyFeatures();

	void setAdditionalSafetyFeatures(String safetyFeatures);

	Date getLastUpdated();

	void setLastUpdated(Date lastEdit);

	Date getOriginalDate();

	void setOriginalDate(Date originalDate);

	@StringLength(value = StringLength.UNLIMITED)
	String getDeleteReason();

	void setDeleteReason(String reason);

	@ManyToMany(value = CausesToHazards.class)
	Hazards[] getHazards();
	
	int getTransfer();
	
	void setTransfer(int transferID);
}


