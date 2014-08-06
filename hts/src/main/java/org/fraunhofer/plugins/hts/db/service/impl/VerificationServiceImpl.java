package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Verifications;
import org.fraunhofer.plugins.hts.db.service.VerificationService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class VerificationServiceImpl implements VerificationService {
	
	private final ActiveObjects ao;
	
	public VerificationServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	
	@Override
	public List<Verifications> all() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Verifications add(String description) {
		return null;
	}
	
	
	
}