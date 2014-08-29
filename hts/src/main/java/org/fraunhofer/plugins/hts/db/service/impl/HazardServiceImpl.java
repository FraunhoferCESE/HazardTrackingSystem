package org.fraunhofer.plugins.hts.db.service.impl;

import com.atlassian.activeobjects.external.ActiveObjects;

import net.java.ao.DBParam;
import net.java.ao.Query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fraunhofer.plugins.hts.db.GroupToHazard;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.Mission_Phase;
import org.fraunhofer.plugins.hts.db.PhaseToHazard;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.SubsystemToHazard;
import org.fraunhofer.plugins.hts.db.Subsystems;
import org.fraunhofer.plugins.hts.db.Verifications;
import org.fraunhofer.plugins.hts.db.service.HazardService;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class HazardServiceImpl implements HazardService {
	private final ActiveObjects ao;

	public HazardServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	// TODO add javadoc and fix the remaining fields.
	@Override
	public Hazards add(String title, String safetyRequirements, String description, String justification, String openWork, String preparer,
			String email, String hazardNum, String hazardVersionNum, Date initationDate, Date completionDate, Date revisionDate, Hazard_Group[] groups,  
			Review_Phases reviewPhase, Subsystems[] subsystems, Mission_Phase[] missionPhase, Mission_Payload missionPayload) {
		final Hazards hazard = ao.create(Hazards.class, new DBParam("TITLE", title), new DBParam("HAZARD_NUM",
				hazardNum));
		hazard.setHazardVersionNum(hazardVersionNum);
		hazard.setHazardSafetyRequirements(safetyRequirements);
		hazard.setHazardDesc(description);
		hazard.setHazardJustification(justification);
		hazard.setHazardOpenWork(openWork);
		hazard.setPreparer(preparer);
		hazard.setEmail(email);
		hazard.setInitiationDate(initationDate);
		hazard.setCompletionDate(completionDate);
		hazard.setRevisionDate(revisionDate);
		hazard.setReviewPhase(reviewPhase);
		hazard.setMissionPayload(missionPayload);
		hazard.setActive(true);
		hazard.save();
		if (subsystems != null) {
			for (Subsystems subsystem : subsystems) {
				try {
					associateSubsystemToHazard(subsystem, hazard);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (groups != null) {
			for (Hazard_Group group : groups) {
				try {
					associateHazardGroupToHazard(group, hazard);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		if (missionPhase != null) {
			for (Mission_Phase phase : missionPhase) {
				try {
					associateMissionPhaseToHazard(phase, hazard);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return hazard;
	}

	@Override
	public List<Hazards> all() {
		return newArrayList(ao.find(Hazards.class));
	}
	
	@Override
	public List<Hazards> getAllNonDeletedHazards() {
		List<Hazards> allRemaining = new ArrayList<Hazards>();
		for (Hazards current : all()) {
			if (current.getActive() == true) {
				allRemaining.add(current);
			}
		}
		return allRemaining;
	}

	@Override
	public Hazards getHazardByID(String id) {
		final Hazards[] hazards = ao.find(Hazards.class, Query.select().where("ID=?", id));
		return hazards.length > 0 ? hazards[0] : null;
	}

	@Override
	public Hazards getHazardByHazardNum(String hazardNum) {
		final Hazards[] hazards = ao.find(Hazards.class, Query.select().where("HAZARD_NUM=?", hazardNum));
		return hazards.length > 0 ? hazards[0] : null;
	}

	// TODO add init date and completion and error handling
	@Override
	public Hazards update(String id, String title, String safetyRequirements, String description, String justification, String openWork, String preparer, 
			String email, String hazardNum, String hazardVersionNum, Date initationDate, Date completionDate, Date revisionDate, Hazard_Group[] groups, 
			Review_Phases reviewPhase, Subsystems[] subsystems, Mission_Phase[] missionPhase, Mission_Payload missionPayload) {
		Hazards updated = getHazardByID(id);
		if (updated != null) {
			updated.setTitle(title);
			updated.setHazardNum(hazardNum);
			updated.setHazardVersionNum(hazardVersionNum);
			updated.setHazardSafetyRequirements(safetyRequirements);
			updated.setHazardDesc(description);
			updated.setHazardJustification(justification);
			updated.setHazardOpenWork(openWork);
			updated.setPreparer(preparer);
			updated.setEmail(email);
			updated.setInitiationDate(initationDate);
			updated.setCompletionDate(completionDate);
			updated.setRevisionDate(revisionDate);
			updated.setReviewPhase(reviewPhase);
			updated.setMissionPayload(missionPayload);
			updated.save();
			removeSubsystems(updated.getID());
			removeHazardGroups(updated.getID());
			removeMissionPhase(updated.getID());
			if (subsystems != null) {
				for (Subsystems subsystem : subsystems) {
					try {
						associateSubsystemToHazard(subsystem, updated);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (groups != null) {
				for (Hazard_Group group : groups) {
					try {
						associateHazardGroupToHazard(group, updated);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			if (missionPhase != null) {
				for (Mission_Phase phase : missionPhase) {
					try {
						associateMissionPhaseToHazard(phase, updated);
					} catch (SQLException e) {
						// TODO: handle exception
					}
				}
			}
		}
		return updated;
	}

	@Override
	public Boolean hazardNumberExists(String hazardNumber) {
		Hazards[] hazards = ao.find(Hazards.class, Query.select().where("HAZARD_NUM=?", hazardNumber));
		
		if (hazards.length > 0) {
			boolean inUse = false;
			for (Hazards current : hazards) {
				if (current.getActive() == true) {
					inUse = true;
					break;
				}
			}
			return inUse;
		}
		else {
			return false;
		}
	}

	@Override
	public void deleteHazard(Hazards hazardToDelete) {
		// Mark hazards as inactive
		hazardToDelete.setActive(false);
		Date deleteDate = new Date();
		hazardToDelete.setHazardNum(hazardToDelete.getHazardNum() + " (DELETED " + deleteDate.toString() + ")");
		hazardToDelete.save();
		
		// Mark all non-deleted causes as deleted
		for (Hazard_Causes current : hazardToDelete.getHazardCauses()) {
			current.setDeleteReason("HAZARD_DELETED");
			current.save();
		}

		// Mark all non-deleted controls as deleted
		for (Hazard_Controls current : hazardToDelete.getHazardControls()) {
			current.setDeleteReason("HAZARD_DELETED");
			current.save();
		}
		
		// Mark all non-deleted verifications as delete
		for (Verifications current : hazardToDelete.getVerifications()) {
			current.setDeleteReason("HAZARD_DELETED");
			current.save();
		}
	}

	@Override
	public List<Hazards> getHazardsByMissionPayload(String id) {
		List<Hazards> allHazardsBelongingToMission = newArrayList(ao.find(Hazards.class, Query.select().where("MISSION_PAYLOAD_ID=?", id)));
		List<Hazards> allNonDeletedHazards = new ArrayList<Hazards>();
		
		for (Hazards current : allHazardsBelongingToMission) {
			if (current.getActive()) {
				allNonDeletedHazards.add(current);
			}
		}
		
		return allNonDeletedHazards;
	}

	@Override
	public Hazards getNewestHazardReport() {
		Hazards[] hazard = ao.find(Hazards.class, Query.select().order("ID DESC"));
		Hazards lastCreated = null;
		if(hazard.length > 0) {
			lastCreated = hazard[0];
		}
		return lastCreated;
	}

	private void associateSubsystemToHazard(Subsystems subsystems, Hazards hazard) throws SQLException {
		final SubsystemToHazard subsystemToHazard = ao.create(SubsystemToHazard.class);
		subsystemToHazard.setSubsystem(subsystems);
		subsystemToHazard.setHazard(hazard);
		subsystemToHazard.save();
	}

	private void associateHazardGroupToHazard(Hazard_Group hazardGroup, Hazards hazard) throws SQLException {
		final GroupToHazard hazardGroupToHazard = ao.create(GroupToHazard.class);
		hazardGroupToHazard.setHazardGroup(hazardGroup);
		hazardGroupToHazard.setHazard(hazard);
		hazardGroupToHazard.save();
	}

	private void associateMissionPhaseToHazard(Mission_Phase phase, Hazards hazard) throws SQLException {
		final PhaseToHazard phaseToHazard = ao.create(PhaseToHazard.class);
		phaseToHazard.setMissionPhase(phase);
		phaseToHazard.setHazard(hazard);
		phaseToHazard.save();
	}

	private void removeMissionPhase(int id) {
		ao.delete(ao.find(PhaseToHazard.class, Query.select().where("HAZARD_ID=?", id)));
	}

	private void removeSubsystems(int id) {
		ao.delete(ao.find(SubsystemToHazard.class, Query.select().where("HAZARD_ID=?", id)));
	}

	private void removeHazardGroups(int id) {
		ao.delete(ao.find(GroupToHazard.class, Query.select().where("HAZARD_ID=?", id)));
	}

}
