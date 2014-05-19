package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class HazardCauseServiceImpl implements HazardCauseService {
	private final ActiveObjects ao;
	
	public HazardCauseServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	
	@Override
	public Hazard_Causes add(String causeID, String description, String effects, String owner, String title) {
		final Hazard_Causes cause = ao.create(Hazard_Causes.class);
		cause.setCauseID(causeID);
		cause.setDescription(description);
		cause.setEffects(effects);
		cause.setOwner(owner);
		cause.setTitle(title);
		cause.save();
		return cause;
	}

	@Override
	public Hazard_Causes getHazardCauseByID(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Hazard_Causes> all() {
		// TODO Auto-generated method stub
		return newArrayList(ao.find(Hazard_Causes.class));
	}

}
