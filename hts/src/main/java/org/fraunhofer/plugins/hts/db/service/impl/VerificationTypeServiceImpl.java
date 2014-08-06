package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.VerificationType;
import org.fraunhofer.plugins.hts.db.service.VerificationTypeService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class VerificationTypeServiceImpl implements VerificationTypeService {

	private final ActiveObjects ao;
	private static boolean initialized = false;
	private static Object _lock = new Object();
	
	public VerificationTypeServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	
	public void initializeTable() {
		synchronized (_lock) {
			if (!initialized) {
				if (ao.find(VerificationType.class).length == 0) {
					add("Test");
					add("Demonstration");
					add("Simulation");
					add("Analysis");
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
	public VerificationType add(String label) {
		final VerificationType verificationType = ao.create(VerificationType.class);
		verificationType.setLabel(label);
		verificationType.save();
		return verificationType;
	}

	@Override
	public VerificationType getVerificationTypeByID(String id) {
		initializeTable();
		
		final VerificationType[] verificationTypeArr = ao.find(VerificationType.class, Query.select().where("ID=?", id));
		return verificationTypeArr.length > 0 ? verificationTypeArr[0] : null;
	}

	@Override
	public List<VerificationType> all() {
		initializeTable();
		return newArrayList(ao.find(VerificationType.class));
	}
}
