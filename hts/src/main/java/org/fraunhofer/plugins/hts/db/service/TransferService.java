package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Transfers;

public interface TransferService {
	Transfers add(int originID, String originType, int targetID, String targetType);

	List<Transfers> all();

	Transfers getTransferByID(int id);

	/**
	 * Finds all transfers to specified IDs. 
	 * 
	 * @param type the type to search for, i.e., hazard, cause, or control
	 * @param id the hazard, cause, or control id to check for incoming transfers
	 * @return a list of Transfer objects for which <code>id</code> is the target.
	 */
	List<Transfers> getOriginsForId(String type, int ids);

}
