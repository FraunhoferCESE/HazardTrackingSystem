package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Review_Phases;

public interface ReviewPhaseService {
	Review_Phases add(String value, String reviewPhaseDesc);
	Review_Phases getReviewPhaseByID(String id);
	List<Review_Phases> all();
}
