package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Risk_Categories;

import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface RiskCategoryService {
	Risk_Categories add(String value, String riskDesc);
	Risk_Categories getRiskByID(String id);
	List<Risk_Categories> all();
}
