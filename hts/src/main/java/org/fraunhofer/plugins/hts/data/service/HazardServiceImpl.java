package org.fraunhofer.plugins.hts.data.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazards;


import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class HazardServiceImpl implements HazardService {
	private final ActiveObjects ao;
	
	public HazardServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	
	//TODO add javadoc and fix the remaining fields.
	@Override
	public 	Hazards add(String number, String title, String description, String preparer) {
		final Hazards hazard = ao.create(Hazards.class);
		//hazard.setNumber(number);
		hazard.setTitle(title);
		//hazard.setDescription(description);
		//hazard.setPreparer(preparer);
		hazard.save();
		return hazard;
	}
	
	@Override
	public List<Hazards> all() {
		return newArrayList(ao.find(Hazards.class));
	}
}
