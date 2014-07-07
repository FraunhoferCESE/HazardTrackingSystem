package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.datatype.TransferClass;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;

public interface HazardCauseService {
	Hazard_Causes add(String description, String effects, String owner, String title, Hazards hazard);

	Hazard_Causes update(String id, String description, String effects, String owner, String title);

	Hazard_Causes getHazardCauseByID(String id);

	List<Hazard_Causes> all();

	List<Hazard_Causes> getAllCausesWithinAHazard(Hazards hazard);

	Hazard_Causes deleteCause(Hazard_Causes causeToBeDeleted, String reason);

	List<Hazard_Causes> getAllNonDeletedCausesWithinAHazard(Hazards hazard);

	Hazard_Causes addCauseTransfer(String transferComment, int targetID, String title, Hazards hazard);
	
	Hazard_Causes addHazardTransfer(String transferComment, int targetID, String title, Hazards hazard);
	
	List<TransferClass> getAllTransferredCauses(Hazards hazard);
}
