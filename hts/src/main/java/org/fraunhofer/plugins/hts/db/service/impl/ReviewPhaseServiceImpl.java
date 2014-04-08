package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.service.ReviewPhaseService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class ReviewPhaseServiceImpl implements ReviewPhaseService {
	private final ActiveObjects ao;

	private static boolean initialized = false;

	private static Object _lock = new Object();

	public ReviewPhaseServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	public void initializeTable() {
		synchronized (_lock) {
			if (!initialized) {
				if (ao.find(Review_Phases.class).length == 0) {
					add("Phase I", "Phase I safety review");
					add("Phase II", "Phase II safety review");
					add("Phase III", "Phase III safety review");
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
	public Review_Phases add(String label, String reviewPhaseDesc) {
		final Review_Phases reviewPhase = ao.create(Review_Phases.class);
		reviewPhase.setLabel(label);
		reviewPhase.setDescription(reviewPhaseDesc);
		reviewPhase.save();
		return reviewPhase;
	}

	@Override
	public Review_Phases getReviewPhaseByID(String id) {
		initializeTable();

		final Review_Phases[] reviewPhase = ao.find(Review_Phases.class, Query.select().where("ID=?", id));
		return reviewPhase.length > 0 ? reviewPhase[0] : null;
	}

	@Override
	public List<Review_Phases> all() {
		initializeTable();
		return newArrayList(ao.find(Review_Phases.class));
	}

}
