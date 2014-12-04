package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.datatype.HazardCauseTransferDT;
import org.fraunhofer.plugins.hts.datatype.HazardControlDTMinimalJson;
import org.fraunhofer.plugins.hts.datatype.TransferClass;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;

public interface HazardCauseService {
	Hazard_Causes add(int hazardID, String title, String owner, Risk_Categories risk, 
			Risk_Likelihoods likelihood, String description, String effects, String safetyFeatures);
	
	Hazard_Causes updateRegularCause(int causeID, String title, String owner, Risk_Categories risk, 
			Risk_Likelihoods likelihood, String description, String effects, String safetyFeatures);
	
	Hazard_Causes updateTransferredCause(int causeID, String transferReason);
	
	List<Hazard_Causes> getAllCauses();
	
	List<Hazard_Causes> getAllCausesWithinAHazard(Hazards hazard);
	
	List<HazardCauseTransferDT> getAllTransferredCauses(Hazards hazard);
	
	Hazard_Causes getHazardCauseByID(int causeID);
	
	Hazard_Causes getHazardCauseByID(String causeID);
	
	Hazard_Causes[] getHazardCausesByID(Integer[] id);
	
	List<HazardControlDTMinimalJson> getAllNonDeletedControlsWithinCauseMinimalJson(int causeID);
	
	Hazard_Causes addHazardTransfer(int originHazardID, int targetHazardID, String transferReason);
	
	Hazard_Causes addCauseTransfer(int originHazardID, int targetCauseID, String transferReason);
	
	Hazard_Causes deleteCause(int causeID, String deleteReason);
	
	

	
	
	

	List<Hazard_Causes> getAllNonDeletedCausesWithinHazard(Hazards hazard);
}
