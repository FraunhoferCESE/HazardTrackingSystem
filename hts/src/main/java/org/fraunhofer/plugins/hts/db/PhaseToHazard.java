package org.fraunhofer.plugins.hts.db;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table("PhaseToHazard")
public interface PhaseToHazard extends Entity {
	void setHazard(Hazards hazard);

	Hazards getHazard();

	void setMissionPhase(Mission_Phase missionPhase);

	Mission_Phase getMissionPhase();
}
