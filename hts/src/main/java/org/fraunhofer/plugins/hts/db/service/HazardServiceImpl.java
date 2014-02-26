package org.fraunhofer.plugins.hts.db.service;

import com.atlassian.activeobjects.external.ActiveObjects;

import java.util.Date;
import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Risk_Categories;


import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class HazardServiceImpl implements HazardService {
	private final ActiveObjects ao;
	
	public HazardServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	
	//TODO add javadoc and fix the remaining fields.
	@Override
	public Hazards add(String title, String description, String preparer, String hazardNum, Date created, Date lastEdit, Date completed, Risk_Categories risk) {
		final Hazards hazard = ao.create(Hazards.class);
		hazard.setHazardNum(hazardNum);
		hazard.setTitle(title);
		hazard.setHazardDesc(description);
		hazard.setPreparer(preparer);
		hazard.setInitiationDate(created);
		hazard.setRevisionDate(lastEdit);
		hazard.setCompletionDate(completed);
		hazard.setRiskCategory(risk);
		hazard.save();
		return hazard;
	}
	
	@Override
	public List<Hazards> all() {
		return newArrayList(ao.find(Hazards.class));
	}
}
