package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;

import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface RiskLikelihoodsService {
	Risk_Likelihoods add(String value, String riskDesc);
	Risk_Likelihoods getLikelihoodByID(String id);
	List<Risk_Likelihoods> all();
}
