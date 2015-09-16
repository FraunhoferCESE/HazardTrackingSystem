package org.fraunhofer.plugins.hts.model;

import java.util.Comparator;

public class VerificationNumComparator implements Comparator<Verifications> {
	public int compare(Verifications o1, Verifications o2) {
		if (o1.getVerificationNumber() == o2.getVerificationNumber())
			return 0;
		return o1.getVerificationNumber() < o2.getVerificationNumber() ? -1 : 1;
	}
}
