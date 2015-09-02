package org.fraunhofer.plugins.hts.model;

import java.util.Arrays;

public class Hazard_ControlsImpl {
	
	private Hazard_Controls control;

	public Hazard_ControlsImpl(Hazard_Controls control) {
		this.control = control;
	}

	Verifications[] getVerifications() {
		Verifications[] verifications = control.getVerifications();
		Arrays.sort(verifications,new EntityIdComparator());
		return verifications;
	}
}
