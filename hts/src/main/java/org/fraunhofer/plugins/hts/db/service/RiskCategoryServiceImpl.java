package org.fraunhofer.plugins.hts.db.service;

import java.sql.SQLException;
import java.util.List;

import net.java.ao.Query;

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
		risk.save();
		return risk;
	}

	@Override
	public List<Risk_Categories> all() {
		return newArrayList(ao.find(Risk_Categories.class));
	}

	@Override
	//TODO error handling
	public Risk_Categories getRiskByID(String id) {
		final Risk_Categories[] risk = ao.find(Risk_Categories.class, Query.select().where("ID=?", id));
		return risk.length > 0 ? risk[0] : null;
	}

}
