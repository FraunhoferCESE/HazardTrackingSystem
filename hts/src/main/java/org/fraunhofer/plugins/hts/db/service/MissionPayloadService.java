package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.datatype.JIRAProject;
import org.fraunhofer.plugins.hts.db.Hazards;

public interface MissionPayloadService {
	
	List<JIRAProject> all();
}
