package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.datatype.HazardControlTransfers;
import org.fraunhofer.plugins.hts.db.ControlGroups;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;

public interface HazardControlService {
	
	Hazard_Controls add(Hazards hazard, String description, ControlGroups controlGroup, Hazard_Causes[] causes);
	
	Hazard_Controls update(String controlID, String description, ControlGroups controlGroup, Hazard_Causes[] causes);
	
	Hazard_Controls updateTransferredControl(String controlID, String transferReason);

	List<Hazard_Controls> getAllControlsWithinAHazard(Hazards hazard);
	
	List<Hazard_Controls> getAllNonDeletedControlsWithinAHazard(Hazards hazard);
	
	List<HazardControlTransfers> getAllTransferredControls(Hazards hazard);
	
	Hazard_Controls deleteControl(Hazard_Controls controlToBeDeleted, String reason);
	
	Hazard_Controls addControlTransfer(String transferComment, int targetID, Hazards hazard);
	
	Hazard_Controls addCauseTransfer(String transferComment, int targetID, Hazards hazard);
	
	Hazard_Controls getHazardControlByID(String id);

	List<Hazard_Controls> all();

}
