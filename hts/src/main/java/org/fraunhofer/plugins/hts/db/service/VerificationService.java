package org.fraunhofer.plugins.hts.db.service;

import java.util.Date;
import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.VerificationStatus;
import org.fraunhofer.plugins.hts.db.VerificationType;
import org.fraunhofer.plugins.hts.db.Verifications;

public interface VerificationService {
	
	Verifications getVerificationByID(int verificationID);
	
	Verifications getVerificationByID(String verificationID);
	
	List<Verifications> all();
	
	List<Verifications> getAllVerificationsWithinAHazard(Hazards hazard);
	
	List<Verifications> getAllNonDeletedVerificationsWithinAHazard(Hazards hazard);

	Verifications add(int hazardID, String description, VerificationStatus status, VerificationType type,
			String responsibleParty, Date estimatedCompletionDate, Hazard_Controls[] controls);

	Verifications update(int verificationID, String description, VerificationStatus status, VerificationType type,
			String responsibleParty, Date estimatedCompletionDate, Hazard_Controls[] controls);

	Verifications deleteVerification(Verifications verificationToDelete, String reason);
	
}