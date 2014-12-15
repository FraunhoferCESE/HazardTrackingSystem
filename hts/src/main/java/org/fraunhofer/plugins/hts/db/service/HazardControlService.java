package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.datatype.HazardControlDTMinimalJson;
import org.fraunhofer.plugins.hts.datatype.HazardControlTransferDT;
import org.fraunhofer.plugins.hts.db.ControlGroups;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;

public interface HazardControlService {
	
	Hazard_Controls add(int hazardID, String description, ControlGroups controlGroup, Hazard_Causes[] causes);
	
	Hazard_Controls updateRegularControl(int controlID, String description, ControlGroups controlGroup, Hazard_Causes[] causes);
	
	Hazard_Controls updateTransferredControl(int controlID, String transferReason);
	
	Hazard_Controls getHazardControlByID(int controlID);
	
	Hazard_Controls getHazardControlByID(String controlID);
	
	List<HazardControlDTMinimalJson> getAllNonDeletedControlsWithinCauseMinimalJson(int causeID);

	List<Hazard_Controls> getAllControlsWithinAHazard(Hazards hazard);
	
	List<Hazard_Controls> getAllNonDeletedControlsWithinAHazard(Hazards hazard);
	
	List<HazardControlTransferDT> getAllTransferredControls(Hazards hazard);
	
	Hazard_Controls deleteControl(int controlID, String deleteReason);
	
	Hazard_Controls addControlTransfer(int originHazardID, int targetControlID, String transferReason);
	
	Hazard_Controls addCauseTransfer(int originHazardID, int targetCauseID, String transferReason);
	
	Hazard_Controls[] getHazardControlsByID(Integer[] id);

	List<Hazard_Controls> all();

}
