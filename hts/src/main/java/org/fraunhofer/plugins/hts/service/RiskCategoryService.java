package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.model.Risk_Categories;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public class RiskCategoryService {
	private final ActiveObjects ao;

	private static boolean initialized = false;
	private static Object _lock = new Object();

	public RiskCategoryService(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	private Risk_Categories add(String value, String riskDesc) {
		final Risk_Categories risk = ao.create(Risk_Categories.class);
		risk.setValue(value);
		risk.setRiskDesc(riskDesc);
		risk.save();
		return risk;
	}

	public List<Risk_Categories> all() {
		initializeTable();
		return newArrayList(ao.find(Risk_Categories.class));
	}

	// TODO error handling
	public Risk_Categories getRiskByID(String id) {
		initializeTable();
		final Risk_Categories[] risk = ao.find(Risk_Categories.class, Query.select().where("ID=?", id));
		return risk.length > 0 ? risk[0] : null;
	}

	private void initializeTable() {
		synchronized (_lock) {
			if (!initialized) {
				if (ao.find(Risk_Categories.class).length == 0) {
					add("I - Catastrophic",
							"May cause death, equipment loss > $1M, unit downtime > 4 month, data is never recoverable or primary program objectives are lost");
					add("II - Critical",
							"May  cuase severe injury or severe occupational illness, equipment loss 200K-1M, unit downtime 2 weeks to 4 months, may cause repeat of test program");
					add("III - Marginal",
							"May cause minor injury or minor occupational illness, equipment loss 10K to 200K, unit downtime 1 day to 2 weeks, may cause repeat of test period");
					add("IV - Negligible",
							"Will not result in injury or occupational illness, equipment loss < 10K, unit downtime < 1 day, may cause repeat of data point or data may require minor manipulation or computer rerun");
				}
				initialized = true;
			}
		}
	}
}
