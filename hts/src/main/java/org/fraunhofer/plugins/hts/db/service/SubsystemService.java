package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Subsystems;


public interface SubsystemService {
	Subsystems add(String value, String subsysDesc);
	Subsystems getSubsystemByID(String id);
	List<Subsystems> all();
}
