package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Transfers;
import org.fraunhofer.plugins.hts.db.service.TransferService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class TransferServiceImpl implements TransferService {
	private final ActiveObjects ao;
	
	public TransferServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	@Override
	public Transfers addCause(int originID, String originType, int targetID, String targetType) {
		final Transfers transfer = ao.create(Transfers.class);
		transfer.setOriginID(originID);
		transfer.setOriginType(originType);
		transfer.setTargetID(targetID);
		transfer.setTargetType(targetType);
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

}
