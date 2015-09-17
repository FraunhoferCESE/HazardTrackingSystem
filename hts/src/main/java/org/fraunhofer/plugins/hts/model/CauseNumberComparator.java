package org.fraunhofer.plugins.hts.model;

import java.util.Comparator;

public class CauseNumberComparator implements Comparator<Hazard_Causes> {

	@Override
	public int compare(Hazard_Causes x, Hazard_Causes y) {
		return Integer.compare(x.getCauseNumber(), y.getCauseNumber());
	}
}