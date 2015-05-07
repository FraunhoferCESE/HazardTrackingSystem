package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.model.VerificationType;

import com.atlassian.activeobjects.external.ActiveObjects;

public class VerificationTypeService {

	private final ActiveObjects ao;
	private static boolean initialized = false;
	private static Object _lock = new Object();

	public VerificationTypeService(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	private void initializeTable() {
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

	private VerificationType add(String label) {
		final VerificationType verificationType = ao.create(VerificationType.class);
		verificationType.setLabel(label);
		verificationType.save();
		return verificationType;
	}

	public VerificationType getVerificationTypeByID(String id) {
		initializeTable();

		final VerificationType[] verificationTypeArr = ao
				.find(VerificationType.class, Query.select().where("ID=?", id));
		return verificationTypeArr.length > 0 ? verificationTypeArr[0] : null;
	}

	public List<VerificationType> all() {
		initializeTable();
		return newArrayList(ao.find(VerificationType.class));
	}
}
