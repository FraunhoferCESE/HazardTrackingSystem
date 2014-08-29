package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.datatype.TransferClass;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;

public interface HazardCauseService {
	Hazard_Causes add(String description, String effects, String safetyFeatures, 
			Risk_Categories risk, Risk_Likelihoods likelihood, String owner, String title, Hazards hazard);
	
	Hazard_Causes update(String id, String description, String effects, String safetyFeatures, 
			String owner, String title, Risk_Categories risk, Risk_Likelihoods likelihood);

	Hazard_Causes getHazardCauseByID(String id);
	
	Hazard_Causes[] getHazardCausesByID(Integer[] id);

	List<Hazard_Causes> all();

	List<Hazard_Causes> getAllCausesWithinAHazard(Hazards hazard);
	
	List<Hazard_Controls> getAllControlsWithinACause(Hazard_Causes cause);
	
	List<Hazard_Controls> getAllNonDeletedControlsWithinACause(Hazard_Causes cause);

	Hazard_Causes deleteCause(Hazard_Causes causeToBeDeleted, String reason);

	List<Hazard_Causes> getAllNonDeletedCausesWithinAHazard(Hazards hazard);

	Hazard_Causes addCauseTransfer(String transferComment, int targetID, String title, Hazards hazard);
	
	Hazard_Causes addHazardTransfer(String transferComment, int targetID, String title, Hazards hazard);
	
	List<TransferClass> getAllTransferredCauses(Hazards hazard);
}
