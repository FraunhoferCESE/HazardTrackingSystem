package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.java.ao.DBParam;
import net.java.ao.Query;

import org.fraunhofer.plugins.hts.datatype.HazardCauseTransferDT;
import org.fraunhofer.plugins.hts.datatype.TransferClass;
import org.fraunhofer.plugins.hts.db.CausesToHazards;
import org.fraunhofer.plugins.hts.db.Hazard_Causes;
import org.fraunhofer.plugins.hts.db.Hazard_Controls;
import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.Risk_Categories;
import org.fraunhofer.plugins.hts.db.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.db.Transfers;
import org.fraunhofer.plugins.hts.db.service.HazardCauseService;
import org.fraunhofer.plugins.hts.db.service.HazardService;
import org.fraunhofer.plugins.hts.db.service.TransferService;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.google.common.base.Strings;

public class HazardCauseServiceImpl implements HazardCauseService {
	private final ActiveObjects ao;
	private final TransferService transferService;
	private final HazardService hazardService;

	public HazardCauseServiceImpl(ActiveObjects ao, TransferService transferService, HazardService hazardService) {
		this.ao = checkNotNull(ao);
		this.transferService = checkNotNull(transferService);
		this.hazardService = checkNotNull(hazardService);
	}
	
	@Override
	public Hazard_Causes add(int hazardID, String title, String owner, Risk_Categories risk, 
			Risk_Likelihoods likelihood, String description, String effects, String safetyFeatures) {
		Hazard_Causes cause = ao.create(Hazard_Causes.class, new DBParam("TITLE", title));
		Hazards hazard = hazardService.getHazardByID(hazardID);
		cause.setCauseNumber(hazard.getHazardCauses().length + 1);
		cause.setTransfer(0);
		cause.setOwner(owner);
		cause.setRiskCategory(risk);
		cause.setRiskLikelihood(likelihood);
		cause.setDescription(description);
		cause.setEffects(effects);
		cause.setAdditionalSafetyFeatures(safetyFeatures);
		cause.setLastUpdated(new Date());
		cause.save();
		associateCauseToHazard(hazard, cause);
		return cause;
	}
	
	@Override
	public Hazard_Causes updateRegularCause(int causeID, String title, String owner, Risk_Categories risk,
			Risk_Likelihoods likelihood, String description, String effects, String safetyFeatures) {
		Hazard_Causes cause = getHazardCauseByID(causeID);
		cause.setTitle(title);
		cause.setOwner(owner);
		
		if (risk != null && cause.getRiskCategory() != null) {
			if (risk.getID() != cause.getRiskCategory().getID()) {
				cause.setRiskCategory(risk);
			}
		} else {
			cause.setRiskCategory(risk);
		}
		
		if (likelihood != null && cause.getRiskLikelihood() != null) {
			if (likelihood.getID() != cause.getRiskLikelihood().getID()) {
				cause.setRiskLikelihood(likelihood);
			}
		} else {
			cause.setRiskLikelihood(likelihood);
		}
		
		cause.setDescription(description);
		cause.setEffects(effects);
		cause.setAdditionalSafetyFeatures(safetyFeatures);
		cause.setLastUpdated(new Date());
		cause.save();
		return cause;
	}
	
	public Hazard_Causes updateTransferredCause(int causeID, String transferReason) {
		Hazard_Causes cause = getHazardCauseByID(causeID);
		cause.setDescription(transferReason);
		cause.setLastUpdated(new Date());
		cause.save();
		return cause;
	}
	
	@Override
	public List<Hazard_Causes> getAllCauses() {
		return newArrayList(ao.find(Hazard_Causes.class));
	}
	
	@Override
	public List<Hazard_Causes> getAllCausesWithinAHazard(Hazards hazard) {
		return newArrayList(hazard.getHazardCauses());
	}
	
	@Override
	public List<HazardCauseTransferDT> getAllTransferredCauses(Hazards hazard) {
		List<HazardCauseTransferDT> transferredCauses = new ArrayList<HazardCauseTransferDT>();
		List<Hazard_Causes> allCausesWithinHazard = getAllCausesWithinAHazard(hazard);
		for (Hazard_Causes originCause : allCausesWithinHazard) {
			if (originCause.getTransfer() != 0) {
				Transfers transfer = transferService.getTransferByID(originCause.getTransfer());
				if (transfer.getTargetType().equals("CAUSE")) {
					// CauseToCause transfer
					Hazard_Causes targetCause = getHazardCauseByID(transfer.getTargetID());
					transferredCauses.add(HazardCauseTransferDT.createCauseToCause(transfer, originCause, targetCause));
				} else {
					// CauseToHazard transfer
					Hazards targetHazard = hazardService.getHazardByID(transfer.getTargetID());
					transferredCauses.add(HazardCauseTransferDT.createCauseToHazard(transfer, originCause, targetHazard));
				}
			}
		}
		return transferredCauses;
	}
	
