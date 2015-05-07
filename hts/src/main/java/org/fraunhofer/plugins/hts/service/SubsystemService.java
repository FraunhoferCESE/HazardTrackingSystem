package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.model.Subsystems;

import com.atlassian.activeobjects.external.ActiveObjects;

public class SubsystemService {
	private final ActiveObjects ao;

	private static boolean initialized = false;
	private static Object _lock = new Object();

	public SubsystemService(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	public Subsystems add(String label, String subsysDesc) {
		final Subsystems subsys = ao.create(Subsystems.class);
		subsys.setLabel(label);
		subsys.setDescription(subsysDesc);
		subsys.save();
		return subsys;
	}

	public Subsystems getSubsystemByID(String id) {
		initializeTable();
		final Subsystems[] subsystem = ao.find(Subsystems.class, Query.select().where("ID=?", id));
		return subsystem.length > 0 ? subsystem[0] : null;
	}

	public List<Subsystems> all() {
		initializeTable();
		return newArrayList(ao.find(Subsystems.class));
	}

	public Subsystems update(Subsystems subsystemToUpdate, String label) {
		subsystemToUpdate.setLabel(label);
		subsystemToUpdate.save();
		return null;
	}

	public Subsystems[] getSubsystemsByID(Integer[] id) {
		initializeTable();
		if (id == null) {
			return null;
		} else {
			Subsystems[] subsystemArr = new Subsystems[id.length];
			for (int i = 0; i < id.length; i++) {
				subsystemArr[i] = ao.get(Subsystems.class, id[i]);
			}
			return subsystemArr;
		}
	}

	public List<Subsystems> getRemaining(Subsystems[] currentList) {
		List<Subsystems> listAll = all();

		for (Subsystems currRegistered : currentList) {
			listAll.remove(currRegistered);
		}

		return listAll;
	}

	public void initializeTable() {
		synchronized (_lock) {
			if (!initialized) {
				if (ao.find(Subsystems.class).length == 0) {
					add("Structure", "");
					add("Power", "Battery, Solar Arrays, Electrical Distribution");
					add("Communications", "Antennas, HGA");
					add("Propulsion", "");
					add("Guidance and Control", "Reaction Wheels");
					add("Instrument", "");
					add("Ordnance/Pyro", "");
					add("Hazardous Materials", "");
					add("GSE Electrical Materials", "");
					add("GSE Mechanical Materials", "");
					add("System Interfaces", "S/C, Human, Facility");
					add("Environmental", "");
					add("Ionized Radiation", "");
					add("Non-Ionized Radiation", "");
					add("Propellants", "");
					add("Electrical/Electronics", "");
					add("Human Factors", "");
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
