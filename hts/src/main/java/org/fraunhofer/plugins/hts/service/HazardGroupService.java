package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.fraunhofer.plugins.hts.model.Hazard_Group;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public class HazardGroupService {
	private final ActiveObjects ao;

	private static boolean initialized = false;
	private static Object _lock = new Object();

	public HazardGroupService(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	private Hazard_Group add(String label) {
		final Hazard_Group group = ao.create(Hazard_Group.class);
		group.setLabel(label);
		group.save();
		return group;
	}

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

	public List<Hazard_Group> getRemaining(Hazard_Group[] currentList) {
		initializeTable();
		List<Hazard_Group> listAll = newArrayList(ao.find(Hazard_Group.class));

		if (!listAll.isEmpty()) {
			for (Hazard_Group currRegistered : currentList) {
				listAll.remove(currRegistered);
			}
		}

		return listAll;
	}

	private void initializeTable() {
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
}
