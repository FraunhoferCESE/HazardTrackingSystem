package org.fraunhofer.plugins.hts.db;
import net.java.ao.Entity;

public interface SubsystemToHazard extends Entity {
	void setHazard(Hazards hazard);
	Hazards getHazard();
	
	void setSubsystem(Subsystems subsystems);
	Subsystems getSubsystem();
}
