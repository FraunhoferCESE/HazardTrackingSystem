package org.fraunhofer.plugins.hts.db.service;

import java.util.Date;
import java.util.List; 

import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Risk_Categories;


import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface HazardService {
	Hazards add(String title, String description, String preparer, String hazardNum, Date created, Date lastEdit, Date completed, Risk_Categories risk);
	List<Hazards> all();
}
