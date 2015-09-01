package org.fraunhofer.plugins.hts.model;

import java.util.Date;

import net.java.ao.Entity;
import net.java.ao.Implementation;
import net.java.ao.ManyToMany;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

/**
 * The table definition for the Verifications table. It extends Entity which
 * provides the ID field and getID method.
 * 
 * @author ASkulason
 * 
 */
@Table("Verifications")
@Implementation(VerificationsImpl.class)
public interface Verifications extends Entity {

	@NotNull
	@StringLength(value = StringLength.UNLIMITED)
	String getVerificationDesc();

	void setVerificationDesc(String description);
	
	VerificationType getVerificationType();
	
	void setVerificationType(VerificationType type);
	
	String getResponsibleParty();

	void setResponsibleParty(String responsibleParty);

	Date getEstCompletionDate();

	void setEstCompletionDate(Date estCompletionDate);
	
	VerificationStatus getVerificationStatus();
	
	void setVerificationStatus(VerificationStatus status);
	
	int getVerificationNumber();

	void setVerificationNumber(int verificationNumber);
	
	Date getLastUpdated();

	void setLastUpdated(Date lastEdit);
	
	int getTransfer();
	
	void setTransfer(int transferID);
	
	@StringLength(value = StringLength.UNLIMITED)
	String getDeleteReason();

	void setDeleteReason(String reason);
	
	@ManyToMany(value = VerifcToHazard.class)
	Hazards[] getHazards();
	
	@ManyToMany(value = VerifcToControl.class)
	Hazard_Controls[] getControls();
}
