package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.VerificationStatus;
import org.fraunhofer.plugins.hts.db.service.VerificationStatusService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class VerificationStatusServiceImpl implements VerificationStatusService {

	private final ActiveObjects ao;
	private static boolean initialized = false;
	private static Object _lock = new Object();
	
	public VerificationStatusServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	
	public void initializeTable() {
		synchronized (_lock) {
			if (!initialized) {
				if (ao.find(VerificationStatus.class).length == 0) {
					add("Open");
					add("Open to Safety Tracking List");
					add("Closed");
				}
				initialized = true;
			}
		}
	}
	
	public static boolean isInitialized() {
		synchronized (_lock) {
			return initialized;
		}
	}
	
	public static void reset() {
		synchronized (_lock) {
			initialized = false;
		}
	}
	
	@Override
	public VerificationStatus add(String label) {
		final VerificationStatus verificationStatus = ao.create(VerificationStatus.class);
		verificationStatus.setLabel(label);
		verificationStatus.save();
		return verificationStatus;
	}

	@Override
	public VerificationStatus getVerificationStatusByID(String id) {
		initializeTable();
		
		final VerificationStatus[] verificationStatusArr = ao.find(VerificationStatus.class, Query.select().where("ID=?", id));
		return verificationStatusArr.length > 0 ? verificationStatusArr[0] : null;
	}

	@Override
	public List<VerificationStatus> all() {
		initializeTable();
		return newArrayList(ao.find(VerificationStatus.class));
	}
}
