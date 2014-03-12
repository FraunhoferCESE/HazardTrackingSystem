package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;

import com.atlassian.activeobjects.external.ActiveObjects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class MissionPayloadServiceImpl implements MissionPayloadService {
	private final ActiveObjects ao;
	
	public MissionPayloadServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	@Override
	public Mission_Payload add(Hazards hazard, String name) {
		final Mission_Payload missionPayload = ao.create(Mission_Payload.class);
		missionPayload.setHazard(hazard);
		missionPayload.setName(name);
		missionPayload.save();
		return missionPayload;
	}

	@Override
	public Mission_Payload getMissionPayloadByID(String id) {
		final Mission_Payload[] missionPayload = ao.find(Mission_Payload.class, Query.select().where("ID=?", id));
		return missionPayload.length > 0 ? missionPayload[0] : null;
	}
	
	@Override
	public Mission_Payload update(Mission_Payload payloadToUpdate, String name) {
		payloadToUpdate.setName(name);
		payloadToUpdate.save();
		return payloadToUpdate;
	}

	@Override
	public List<Mission_Payload> all() {
		return newArrayList(ao.find(Mission_Payload.class));
	}
}
