package org.fraunhofer.plugins.hts.db;
import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table("SubsystemToHazard")
public interface SubsystemToHazard extends Entity {
	void setHazard(Hazards hazard);
	Hazards getHazard();
	
	void setSubsystem(Subsystems subsystems);
	Subsystems getSubsystem();
}
