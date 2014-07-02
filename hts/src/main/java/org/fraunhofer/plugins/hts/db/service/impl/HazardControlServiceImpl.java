package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Date;
import java.util.List;

import net.java.ao.DBParam;

import org.fraunhofer.plugins.hts.db.ControlGroups;
import org.fraunhofer.plugins.hts.db.ControlToCause;
import org.fraunhofer.plugins.hts.db.ControlToHazard;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class HazardControlServiceImpl implements HazardControlService {
	private final ActiveObjects ao;
	
	public HazardControlServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	@Override
	public Hazard_Controls add(Hazards hazard, String description, ControlGroups controlGroup, Hazard_Causes[] causes) {
		final Hazard_Controls control = ao.create(Hazard_Controls.class, new DBParam("DESCRIPTION", description));
		control.setControlGroup(controlGroup);
		if (causes != null) {
			for (Hazard_Causes hc : causes) {
				associateControlToCause(control, hc);
			}
		}
		// Need to perform check at this point, update or create?
		// Check if OriginalDate is null?
		control.setOriginalDate(new Date());
		control.save();
		associateControlToHazard(hazard, control);
		return control;
	}
	
	@Override
	public List<Hazard_Controls> getAllControlsWithinAHazard(Hazards hazard) {
		return newArrayList(hazard.getHazardControls());
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
	
	private void associateControlToHazard(Hazards hazard, Hazard_Controls control) {
		final ControlToHazard controlToHazard = ao.create(ControlToHazard.class);
		controlToHazard.setHazard(hazard);
		controlToHazard.setControl(control);
		controlToHazard.save();
	}
	
	private void associateControlToCause(Hazard_Controls control, Hazard_Causes cause) {
		final ControlToCause controlToCause = ao.create(ControlToCause.class);
		controlToCause.setCause(cause);
		controlToCause.setControl(control);
		controlToCause.save();
	}

}
