package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.model.Review_Phases;

import com.atlassian.activeobjects.external.ActiveObjects;

public class ReviewPhaseService {
	private final ActiveObjects ao;

	private static boolean initialized = false;

	private static Object _lock = new Object();

	public ReviewPhaseService(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	private void initializeTable() {
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

	private Review_Phases add(String label, String reviewPhaseDesc) {
		final Review_Phases reviewPhase = ao.create(Review_Phases.class);
		reviewPhase.setLabel(label);
		reviewPhase.setDescription(reviewPhaseDesc);
		reviewPhase.save();
		return reviewPhase;
	}

	public Review_Phases getReviewPhaseByID(String id) {
		initializeTable();

		final Review_Phases[] reviewPhase = ao.find(Review_Phases.class, Query.select().where("ID=?", id));
		return reviewPhase.length > 0 ? reviewPhase[0] : null;
	}

	public List<Review_Phases> all() {
		initializeTable();
		return newArrayList(ao.find(Review_Phases.class));
	}
}
