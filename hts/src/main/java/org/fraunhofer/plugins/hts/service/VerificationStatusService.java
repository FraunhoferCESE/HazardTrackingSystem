package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.model.VerificationStatus;

import com.atlassian.activeobjects.external.ActiveObjects;

public class VerificationStatusService {

	private final ActiveObjects ao;
	private static boolean initialized = false;
	private static Object _lock = new Object();

	public VerificationStatusService(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	private void initializeTable() {
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

	private VerificationStatus add(String label) {
		final VerificationStatus verificationStatus = ao.create(VerificationStatus.class);
		verificationStatus.setLabel(label);
		verificationStatus.save();
		return verificationStatus;
	}

	public VerificationStatus getVerificationStatusByID(String id) {
		initializeTable();

		final VerificationStatus[] verificationStatusArr = ao.find(VerificationStatus.class,
				Query.select().where("ID=?", id));
		return verificationStatusArr.length > 0 ? verificationStatusArr[0] : null;
	}

	public List<VerificationStatus> all() {
		initializeTable();
		return newArrayList(ao.find(VerificationStatus.class));
	}
}
