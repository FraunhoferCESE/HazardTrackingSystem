package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.service.HazardGroupService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class HazardGroupServiceImpl implements HazardGroupService {
	private final ActiveObjects ao;

	private static boolean initialized = false;
	private static Object _lock = new Object();

	public HazardGroupServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	@Override
	public Hazard_Group add(String label) {
		final Hazard_Group group = ao.create(Hazard_Group.class);
		group.setLabel(label);
		group.save();
		return group;
	}

	@Override
	public List<Hazard_Group> all() {
		initializeTable();
		return newArrayList(ao.find(Hazard_Group.class));
	}

	@Override
	public Hazard_Group getHazardGroupByID(String id) {
		initializeTable();
		final Hazard_Group[] group = ao.find(Hazard_Group.class, Query.select().where("ID=?", id));
		return group.length > 0 ? group[0] : null;
	}

	// TODO see if a better way is possible
	@Override
	public Hazard_Group[] getHazardGroupsByID(Integer[] id) {
		initializeTable();
		if (id == null) {
			return null;
		} else {
			Hazard_Group[] hazardGroupArr = new Hazard_Group[id.length];
			for (int i = 0; i < id.length; i++) {
				hazardGroupArr[i] = ao.get(Hazard_Group.class, id[i]);
			}
			return hazardGroupArr;
		}
	}

	@Override
	public List<Hazard_Group> getRemainingHazardGroups(Hazard_Group[] currentList) {
		List<Hazard_Group> listAll = all();

		if (!listAll.isEmpty()) {
			for (Hazard_Group currRegistered : currentList) {
				listAll.remove(currRegistered);
			}
		}

		return listAll;
	}

	public void initializeTable() {
		synchronized (_lock) {
			if (!initialized) {
				if (ao.find(Hazard_Group.class).length == 0) {
					add("Acceleration");
					add("Asphyxiation");
					add("Contamination");
					add("Corrosion");
					add("Electrical");
					add("Fire/Explosion");
					add("Impact");
					add("Injury or Illness");
					add("Noise");
					add("Pressure");
					add("Ionizing Radiation");
					add("Non-Ionizing Radiation");
					add("Temperature");
					add("Toxic");
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
