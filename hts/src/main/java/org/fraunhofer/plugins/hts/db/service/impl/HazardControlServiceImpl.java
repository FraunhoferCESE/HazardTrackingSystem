package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import net.java.ao.DBParam;

import org.fraunhofer.plugins.hts.db.ControlGroups;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class HazardControlServiceImpl implements HazardControlService {
	private final ActiveObjects ao;
	
	public HazardControlServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	@Override
	// Add parameter here: Hazard_Causes[] causes
	public Hazard_Controls add(String description, ControlGroups controlGroup) {
		final Hazard_Controls control = ao.create(Hazard_Controls.class, new DBParam("DESCRIPTION", description));
		control.setControlGroup(controlGroup);
		control.save();
		return control;
	}

	@Override
	public Hazard_Controls getHazardControlByID(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Hazard_Controls> all() {
		// TODO Auto-generated method stub
		return null;
	}

}
