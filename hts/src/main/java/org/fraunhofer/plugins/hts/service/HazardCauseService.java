package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.java.ao.DBParam;
import net.java.ao.Query;

import org.fraunhofer.plugins.hts.model.CausesToHazards;
import org.fraunhofer.plugins.hts.model.Hazard_Causes;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Risk_Categories;
import org.fraunhofer.plugins.hts.model.Risk_Likelihoods;
import org.fraunhofer.plugins.hts.model.Transfers;
import org.fraunhofer.plugins.hts.rest.model.CauseJSON;
import org.fraunhofer.plugins.hts.view.model.HazardCauseTransfer;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.google.common.base.Strings;

public class HazardCauseService {
	private final ActiveObjects ao;
	private final TransferService transferService;
	private final HazardService hazardService;

	public HazardCauseService(ActiveObjects ao, TransferService transferService, HazardService hazardService) {
		this.ao = checkNotNull(ao);
		this.transferService = checkNotNull(transferService);
		this.hazardService = checkNotNull(hazardService);
	}

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
		cause.setOriginalDate(new Date());
		cause.setLastUpdated(new Date());
		cause.save();
		associateCauseToHazard(hazard, cause);
		return cause;
	}

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

//	public List<Hazard_Causes> getAllCausesWithinAHazard(Hazards hazard) {
//		return newArrayList(hazard.getHazardCauses());
//	}

	public List<Hazard_Causes> getAllNonDeletedCausesWithinHazard(Hazards hazard) {
		List<Hazard_Causes> allRemaining = new ArrayList<Hazard_Causes>();
		for (Hazard_Causes current : hazard.getHazardCauses()) {
			if (Strings.isNullOrEmpty(current.getDeleteReason())) {
				allRemaining.add(current);
			}
		}
		return allRemaining;
	}

	public List<HazardCauseTransfer> getAllTransferredCauses(Hazards hazard) {
		List<HazardCauseTransfer> transferredCauses = new ArrayList<HazardCauseTransfer>();
		for (Hazard_Causes originCause : hazard.getHazardCauses()) {
			if (originCause.getTransfer() != 0) {
				Transfers transfer = transferService.getTransferByID(originCause.getTransfer());
				if (transfer.getTargetType().equals("CAUSE")) {
					// CauseToCause transfer
					Hazard_Causes targetCause = getHazardCauseByID(transfer.getTargetID());
					transferredCauses.add(HazardCauseTransfer.createCauseToCause(transfer, originCause, targetCause));
				} else {
					// CauseToHazard transfer
					Hazards targetHazard = hazardService.getHazardByID(transfer.getTargetID());
					transferredCauses.add(HazardCauseTransfer.createCauseToHazard(transfer, originCause, targetHazard));
				}
			}
		}
		return transferredCauses;
	}

	public List<CauseJSON> getAllNonDeletedCausesWithinHazardMinimalJson(int hazardID) {
		Hazards hazard = hazardService.getHazardByID(hazardID);
		List<CauseJSON> causes = new ArrayList<CauseJSON>();
		if (hazard != null) {
			for (Hazard_Causes cause : hazard.getHazardCauses()) {
				if (Strings.isNullOrEmpty(cause.getDeleteReason())) {
					if (cause.getTransfer() == 0) {
						// Regular Cause
						causes.add(new CauseJSON(cause.getID(), cause.getCauseNumber(), cause.getTitle(), false, true,
								"CAUSE"));
					} else {
						// Transferred Cause
						Transfers transfer = transferService.getTransferByID(cause.getTransfer());
						if (transfer.getTargetType().equals("CAUSE")) {
							Hazard_Causes targetCause = getHazardCauseByID(transfer.getTargetID());
							causes.add(new CauseJSON(cause.getID(), cause.getCauseNumber(), targetCause.getTitle(),
									true, true, "CAUSE"));
						} else if (transfer.getTargetType().equals("HAZARD")) {
							Hazards targetHazard = hazardService.getHazardByID(transfer.getTargetID());
							causes.add(new CauseJSON(cause.getID(), cause.getCauseNumber(), targetHazard
									.getHazardTitle(), true, true, "CAUSE"));
						}
					}
				}
			}
		}
		return causes;
	}

	
	public Hazard_Causes getHazardCauseByID(int causeID) {
		final Hazard_Causes[] hazardCause = ao.find(Hazard_Causes.class, Query.select().where("ID=?", causeID));
		return hazardCause.length > 0 ? hazardCause[0] : null;
	}

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

	public Hazard_Causes addHazardTransfer(int originHazardID, int targetHazardID, String transferReason) {
		Hazard_Causes cause = add(originHazardID, "transfer", null, null, null, transferReason, null, null);
		int transferID = createTransfer(cause.getID(), "CAUSE", targetHazardID, "HAZARD");
		cause.setTransfer(transferID);
		cause.save();
		return cause;
	}

	public Hazard_Causes addCauseTransfer(int originHazardID, int targetCauseID, String transferReason) {
		Hazard_Causes cause = add(originHazardID, "transfer", null, null, null, transferReason, null, null);
		int transferID = createTransfer(cause.getID(), "CAUSE", targetCauseID, "CAUSE");
		cause.setTransfer(transferID);
		cause.save();
		return cause;
	}

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

	private void removeTransfer(int id) {
		ao.delete(ao.find(Transfers.class, Query.select().where("ID=?", id)));
	}

}
