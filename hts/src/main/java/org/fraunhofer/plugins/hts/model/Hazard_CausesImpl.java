package org.fraunhofer.plugins.hts.model;

import java.util.Arrays;
import java.util.Comparator;

public class Hazard_CausesImpl {

	private Hazard_Causes cause;

	private Comparator<Hazard_Controls> controlIdComparator = new Comparator<Hazard_Controls>() {
		public int compare(Hazard_Controls o1, Hazard_Controls o2) {
			if (o1.getID() == o2.getID())
				return 0;
			return o1.getID() < o2.getID() ? -1 : 1;
		}
	};

	public Hazard_CausesImpl(Hazard_Causes cause) {
		this.cause = cause;
	}

	public Hazard_Controls[] getControls() {
		Hazard_Controls[] controls = cause.getControls();
		Arrays.sort(controls, controlIdComparator);
		return controls;
	}

}
