package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Mission_Phase;
import org.fraunhofer.plugins.hts.db.service.MissionPhaseService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class MissionPhaseServiceImpl implements MissionPhaseService {
	private final ActiveObjects ao;

	private static boolean initialized = false;
	private static Object _lock = new Object();

	public MissionPhaseServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	@Override
	public Mission_Phase add(String label) {
		final Mission_Phase phase = ao.create(Mission_Phase.class);
		phase.setLabel(label);
		phase.save();
		return phase;
	}

	@Override
	public List<Mission_Phase> all() {
		initializeTable();
		return newArrayList(ao.find(Mission_Phase.class));
	}

	@Override
	public Mission_Phase getMissionPhaseByID(String id) {
		initializeTable();
		final Mission_Phase[] phase = ao.find(Mission_Phase.class, Query.select().where("ID=?", id));
		return phase.length > 0 ? phase[0] : null;
	}
	
	@Override
	public Mission_Phase[] getMissionPhasesByID(Integer[] id) {
		initializeTable();
		if(id == null) {
			return null;
		}
		else {
			Mission_Phase[] missionPhaseArr = new Mission_Phase[id.length];
			for(int i = 0; i < id.length; i++) {
				missionPhaseArr[i] = ao.get(Mission_Phase.class, id[i]);
			}
			return missionPhaseArr;
		}
	}
	
	@Override
	public List<Mission_Phase> getRemainingMissionPhases(Mission_Phase[] currentList) {
		List<Mission_Phase> listAll = all();
		
		if(!listAll.isEmpty()) {
			for(Mission_Phase currRegistered : currentList) {
				listAll.remove(currRegistered);
			}
		}
		
		return listAll;
	}

	public void initializeTable() {
		synchronized (_lock) {
			if (!initialized) {
				if (ao.find(Mission_Phase.class).length == 0) {
					add("Pre-launch");
					add("Launch");
					add("Cruise");
					add("Encounter");
					add("Extended Operations");
					add("Decommissioning");
				}
				initialized = true;
			}
		}

	}

	public static boolean isInitialized() {
		synchronized (_lock) {
			return initialized;
		}
	}

	public static void reset() {
		synchronized (_lock) {
			initialized = false;
		}
	}

}
