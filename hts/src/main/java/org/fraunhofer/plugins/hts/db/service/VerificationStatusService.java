package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.VerificationStatus;

public interface VerificationStatusService {
	VerificationStatus add(String label);
	
	VerificationStatus getVerificationStatusByID(String id);
	
	List<VerificationStatus> all();
}