package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazard_Group;


import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface HazardGroupService {
	Hazard_Group add(String label);
	List<Hazard_Group> all();
}
