package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Verifications;

public interface VerificationService {
	
	List<Verifications> all();

	Verifications add(String description);
}