package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.DBParam;
import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Transfers;
import org.fraunhofer.plugins.hts.db.service.TransferService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class TransferServiceImpl implements TransferService {
	private final ActiveObjects ao;
	
	public TransferServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	@Override
	public Transfers add(int originID, String originType, int targetID, String targetType) {
		final Transfers transfer = ao.create(Transfers.class, new DBParam("ORIGIN_ID", originID), 
				new DBParam("ORIGIN_TYPE", originType), new DBParam("TARGET_ID", targetID), 
				new DBParam("TARGET_TYPE", targetType));
		transfer.save();
		return transfer;
	}

	@Override
	public Transfers update() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Transfers> all() {
		return newArrayList(ao.find(Transfers.class));
	}

	@Override
	public Transfers getTransferByID(int id) {
		final Transfers[] transfer = ao.find(Transfers.class, Query.select().where("ID=?", id));
		return transfer.length > 0 ? transfer[0] : null;
	}
	
	public boolean checkForActiveTransferTarget(int targetID, String targetType) {
		final Transfers[] transfers = ao.find(Transfers.class, Query.select().where("TARGET_ID=? AND TARGET_TYPE=? AND ACTIVE=?", targetID, targetType, true));
		return transfers.length > 0 ? true : false;
	}

}
