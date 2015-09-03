package org.fraunhofer.plugins.hts.view.model;

import org.fraunhofer.plugins.hts.model.Verifications;

public class VerificationTransfer {
	
	private Verifications origin;
	private Verifications target;
	
	
	public VerificationTransfer(Verifications origin, Verifications target) {
		super();
		this.origin = origin;
		this.target = target;
	}

	public Verifications getOrigin() {
		return origin;
	}

	public Verifications getTarget() {
		return target;
	}
	
	public static VerificationTransfer createTransfer(Verifications origin, Verifications target) {
		return new VerificationTransfer(origin, target);
	}

}
