package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.db.service.RiskLikelihoodsService;

import com.atlassian.activeobjects.external.ActiveObjects;

//TODO figure out if it is possible to create one basic function accepting objects and then figuring on which object it uses(RiskLikelihood, Subsystems etc..)
public class RiskLikelihoodsServiceImpl implements RiskLikelihoodsService {
	private final ActiveObjects ao;

	private static boolean initialized = false;
	private static Object _lock = new Object();

	public RiskLikelihoodsServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	@Override
	public Risk_Likelihoods add(String value, String likeliHoodDesc) {
		final Risk_Likelihoods likelihood = ao.create(Risk_Likelihoods.class);
		likelihood.setValue(value);
		likelihood.setLikelihoodDesc(likeliHoodDesc);
		likelihood.save();
		return likelihood;
	}

	@Override
	public List<Risk_Likelihoods> all() {
		initializeTable();
		return newArrayList(ao.find(Risk_Likelihoods.class));
	}

	@Override
	public Risk_Likelihoods getLikelihoodByID(String id) {
		initializeTable();
		final Risk_Likelihoods[] likelihood = ao.find(Risk_Likelihoods.class, Query.select().where("ID=?", id));
		return likelihood.length > 0 ? likelihood[0] : null;
	}

	public void initializeTable() {
		synchronized (_lock) {
			if (!initialized) {
				if (ao.find(Risk_Likelihoods.class).length == 0) {
					add("A - Frequent", "Likely to occur repeatedly");
					add("B - Reasonably probable", "Likely to occur several times");
					add("C - Occasional", "Likely to occur sometime");
					add("D - Remote", "Unlikely to occur, but possible");
					add("E - Extremely improbable", "The probability of occurence cannot be distinguished from zero");
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

}
