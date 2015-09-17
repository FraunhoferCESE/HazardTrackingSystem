package org.fraunhofer.plugins.hts.model;

import java.util.Comparator;

public class VerificationNumberComparator implements Comparator<Verifications> {

	@Override
	public int compare(Verifications x, Verifications y) {
		return Integer.compare(x.getVerificationNumber(), y.getVerificationNumber());
	}
}