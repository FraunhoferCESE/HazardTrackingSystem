package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Review_Phases;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import com.atlassian.activeobjects.external.ActiveObjects;

public class ReviewPhaseServiceImpl implements ReviewPhaseService{
	private final ActiveObjects ao;
	
	public ReviewPhaseServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
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
		final Review_Phases[] reviewPhase = ao.find(Review_Phases.class, Query.select().where("ID=?", id));
		return reviewPhase.length > 0 ? reviewPhase[0] : null;
	}

	@Override
	public List<Review_Phases> all() {
		return newArrayList(ao.find(Review_Phases.class));
	}
	
	

}
