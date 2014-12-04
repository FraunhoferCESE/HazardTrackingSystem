package org.fraunhofer.plugins.hts.db.service;

import java.util.List;
import org.fraunhofer.plugins.hts.db.ControlGroups;

public interface ControlGroupsService {
	ControlGroups add(String label);
	
	ControlGroups getControlGroupByID(String id);
	
	List<ControlGroups> all();
}
