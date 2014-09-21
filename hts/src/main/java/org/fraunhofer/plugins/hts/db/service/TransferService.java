package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Transfers;

public interface TransferService {
	Transfers add(int originID, String originType, int targetID, String targetType, boolean active);
	
	Transfers update();
	
	List<Transfers> all();
	
	Transfers getTransferByID(int id);
	
	boolean checkForActiveTransferTarget(int targetID, String targetType);
}
