package org.fraunhofer.plugins.hts.db;

import java.util.Date;

import net.java.ao.Entity;
import net.java.ao.ManyToMany;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;
import net.java.ao.schema.NotNull;

//TODO look into if skipping preload is better writing javadoc
/**
 * The table definition for the Hazards table. It extends Entity which provides
 * the ID field and getID method.
 * 
 * @author ASkulason
 * 
 */
@Table("Hazards")
public interface Hazards extends Entity {
	
	@NotNull
	@StringLength(value = 512)
	String getTitle();

	void setTitle(String title);
	
	@StringLength(value = StringLength.UNLIMITED)
	String getHazardSafetyRequirements();

	void setHazardSafetyRequirements(String safetyRequirements);

	@StringLength(value = StringLength.UNLIMITED)
	String getHazardDesc();

	void setHazardDesc(String description);
	
	@StringLength(value = StringLength.UNLIMITED)
	String getHazardJustification();

	void setHazardJustification(String justification);
	
	@StringLength(value = StringLength.UNLIMITED)
	String getHazardOpenWork();

	void setHazardOpenWork(String openWork);

	String getPreparer();

	void setPreparer(String preparer);

	String getEmail();

	void setEmail(String email);
	
	boolean getActive();
	
	void setActive(boolean state);

	@NotNull
	String getHazardNum();

	void setHazardNum(String hazardNum);
	
	String getHazardVersionNum();

	void setHazardVersionNum(String versionNum);

	Date getInitiationDate();

	void setInitiationDate(Date initationDate);

	Date getCompletionDate();

	void setCompletionDate(Date completionDate);

	Date getRevisionDate();

	void setRevisionDate(Date revisionDate);

	// FOREIGN KEYS
	void setReviewPhase(Review_Phases phase);

	Review_Phases getReviewPhase();

	void setMissionPayload(Mission_Payload missionPayload);

	Mission_Payload getMissionPayload();

	@ManyToMany(value = SubsystemToHazard.class)
	Subsystems[] getSubsystems();

	@ManyToMany(value = GroupToHazard.class)
	Hazard_Group[] getHazardGroups();

	@ManyToMany(value = PhaseToHazard.class)
	Mission_Phase[] getMissionPhases();

	@ManyToMany(value = CausesToHazards.class)
	Hazard_Causes[] getHazardCauses();
	
	@ManyToMany(value = ControlToHazard.class)
	Hazard_Controls[] getHazardControls();
	
	@ManyToMany(value = VerifcToHazard.class)
	Verifications[] getVerifications();
}
