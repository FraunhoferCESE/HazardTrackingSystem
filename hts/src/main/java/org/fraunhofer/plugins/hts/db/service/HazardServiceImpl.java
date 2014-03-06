package org.fraunhofer.plugins.hts.db.service;

import com.atlassian.activeobjects.external.ActiveObjects;

import net.java.ao.DBParam;

import java.util.Date;
import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazard_Group;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class HazardServiceImpl implements HazardService {
	private final ActiveObjects ao;
	
	public HazardServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	
	//TODO add javadoc and fix the remaining fields.
	@Override
	public Hazards add(String title, String description, String preparer, String hazardNum, Date created, Date completed, Date lastEdit, 
			Risk_Categories risk, Risk_Likelihoods likelihood, Hazard_Group group, Review_Phases reviewPhase) {
		final Hazards hazard = ao.create(Hazards.class, 
				new DBParam("TITLE", title), new DBParam("HAZARD_NUM", hazardNum));
		hazard.setHazardDesc(description);
		hazard.setPreparer(preparer);
		hazard.setInitiationDate(created);
		hazard.setCompletionDate(completed);
		hazard.setRevisionDate(lastEdit);
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
	
	
}
