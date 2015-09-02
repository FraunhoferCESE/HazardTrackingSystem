package org.fraunhofer.plugins.hts.model;

import java.util.Arrays;

public class VerificationsImpl {
	
	private Verifications verification;
	
	public VerificationsImpl(Verifications verification) {
		this.verification = verification;
	}

	Hazard_Controls[] getControls() {
		Hazard_Controls[] controls = verification.getControls();
		Arrays.sort(controls, new EntityIdComparator());
		return controls;
	}
}
