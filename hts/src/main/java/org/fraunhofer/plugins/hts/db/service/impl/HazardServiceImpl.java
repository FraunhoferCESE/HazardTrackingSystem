package org.fraunhofer.plugins.hts.db.service.impl;

import com.atlassian.activeobjects.external.ActiveObjects;

import net.java.ao.DBParam;
import net.java.ao.Query;

import java.util.Date;
import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
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
			Risk_Likelihoods likelihood, Hazard_Group group, Review_Phases reviewPhase) {
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
		hazard.setHazardGroup(group);
		hazard.setReviewPhase(reviewPhase);
		hazard.save();
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
			Risk_Likelihoods likelihood, Hazard_Group group, Review_Phases reviewPhase) {
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
			updated.setHazardGroup(group);
			updated.setReviewPhase(reviewPhase);
			updated.save();
		}
		return updated;
	}

	@Override
	public Subsystems[] getSubsystems() {
		return null;

	}

	@Override
	public Boolean hazardNumberExists(String hazardNumber) {
		// TODO Auto-generated method stub
		Hazards[] hazards = ao.find(Hazards.class, Query.select().where("HAZARD_NUM=?", hazardNumber));
		return hazards.length > 0 ? true : false;
	}
	
}
