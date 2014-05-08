package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Mission_Phase;

import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface MissionPhaseService {
	Mission_Phase add(String label);

	Mission_Phase getMissionPhaseByID(String id);

	List<Mission_Phase> all();

	Mission_Phase[] getMissionPhasesByID(Integer[] id);

	List<Mission_Phase> getRemainingMissionPhases(Mission_Phase[] currentList);
}
