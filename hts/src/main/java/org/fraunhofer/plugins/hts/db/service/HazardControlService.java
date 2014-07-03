package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.ControlGroups;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;

public interface HazardControlService {
	
	Hazard_Controls add(Hazards hazard, String description, ControlGroups controlGroup, Hazard_Causes[] causes);
	
	Hazard_Controls update(String controlID, String description, ControlGroups controlGroup, Hazard_Causes[] causes);

	List<Hazard_Controls> getAllControlsWithinAHazard(Hazards hazard);
	
	Hazard_Controls getHazardControlByID(String id);

	List<Hazard_Controls> all();
}
