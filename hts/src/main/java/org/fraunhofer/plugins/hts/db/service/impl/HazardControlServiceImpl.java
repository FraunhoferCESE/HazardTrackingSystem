package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.datatype.HazardControlTransfers;
import org.fraunhofer.plugins.hts.db.ControlGroups;
import org.fraunhofer.plugins.hts.db.ControlToCause;
import org.fraunhofer.plugins.hts.db.ControlToHazard;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Transfers;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardControlService;
import org.fraunhofer.plugins.hts.db.service.TransferService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class HazardControlServiceImpl implements HazardControlService {
	private final ActiveObjects ao;
	private final TransferService transferService;
	private final HazardCauseService hazardCauseService;
	
	public HazardControlServiceImpl(ActiveObjects ao, TransferService transferService, HazardCauseService hazardCauseService) {
		this.ao = checkNotNull(ao);
		this.transferService = checkNotNull(transferService);
		this.hazardCauseService = hazardCauseService;
	}

	@Override
	public Hazard_Controls add(Hazards hazard, String description, ControlGroups controlGroup, Hazard_Causes[] causes) {
		final Hazard_Controls control = ao.create(Hazard_Controls.class);
		control.setDescription(description);
		control.setControlGroup(controlGroup);
		if (causes != null) {
			for (Hazard_Causes hc : causes) {
				associateControlToCause(control, hc);
			}
		}
		control.setOriginalDate(new Date());
		control.setLastUpdated(null);
		control.setControlNumber(getNewControlNumber(hazard));
		control.save();
		associateControlToHazard(hazard, control);
		return control;
	}
	
	@Override
	public Hazard_Controls update(String controlID, String description, ControlGroups controlGroup, Hazard_Causes[] causes) {
		Hazard_Controls controlToBeUpdated = getHazardControlByID(controlID);
		if (!description.equals(controlToBeUpdated.getDescription())) {
			controlToBeUpdated.setDescription(description);
		}
		if (controlGroup.getID() != controlToBeUpdated.getControlGroup().getID()) {
			controlToBeUpdated.setControlGroup(controlGroup);
		}
		if (causes != null) {
			
			removeAssociationsControlToCause(controlToBeUpdated.getID());
			for (Hazard_Causes hc : causes) {
				associateControlToCause(controlToBeUpdated, hc);
			}
		}
		else {
			removeAssociationsControlToCause(controlToBeUpdated.getID());
		}
		controlToBeUpdated.setLastUpdated(new Date());
		controlToBeUpdated.save();
		return controlToBeUpdated;
	}
	
	@Override
	public Hazard_Controls updateTransferredControl(String controlID, String transferReason) {
		Hazard_Controls controlToBeUpdated = getHazardControlByID(controlID);
		if (!transferReason.equals(controlToBeUpdated.getDescription())) {
			controlToBeUpdated.setDescription(transferReason);
		}
		controlToBeUpdated.setLastUpdated(new Date());
		controlToBeUpdated.save();
		return controlToBeUpdated;
	}

	@Override
	public List<Hazard_Controls> getAllControlsWithinAHazard(Hazards hazard) {
		return newArrayList(hazard.getHazardControls());
	}
	
	@Override
	public List<Hazard_Controls> getAllNonDeletedControlsWithinAHazard(Hazards hazard) {
		List<Hazard_Controls> allRemaining = new ArrayList<Hazard_Controls>();
		for (Hazard_Controls current : getAllControlsWithinAHazard(hazard)) {
			if (current.getDeleteReason() == null) {
				allRemaining.add(current);
			}
		}
		return allRemaining;
	}
	
	@Override
	public List<HazardControlTransfers> getAllTransferredControls(Hazards hazard) {
		List<Hazard_Controls> allControls = getAllControlsWithinAHazard(hazard);
		List<Transfers> allTransfers = new ArrayList<Transfers>();
		for (Hazard_Controls control : allControls) {
			if (control.getTransfer() != 0) {
				allTransfers.add(transferService.getTransferByID(control.getTransfer()));
			}
		}
		
		List<HazardControlTransfers> transferInfo = new ArrayList<HazardControlTransfers>();
		for (Transfers transfer : allTransfers) {
			int transferID = transfer.getID();
			int targetID = transfer.getTargetID();
			String targetType = transfer.getTargetType();			
			Hazard_Controls originControl = getHazardControlByID(String.valueOf(transfer.getOriginID()));
			int originID = originControl.getID();
			String transferReason = originControl.getDescription();
			
			if (transfer.getTargetType().equals("CONTROL")) {
				Hazard_Controls targetControl = getHazardControlByID(String.valueOf(transfer.getTargetID()));
				Hazards targetHazard = targetControl.getHazard()[0];
				int targetHazardID = targetHazard.getID();
				String targetHazardNo = targetHazard.getHazardNum();
				String targetHazardTitle = targetHazard.getTitle();
				
				int targetHazardControlNo = targetControl.getControlNumber();
				String targetHazardControlDescription = targetControl.getDescription();
				
				HazardControlTransfers controlTransfer = new HazardControlTransfers();
				controlTransfer.setTransferID(transferID);
				controlTransfer.setTargetID(targetID);
				controlTransfer.setTargetType(targetType);
				controlTransfer.setOriginHazardControlTransferReason(transferReason);
				controlTransfer.setOriginID(originID);
				controlTransfer.setTargetHazardID(targetHazardID);
				controlTransfer.setTargetHazardNo(targetHazardNo);
				controlTransfer.setTargetHazardTitle(targetHazardTitle);
				controlTransfer.setTargetHazardControlNo(targetHazardControlNo);
				controlTransfer.setTargetHazardControlDescription(targetHazardControlDescription);
				transferInfo.add(controlTransfer);
			}
			
			if (transfer.getTargetType().equals("CAUSE")) {
				Hazard_Causes targetCause = hazardCauseService.getHazardCauseByID(String.valueOf(transfer.getTargetID()));
				Hazards targetHazard = targetCause.getHazards()[0];
				int targetHazardID = targetHazard.getID();
				String targetHazardNo = targetHazard.getHazardNum();
				String targetHazardTitle = targetHazard.getTitle();
				
				int targetHazardCauseNo = targetCause.getCauseNumber();
				String targetHazardCauseTitle = targetCause.getTitle();
				
				HazardControlTransfers causeTransfer = new HazardControlTransfers();
				causeTransfer.setTransferID(transferID);
				causeTransfer.setTargetID(targetID);
				causeTransfer.setTargetType(targetType);
				causeTransfer.setOriginHazardControlTransferReason(transferReason);
				causeTransfer.setOriginID(originID);
				causeTransfer.setTargetHazardID(targetHazardID);
				causeTransfer.setTargetHazardNo(targetHazardNo);
				causeTransfer.setTargetHazardTitle(targetHazardTitle);
				causeTransfer.setTargetHazardCauseNo(targetHazardCauseNo);
				causeTransfer.setTargetHazardCauseTitle(targetHazardCauseTitle);
				transferInfo.add(causeTransfer);
			}
		}
		
		return transferInfo;
	}
	
	@Override
	public Hazard_Controls deleteControl(Hazard_Controls controlToBeDeleted, String reason) {
		controlToBeDeleted.setDeleteReason(reason);
		if (controlToBeDeleted.getTransfer() != 0) {
			int transferID = controlToBeDeleted.getTransfer();
			controlToBeDeleted.setTransfer(0);
			removeTransfer(transferID);
		}
		controlToBeDeleted.save();
		return controlToBeDeleted;
	}

	
	@Override
	public Hazard_Controls addControlTransfer(String transferComment, int targetID, Hazards hazard) {
		Hazard_Controls control = add(hazard, transferComment, null, null);
		int transferID = createTransfer(control.getID(), "CONTROL", targetID, "CONTROL");
		control.setTransfer(transferID);
		control.save();
		return control;
	}
	
	@Override
	public Hazard_Controls addCauseTransfer(String transferComment, int targetID, Hazards hazard) {
		Hazard_Controls control = add(hazard, transferComment, null, null);
		int transferID = createTransfer(control.getID(), "CONTROL", targetID, "CAUSE");
		control.setTransfer(transferID);
		control.save();
		return control;
	}

	@Override
	public Hazard_Controls getHazardControlByID(String id) {
		final Hazard_Controls[] control = ao.find(Hazard_Controls.class, Query.select().where("ID=?", id));
		return control.length > 0 ? control[0] : null;
	}

	@Override
	public List<Hazard_Controls> all() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void associateControlToHazard(Hazards hazard, Hazard_Controls control) {
		final ControlToHazard controlToHazard = ao.create(ControlToHazard.class);
		controlToHazard.setHazard(hazard);
		controlToHazard.setControl(control);
		controlToHazard.save();
	}
	
	private void associateControlToCause(Hazard_Controls control, Hazard_Causes cause) {
		final ControlToCause controlToCause = ao.create(ControlToCause.class);
		controlToCause.setCause(cause);
		controlToCause.setControl(control);
		controlToCause.save();
	}
	
	private void removeAssociationsControlToCause(int id) {
		ao.delete(ao.find(ControlToCause.class, Query.select().where("CONTROL_ID=?", id)));
	}
	
	private int getNewControlNumber(Hazards hazard) {
		return hazard.getHazardControls().length + 1;
	}
	
	private int createTransfer(int originID, String originType, int targetID, String targetType) {
		Transfers transfer = transferService.add(originID, originType, targetID, targetType);
		return transfer.getID();
	}
	
	private void removeTransfer(int id) {
		ao.delete(ao.find(Transfers.class, Query.select().where("ID=?", id)));
	}
}
