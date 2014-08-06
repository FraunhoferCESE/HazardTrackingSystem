package org.fraunhofer.plugins.hts.db.service;

import java.util.Date;
import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.VerificationStatus;
import org.fraunhofer.plugins.hts.db.VerificationType;
import org.fraunhofer.plugins.hts.db.Verifications;

public interface VerificationService {
	
	List<Verifications> all();
	
	List<Verifications> getAllVerificationsWithinAHazard(Hazards hazard);

	Verifications add(Hazards hazard, String description, VerificationType verificationType, 
			String responsibleParty, Date estCompletionDate, VerificationStatus verificationStatus);
	
	
}