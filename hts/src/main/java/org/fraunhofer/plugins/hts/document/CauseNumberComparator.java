package org.fraunhofer.plugins.hts.document;

import java.util.Comparator;

import org.fraunhofer.plugins.hts.model.Hazard_Causes;

public class CauseNumberComparator implements Comparator<Hazard_Causes> {

	@Override
	public int compare(Hazard_Causes x, Hazard_Causes y) {
		return Integer.compare(x.getCauseNumber(), y.getCauseNumber());
	}
}