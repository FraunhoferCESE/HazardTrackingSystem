package org.fraunhofer.plugins.hts.model;

import java.util.Comparator;

public class ControlNumberComparator implements Comparator<Hazard_Controls> {

	@Override
	public int compare(Hazard_Controls x, Hazard_Controls y) {
		return Integer.compare(x.getControlNumber(), y.getControlNumber());
	}
}