	@Override
	public Hazard_Causes getHazardCauseByID(int causeID) {
		final Hazard_Causes[] hazardCause = ao.find(Hazard_Causes.class, Query.select().where("ID=?", causeID));
		return hazardCause.length > 0 ? hazardCause[0] : null;
	}
	
	@Override
	public Hazard_Causes getHazardCauseByID(String causeID) {
		int causeIDInt = Integer.parseInt(causeID);
		return getHazardCauseByID(causeIDInt);
	}
	
	@Override
	public Hazard_Causes addHazardTransfer(int originHazardID, int targetHazardID, String transferReason) {
		//Hazards targetHazard = hazardService.getHazardByID(targetHazardID);
		Hazard_Causes cause = add(originHazardID, "transfer", null, null, null, transferReason, null, null);
		int transferID = createTransfer(cause.getID(), "CAUSE", targetHazardID, "HAZARD");
		cause.setTransfer(transferID);
		cause.save();
		return cause;
	}
	
	@Override
	public Hazard_Causes addCauseTransfer(int originHazardID, int targetCauseID, String transferReason) {
		//Hazard_Causes targetCause = getHazardCauseByID(targetCauseID);
		Hazard_Causes cause = add(originHazardID, "transfer", null, null, null, transferReason, null, null);
		int transferID = createTransfer(cause.getID(), "CAUSE", targetCauseID, "CAUSE");
		cause.setTransfer(transferID);
		cause.save();
		return cause;
	}

	@Override
	public Hazard_Causes deleteCause(int causeID, String deleteReason) {
		Hazard_Causes cause = getHazardCauseByID(causeID);
		if (cause != null) {
			cause.setDeleteReason(deleteReason);
			if (cause.getTransfer() != 0) {
				Transfers transfer = transferService.getTransferByID(cause.getTransfer());
				removeTransfer(transfer.getID());
				transfer.save();
				cause.setTransfer(0);
			}
			cause.save();
		}
		return cause;
	}
	
	
	
	
	
	
	
	




	
	@Override
	public Hazard_Causes[] getHazardCausesByID(Integer[] id) {
		if (id == null) {
			return null;
		} else {
			Hazard_Causes[] causesArr = new Hazard_Causes[id.length];
			for (int i = 0; i < id.length; i++) {
				causesArr[i] = ao.get(Hazard_Causes.class, id[i]);
			}
			return causesArr;
		}
	}
	
	@Override
	public List<Hazard_Controls> getAllControlsWithinACause(Hazard_Causes cause) {
		return newArrayList(cause.getControls());
	}
	
	@Override
	public List<Hazard_Controls> getAllNonDeletedControlsWithinACause(Hazard_Causes cause) {
		List<Hazard_Controls> allRemaining = new ArrayList<Hazard_Controls>();
		for (Hazard_Controls control : getAllControlsWithinACause(cause)) {
			if (control.getDeleteReason() == null) {
				allRemaining.add(control);
			}
		}
		return allRemaining;
	}

	@Override
	public List<Hazard_Causes> getAllNonDeletedCausesWithinAHazard(Hazards hazard) {
		List<Hazard_Causes> allRemaining = new ArrayList<Hazard_Causes>();
		for (Hazard_Causes current : getAllCausesWithinAHazard(hazard)) {
			if (Strings.isNullOrEmpty(current.getDeleteReason())) {
				allRemaining.add(current);
			}
		}
		return allRemaining;
	}

	private void associateCauseToHazard(Hazards hazard, Hazard_Causes hazardCause) {
		final CausesToHazards causeToHazard = ao.create(CausesToHazards.class);
		causeToHazard.setHazard(hazard);
		causeToHazard.setHazardCause(hazardCause);
		causeToHazard.save();
	}
	
	private int createTransfer(int originID, String originType, int targetID, String targetType) {
		Transfers transfer = transferService.add(originID, originType, targetID, targetType);
		return transfer.getID();
	}
	
	private int checkForInactiveTransfer(int originID, String originType, int targetID, String targetType) {
		int rtn = 0;
		List<Transfers> allTransfers = transferService.all();
		for (Transfers currentTransfer : allTransfers) {
			if (currentTransfer.getTargetID() == targetID &&
				currentTransfer.getOriginType() == originType &&
				currentTransfer.getTargetType() == targetType) {
					rtn = currentTransfer.getID();
					currentTransfer.setOriginID(originID);
					currentTransfer.save();
					break;
			}
		}
		return rtn;
	}
	
	private void removeTransfer(int id) {
		ao.delete(ao.find(Transfers.class, Query.select().where("ID=?", id)));
	}

}

