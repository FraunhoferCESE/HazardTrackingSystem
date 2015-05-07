package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.DBParam;
import net.java.ao.Query;

import org.fraunhofer.plugins.hts.model.Transfers;
import org.fraunhofer.plugins.hts.model.Transfers.TransferType;

import com.atlassian.activeobjects.external.ActiveObjects;

public class TransferService {
	private final ActiveObjects ao;

	public TransferService(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	public Transfers add(int originID, String originType, int targetID, String targetType) {
		final Transfers transfer = ao.create(Transfers.class, new DBParam("ORIGIN_ID", originID), new DBParam(
				"ORIGIN_TYPE", originType), new DBParam("TARGET_ID", targetID), new DBParam("TARGET_TYPE", targetType));
		transfer.save();
		return transfer;
	}

	public List<Transfers> all() {
		return newArrayList(ao.find(Transfers.class));
	}

	public Transfers getTransferByID(int id) {
		final Transfers[] transfer = ao.find(Transfers.class, Query.select().where("ID=?", id));
		return transfer.length > 0 ? transfer[0] : null;
	}

	/**
	 * Finds all transfers to specified IDs.
	 * 
	 * @param type
	 *            the type to search for, i.e., hazard, cause, or control
	 * @param id
	 *            the hazard, cause, or control id to check for incoming
	 *            transfers
	 * @return a list of Transfer objects for which <code>id</code> is the
	 *         target.
	 */
	public List<Transfers> getOriginsForId(String type, int id) {
		checkArgument(type != null && TransferType.valueOf(type.toUpperCase()) != null && id >= 0);
		return newArrayList(ao.find(Transfers.class, "TARGET_ID = ? AND TARGET_TYPE = ?", id, type.toUpperCase()));
	}

}
