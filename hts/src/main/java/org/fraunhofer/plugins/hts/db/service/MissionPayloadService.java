package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.datatype.JIRAProject;

import com.atlassian.jira.user.ApplicationUser;

public interface MissionPayloadService {
	
	List<JIRAProject> all();

	List<JIRAProject> getUserProjects(ApplicationUser user);
}
