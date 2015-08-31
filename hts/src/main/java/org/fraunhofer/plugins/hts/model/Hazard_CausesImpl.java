package org.fraunhofer.plugins.hts.model;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class Hazard_CausesImpl {

	private Hazard_Causes cause;

	public Hazard_CausesImpl(Hazard_Causes cause) {
		this.cause = cause;
	}

	public Hazard_Controls[] getControls() {
		Hazard_Controls[] controls = cause.getControls();
		Arrays.sort(controls, new EntityIdComparator());
		return controls;
	}
	
	public List<Hazard_Controls> getActiveControls() {
		List<Hazard_Controls> activeControls = Lists.newArrayList();
		Hazard_Controls[] controls = getControls();
		for (int i = 0; i < controls.length; i++) {
			if(Strings.isNullOrEmpty(controls[i].getDeleteReason())) {
				activeControls.add(controls[i]);
			}
		}
		return activeControls;
		
	}
}
