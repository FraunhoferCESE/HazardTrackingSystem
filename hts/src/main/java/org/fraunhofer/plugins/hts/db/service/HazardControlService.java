package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.ControlGroups;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;

public interface HazardControlService {
	Hazard_Controls add(String description, ControlGroups controlGroup);

	Hazard_Controls getHazardControlByID(String id);

	List<Hazard_Controls> all();
	
}
