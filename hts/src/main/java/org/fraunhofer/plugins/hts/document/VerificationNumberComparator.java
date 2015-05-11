package org.fraunhofer.plugins.hts.document;

import java.util.Comparator;

import org.fraunhofer.plugins.hts.db.Verifications;
 
public class VerificationNumberComparator implements Comparator<Verifications> {

	@Override
	public int compare(Verifications x, Verifications y) {
		return Integer.compare(x.getVerificationNumber(), y.getVerificationNumber());
	}
}