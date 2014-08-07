package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.VerificationType;

public interface VerificationTypeService {
	VerificationType add(String label);
	
	VerificationType getVerificationTypeByID(String id);
	
	List<VerificationType> all();
}