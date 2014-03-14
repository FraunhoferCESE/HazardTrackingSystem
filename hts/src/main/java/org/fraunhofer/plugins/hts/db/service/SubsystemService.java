package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Subsystems;


public interface SubsystemService {
	Subsystems add(Hazards hazard, String label, String subsysDesc);
	Subsystems getSubsystemByID(String id);
	Subsystems update(Subsystems subsystemToUpdate, String label);
	List<Subsystems> all();
}
