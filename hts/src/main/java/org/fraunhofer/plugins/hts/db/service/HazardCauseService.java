package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;

public interface HazardCauseService {
	Hazard_Causes add(String causeID, String description, String effects, String owner, String title, Hazards hazard);
	
	Hazard_Causes getHazardCauseByID(String id);
	
	List<Hazard_Causes> all();
}
