package org.fraunhofer.plugins.hts.model;

import java.util.Arrays;

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
}
