package org.fraunhofer.plugins.hts.db.service.impl;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Review_Phases;
import org.fraunhofer.plugins.hts.db.service.ReviewPhaseService;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import com.atlassian.activeobjects.external.ActiveObjects;

public class ReviewPhaseServiceImpl implements ReviewPhaseService {
	private final ActiveObjects ao;

	private static boolean initialized = false;
	private static Object _lock;

	public ReviewPhaseServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
		synchronized (_lock) {
			if (!initialized) {
				initializeTables();
				initialized = true;
			}
		}
	}

	private void initializeTables() {
		// TODO Initialize database tables
		
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
		final Review_Phases[] reviewPhase = ao.find(Review_Phases.class, Query
				.select().where("ID=?", id));
		return reviewPhase.length > 0 ? reviewPhase[0] : null;
	}

	@Override
	public List<Review_Phases> all() {
		return newArrayList(ao.find(Review_Phases.class));
	}

}
