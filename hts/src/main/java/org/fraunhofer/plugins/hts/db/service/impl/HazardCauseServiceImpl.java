package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Date;
import java.util.List;

import net.java.ao.DBParam;
import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Causes_to_Hazards;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class HazardCauseServiceImpl implements HazardCauseService {
	private final ActiveObjects ao;
	
	public HazardCauseServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	
	@Override
	public Hazard_Causes add(String description, String effects, String owner, String title, Hazards hazard) {
		final Hazard_Causes cause = ao.create(Hazard_Causes.class, new DBParam("TITLE", title));
		cause.setCauseNumber("Cause " + getNewCauseNumber(hazard));
		cause.setDescription(description);
		cause.setEffects(effects);
		cause.setOwner(owner);
		cause.setLastUpdated(new Date());
		cause.save();
		associateCauseToHazard(hazard, cause);
		return cause;
	}
	
	@Override
	public Hazard_Causes update(String id, String description, String effects, String owner, String title) {
		Hazard_Causes causeToBeupdated = getHazardCauseByID(id);
		causeToBeupdated.setDescription(description);
		causeToBeupdated.setEffects(effects);
		causeToBeupdated.setOwner(owner);
		causeToBeupdated.setTitle(title);
		causeToBeupdated.save();
		return causeToBeupdated;
	}

	@Override
	public Hazard_Causes getHazardCauseByID(String id) {
		final Hazard_Causes[] hazardCause = ao.find(Hazard_Causes.class, Query.select().where("ID=?" ,id));
		return hazardCause.length > 0 ? hazardCause[0] : null;
	}

	@Override
	public List<Hazard_Causes> all() {
		return newArrayList(ao.find(Hazard_Causes.class));
	}
	
	private void associateCauseToHazard(Hazards hazard, Hazard_Causes hazardCause) {
		final Causes_to_Hazards causeToHazard = ao.create(Causes_to_Hazards.class);
		causeToHazard.setHazard(hazard);
		causeToHazard.setHazardCause(hazardCause);
		causeToHazard.save();
	}
	
	private int getNewCauseNumber(Hazards hazard) {
		return hazard.getHazardCauses().length + 1;
	}
}
