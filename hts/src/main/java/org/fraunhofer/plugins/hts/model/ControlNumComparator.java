package org.fraunhofer.plugins.hts.model;

import java.util.Comparator;

public class ControlNumComparator implements Comparator<Hazard_Controls> {
	public int compare(Hazard_Controls o1, Hazard_Controls o2) {
		if (o1.getControlNumber() == o2.getControlNumber())
			return 0;
		return o1.getControlNumber() < o2.getControlNumber() ? -1 : 1;
	}
}