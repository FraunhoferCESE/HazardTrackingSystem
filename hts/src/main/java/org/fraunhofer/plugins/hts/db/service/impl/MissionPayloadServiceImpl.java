package org.fraunhofer.plugins.hts.db.service.impl;

import java.util.ArrayList;
import java.util.List;

import net.java.ao.DBParam;
import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Mission_Payload;
import org.fraunhofer.plugins.hts.db.service.MissionPayloadService;

import com.atlassian.activeobjects.external.ActiveObjects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class MissionPayloadServiceImpl implements MissionPayloadService {
	private final ActiveObjects ao;
	
	public MissionPayloadServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	@Override
	public Mission_Payload add(String name) {
		final Mission_Payload missionPayload = ao.create(Mission_Payload.class, new DBParam("NAME", name));
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

	@Override
	public void deleteMissionPayload(int id) {
		ao.delete(ao.find(Mission_Payload.class, Query.select().where("ID=?", id)));
	}

	@Override
	public Boolean payloadNameExists(String payloadName) {
		final Mission_Payload[] missionPayload = ao.find(Mission_Payload.class,
				Query.select().where("NAME=?", payloadName));
		return missionPayload.length > 0 ? true : false;
	}

	@Override
	public Mission_Payload getMissionPayloadByName(String name) {
		final Mission_Payload[] missionPayload = ao.find(Mission_Payload.class, Query.select().where("NAME=?", name));
		return missionPayload.length > 0 ? missionPayload[0] : null;
	}

	@Override
	public List<Hazards> getAllHazardsWithinMission(String id) {
		Mission_Payload payload = getMissionPayloadByID(id);
		Hazards[] allHazards = payload.getHazards();
		
		List<Hazards> nonDeleteHarzards = new ArrayList<Hazards>();
		for (Hazards current : allHazards) {
			if (current.getActive() == true) {
				nonDeleteHarzards.add(current);
			}
		}
				
		return nonDeleteHarzards;
	}
}
