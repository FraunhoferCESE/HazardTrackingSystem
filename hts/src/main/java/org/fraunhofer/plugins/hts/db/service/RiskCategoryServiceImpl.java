package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Risk_Categories;

import com.atlassian.activeobjects.external.ActiveObjects;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class RiskCategoryServiceImpl implements RiskCategoryService {
	private final ActiveObjects ao;
	
	public RiskCategoryServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	
	@Override
	public Risk_Categories add(String value, String riskDesc) {
		final Risk_Categories risk = ao.create(Risk_Categories.class);
		risk.setValue(value);
		risk.setRiskDesc(riskDesc);
		return risk;
	}

	@Override
	public List<Risk_Categories> all() {
		return newArrayList(ao.find(Risk_Categories.class));
	}

}
