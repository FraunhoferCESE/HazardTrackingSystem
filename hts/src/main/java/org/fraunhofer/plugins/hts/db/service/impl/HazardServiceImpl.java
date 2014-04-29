package org.fraunhofer.plugins.hts.db.service.impl;

import com.atlassian.activeobjects.external.ActiveObjects;

import net.java.ao.DBParam;
import net.java.ao.Query;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.fraunhofer.plugins.hts.db.GroupToHazard;
import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.db.SubsystemToHazard;
import org.fraunhofer.plugins.hts.db.Subsystems;
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
	public Hazards add(String title, String description, String preparer, String email, String hazardNum,
			Date initationDate, Date completionDate, Date revisionDate, Risk_Categories risk,
			Risk_Likelihoods likelihood, Hazard_Group[] groups, Review_Phases reviewPhase, Subsystems[] subsystems, Mission_Payload missionPayload) {
		final Hazards hazard = ao.create(Hazards.class, new DBParam("TITLE", title), new DBParam("HAZARD_NUM",
				hazardNum));
		hazard.setHazardDesc(description);
		hazard.setPreparer(preparer);
		hazard.setEmail(email);
		hazard.setInitiationDate(initationDate);
		hazard.setCompletionDate(completionDate);
		hazard.setRevisionDate(revisionDate);
		hazard.setRiskCategory(risk);
		hazard.setRiskLikelihood(likelihood);
		hazard.setReviewPhase(reviewPhase);
		hazard.setMissionPayload(missionPayload);
		hazard.save();
		if(subsystems != null) {
			for(Subsystems subsystem : subsystems) {
				try {
					associateSubsystemToHazard(subsystem, hazard);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(groups != null) {
			for(Hazard_Group group : groups) {
				try {
					associateHazardGroupToHazard(group, hazard);
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
	public Hazards getHazardByID(String id) {
		final Hazards[] hazards = ao.find(Hazards.class, Query.select().where("ID=?", id));
		return hazards.length > 0 ? hazards[0] : null;
	}

	@Override
	// TODO add init date and completion and error handling
	public Hazards update(String id, String title, String description, String preparer, String email, String hazardNum,
			Date initationDate, Date completionDate, Date revisionDate, Risk_Categories risk,
			Risk_Likelihoods likelihood, Hazard_Group[] groups, Review_Phases reviewPhase, Subsystems[] subsystems, Mission_Payload missionPayload) {
		Hazards updated = getHazardByID(id);
		if (updated != null) {
			updated.setTitle(title);
			updated.setHazardNum(hazardNum);
			updated.setHazardDesc(description);
			updated.setPreparer(preparer);
			updated.setEmail(email);
			updated.setInitiationDate(initationDate);
			updated.setCompletionDate(completionDate);
			updated.setRevisionDate(revisionDate);
			updated.setRiskCategory(risk);
			updated.setRiskLikelihood(likelihood);
			updated.setReviewPhase(reviewPhase);
			updated.setMissionPayload(missionPayload);
			updated.save();
			removeSubsystems(updated.getID());
			removeHazardGroups(updated.getID());
			if(subsystems != null) {
				for(Subsystems subsystem : subsystems) {
					try {
						associateSubsystemToHazard(subsystem, updated);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if(groups != null) {
				for(Hazard_Group group : groups) {
					try {
						associateHazardGroupToHazard(group, updated);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return updated;
	}

	@Override
	public Boolean hazardNumberExists(String hazardNumber) {
		Hazards[] hazards = ao.find(Hazards.class, Query.select().where("HAZARD_NUM=?", hazardNumber));
		return hazards.length > 0 ? true : false;
	}

	@Override
	public void deleteHazard(int id) {
		removeSubsystems(id);
		//removeHazardGroups(id);
		ao.delete(ao.find(Hazards.class, Query.select().where("ID=?", id)));		
	}

	@Override
	public List<Hazards> getHazardsByMissionPayload(String id) {
		return newArrayList(ao.find(Hazards.class, Query.select().where("MISSION_PAYLOAD_ID=?", id)));
	}
	
	private void associateSubsystemToHazard(Subsystems subsystems, Hazards hazard) throws SQLException {
		final SubsystemToHazard subsystemToHazard = ao.create(SubsystemToHazard.class);
		subsystemToHazard.setSubsystem(subsystems);
		subsystemToHazard.setHazard(hazard);
		subsystemToHazard.save();
	}
	
	private void associateHazardGroupToHazard(Hazard_Group hazardGroup, Hazards hazard) throws SQLException {
		final GroupToHazard hazardGroupToHazard = ao.create(GroupToHazard.class);
		hazardGroupToHazard.setHazard(hazard);
		hazardGroupToHazard.setHazardGroup(hazardGroup);
		hazardGroupToHazard.save();
	}
	
	private void removeSubsystems(int id) {
		ao.delete(ao.find(SubsystemToHazard.class, Query.select().where("HAZARD_ID=?", id)));
	}
	
	private void removeHazardGroups(int id) {
		ao.delete(ao.find(GroupToHazard.class, Query.select().where("HAZARD_ID=?", id)));
	}
}
