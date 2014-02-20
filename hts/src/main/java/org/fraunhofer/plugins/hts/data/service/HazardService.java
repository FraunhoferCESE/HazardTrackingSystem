package org.fraunhofer.plugins.hts.data.service;

import java.util.List; 

import org.fraunhofer.plugins.hts.db.Hazards;


import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface HazardService {
	Hazards add(String number, String title, String description, String preparer);
	List<Hazards> all();
}
