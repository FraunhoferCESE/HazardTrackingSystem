package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.java.ao.DBParam;
import net.java.ao.Query;

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
	public Hazard_Causes add(String description, String effects, String safetyFeatures, Risk_Categories risk, 
			Risk_Likelihoods likelihood, String owner, String title, Hazards hazard) {
		final Hazard_Causes cause = ao.create(Hazard_Causes.class, new DBParam("TITLE", title));
		cause.setCauseNumber(getNewCauseNumber(hazard));
		cause.setDescription(description);
		cause.setEffects(effects);
		cause.setAdditionalSafetyFeatures(safetyFeatures);
		cause.setOwner(owner);
		cause.setRiskCategory(risk);
		cause.setRiskLikelihood(likelihood);
		cause.setLastUpdated(new Date());
		cause.setOriginalDate(new Date());
		cause.setTransfer(0);
		cause.save();
		associateCauseToHazard(hazard, cause);
		return cause;
	}

	@Override
	public Hazard_Causes addCauseTransfer(String transferComment, int targetID, String title, Hazards hazard) {
		Hazard_Causes cause = add(transferComment, null, null, null, null, null, title, hazard);
		int transferID = createTransfer(cause.getID(), "CAUSE", targetID, "CAUSE");
		cause.setTransfer(transferID);
		cause.save();
		return cause;
	}
	
	@Override
	public Hazard_Causes addHazardTransfer(String transferComment, int targetID, String title, Hazards hazard) {
		Hazard_Causes cause = add(transferComment, null, null, null, null, null, title, hazard);
		int transferID = createTransfer(cause.getID(), "CAUSE", targetID, "HAZARD");
		cause.setTransfer(transferID);
		cause.save();
		return cause;
	}

	@Override
	public Hazard_Causes update(String id, String description, String safetyFeatures, String effects, 
			String owner, String title, Risk_Categories risk, Risk_Likelihoods likelihood) {
		Hazard_Causes causeToBeUpdated = getHazardCauseByID(id);
		causeToBeUpdated.setDescription(description);
		causeToBeUpdated.setEffects(effects);
		causeToBeUpdated.setAdditionalSafetyFeatures(safetyFeatures);
		causeToBeUpdated.setOwner(owner);
		causeToBeUpdated.setTitle(title);
		
		if (risk != null && causeToBeUpdated.getRiskCategory() != null) {
			if (risk.getID() != causeToBeUpdated.getRiskCategory().getID()) {
				causeToBeUpdated.setRiskCategory(risk);
			}
		}
		else {
			causeToBeUpdated.setRiskCategory(risk);
		}
		
		if (likelihood != null && causeToBeUpdated.getRiskLikelihood() != null) {
			if (likelihood.getID() != causeToBeUpdated.getRiskLikelihood().getID()) {
				causeToBeUpdated.setRiskLikelihood(likelihood);
			}
		}
		else {
			causeToBeUpdated.setRiskLikelihood(likelihood);
		}
		
		causeToBeUpdated.setLastUpdated(new Date());
		causeToBeUpdated.save();
		return causeToBeUpdated;
	}

	@Override
	public Hazard_Causes getHazardCauseByID(String id) {
		final Hazard_Causes[] hazardCause = ao.find(Hazard_Causes.class, Query.select().where("ID=?", id));
		return hazardCause.length > 0 ? hazardCause[0] : null;
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
	public List<Hazard_Causes> all() {
		return newArrayList(ao.find(Hazard_Causes.class));
	}

	@Override
	public List<Hazard_Causes> getAllCausesWithinAHazard(Hazards hazard) {
		return newArrayList(hazard.getHazardCauses());
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
	public List<TransferClass> getAllTransferredCauses(Hazards hazard) {
		List<Hazard_Causes> allCauses = getAllCausesWithinAHazard(hazard);
		List<Transfers> allTransfers = new ArrayList<Transfers>();
		for (Hazard_Causes cause : allCauses) {
			if (cause.getTransfer() != 0) {
				allTransfers.add(transferService.getTransferByID(cause.getTransfer()));
			}
		}
	
		List<TransferClass> transferInfo = new ArrayList<TransferClass>();
		for (Transfers transfer : allTransfers) {
			Hazard_Causes originCause = getHazardCauseByID(String.valueOf(transfer.getOriginID()));
			String transferReason = originCause.getDescription();
			int transferID = transfer.getID();
			String targetType = transfer.getTargetType();
			int targetID = transfer.getTargetID();
			
			if (transfer.getTargetType().equals("CAUSE")) {
				Hazard_Causes transferredCause = getHazardCauseByID(String.valueOf(transfer.getTargetID()));
				String causeTitle = transferredCause.getTitle();
				int causeNum = transferredCause.getCauseNumber();
				String hazardTitle = transferredCause.getHazards()[0].getTitle();
				String hazardNumb = transferredCause.getHazards()[0].getHazardNum();
				
				Risk_Categories category = transferredCause.getRiskCategory();
				String categoryStr;
				if (category == null) {
					categoryStr = "Not specified";
				}
				else {
					categoryStr = category.getValue();
				}
				
				Risk_Likelihoods likelihood = transferredCause.getRiskLikelihood();
				String likelihoodStr;
				if (likelihood == null) {
					likelihoodStr = "Not specified";
				}
				else {
					likelihoodStr = likelihood.getValue();
				}
				
				Boolean deleted = false;				
				if (!Strings.isNullOrEmpty(transferredCause.getDeleteReason())) {
					deleted = true;
				}
				
				//This is the id of the hazard the cause belongs to, which is needed for the title navigation.
				int hazardID = transferredCause.getHazards()[0].getID();
				TransferClass causeTransfer = new TransferClass(transferID, transferReason, causeTitle, hazardNumb, hazardTitle,
										targetType, hazardID, targetID, causeNum, categoryStr, likelihoodStr, deleted);
				transferInfo.add(causeTransfer);
			}
			else if(transfer.getTargetType().equals("HAZARD")) {
				//get the hazard name and number
				//get reason
				Hazards transferredHazard = hazardService.getHazardByID(String.valueOf(transfer.getTargetID()));
				String hazardTitle = transferredHazard.getTitle();
				String hazardNumb = transferredHazard.getHazardNum();
				Boolean deleted = false;		
				if (transferredHazard.getActive() == false) {
					deleted = true;
				}
	
				TransferClass hazardTransfer = new TransferClass(transferID, transferReason, hazardTitle, hazardNumb, 
										targetType, targetID, deleted);
				transferInfo.add(hazardTransfer);
			}
		}
		return transferInfo;
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

	@Override
	public Hazard_Causes deleteCause(Hazard_Causes causeToBeDeleted, String reason) {
		causeToBeDeleted.setDeleteReason(reason);
		if (causeToBeDeleted.getTransfer() != 0) {
			Transfers transfer = transferService.getTransferByID(causeToBeDeleted.getTransfer());
			removeTransfer(transfer.getID());
			transfer.save();
			causeToBeDeleted.setTransfer(0);
		}
		causeToBeDeleted.save();
		return causeToBeDeleted;
	}

	private void associateCauseToHazard(Hazards hazard, Hazard_Causes hazardCause) {
		final CausesToHazards causeToHazard = ao.create(CausesToHazards.class);
		causeToHazard.setHazard(hazard);
		causeToHazard.setHazardCause(hazardCause);
		causeToHazard.save();
	}

	private int getNewCauseNumber(Hazards hazard) {
		return hazard.getHazardCauses().length + 1;
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

