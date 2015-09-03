package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Transfers;
import org.fraunhofer.plugins.hts.model.VerifcToControl;
import org.fraunhofer.plugins.hts.model.VerifcToHazard;
import org.fraunhofer.plugins.hts.model.VerificationStatus;
import org.fraunhofer.plugins.hts.model.VerificationType;
import org.fraunhofer.plugins.hts.model.Verifications;
import org.fraunhofer.plugins.hts.view.model.VerificationTransfer;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.google.common.collect.Lists;

import net.java.ao.DBParam;
import net.java.ao.Query;

public class VerificationService {
	private final ActiveObjects ao;
	private final HazardService hazardService;
	private final TransferService transferService;

	public VerificationService(ActiveObjects ao, HazardService hazardService, TransferService transferService) {
		this.ao = checkNotNull(ao);
		this.hazardService = checkNotNull(hazardService);
		this.transferService = transferService;
	}

	public Verifications getVerificationByID(int verificationID) {
		final Verifications[] verification = ao.find(Verifications.class, Query.select().where("ID=?", verificationID));
		return verification.length > 0 ? verification[0] : null;
	}

	public Verifications getVerificationByID(String verificationID) {
		checkNotNull(verificationID);
		return getVerificationByID(Integer.parseInt(verificationID));
	}

	public List<Verifications> getAllNonDeletedVerificationsWithinAHazard(Hazards hazard) {
		List<Verifications> allRemaining = new ArrayList<Verifications>();
		for (Verifications current : hazard.getVerifications()) {
			if (current.getDeleteReason() == null) {
				allRemaining.add(current);
			}
		}
		return allRemaining;
	}

	public Verifications add(int hazardID, String description, VerificationStatus status, VerificationType type,
			String responsibleParty, Date estimatedCompletionDate, Hazard_Controls associatedControl) {
		Verifications verification = ao.create(Verifications.class, new DBParam("VERIFICATION_DESC", description));
		Hazards hazard = hazardService.getHazardById(hazardID);
		verification.setVerificationNumber(verification.getID());
		verification.setVerificationStatus(status);
		verification.setVerificationType(type);
		verification.setResponsibleParty(responsibleParty);
		verification.setEstCompletionDate(estimatedCompletionDate);
		verification.setLastUpdated(new Date());
		verification.save();

		if (associatedControl != null) {
			associateVerificationToControl(associatedControl, verification);
		}

		final VerifcToHazard verifcToHazard = ao.create(VerifcToHazard.class);
		verifcToHazard.setHazard(hazard);
		verifcToHazard.setVerification(verification);
		verifcToHazard.save();
		return verification;
	}

	public Verifications addVerificationTransfer(int originHazardID, int targetVerificationId, String transferReason,
			Hazard_Controls associatedControl) {
		Verifications verification = add(originHazardID, transferReason, null, null, null, null, associatedControl);
		int transferID = createTransfer(verification.getID(), "VERIFICATION", targetVerificationId, "VERIFICATION");
		verification.setTransfer(transferID);
		verification.save();
		return verification;
	}

	public Verifications update(int verificationID, String description, VerificationStatus status,
			VerificationType type, String responsibleParty, Date estimatedCompletionDate,
			Hazard_Controls associatedControl) {
		Verifications verification = getVerificationByID(verificationID);
		verification.setVerificationDesc(description);
		verification.setVerificationStatus(status);
		verification.setVerificationType(type);
		verification.setResponsibleParty(responsibleParty);
		verification.setEstCompletionDate(estimatedCompletionDate);
		verification.setLastUpdated(new Date());
		verification.save();
		
		
		removeAssociationsVerificationToControl(verificationID);

		if (associatedControl != null) {
			associateVerificationToControl(associatedControl, verification);
		}

		return verification;
	}

	public Verifications deleteVerification(Verifications verificationToDelete, String reason) {
		verificationToDelete.setDeleteReason(reason);

		if (verificationToDelete.getTransfer() != 0) {
			Transfers transfer = transferService.getTransferByID(verificationToDelete.getTransfer());
			removeTransfer(transfer.getID());
			transfer.save();
			verificationToDelete.setTransfer(0);
		}
		verificationToDelete.save();
		return verificationToDelete;
	}

	private void removeTransfer(int id) {
		ao.delete(ao.find(Transfers.class, Query.select().where("ID=?", id)));
	}

	private void associateVerificationToControl(Hazard_Controls control, Verifications verification) {
		final VerifcToControl verifcToControl = ao.create(VerifcToControl.class);
		verifcToControl.setControl(control);
		verifcToControl.setVerification(verification);
		verifcToControl.save();
	}

	private int createTransfer(int originID, String originType, int targetID, String targetType) {
		Transfers transfer = transferService.add(originID, originType, targetID, targetType);
		return transfer.getID();
	}

	public List<Verifications> getOrphanVerifications(Hazards hazard) {
		List<Verifications> orphanVerifications = Lists.newArrayList();
		List<Verifications> verifications = getAllNonDeletedVerificationsWithinAHazard(hazard);
		for (Verifications verification : verifications) {
			Hazard_Controls[] controls = verification.getControls();
			if (controls == null || controls.length == 0)
				orphanVerifications.add(verification);
		}
		return orphanVerifications;
	}

	public Map<Integer, VerificationTransfer> getAllTransferredVerifications(Hazards hazard) {
		Map<Integer, VerificationTransfer> transferredVerifications = new HashMap<Integer, VerificationTransfer>();
		for (Verifications originVerification : hazard.getVerifications()) {
			if (originVerification.getTransfer() != 0) {
				Transfers transfer = transferService.getTransferByID(originVerification.getTransfer());
				getVerificationByID(transfer.getTargetID());
				transferredVerifications.put(originVerification.getID(), VerificationTransfer
						.createTransfer(originVerification, getVerificationByID(transfer.getTargetID())));
			}
		}
		return transferredVerifications;
	}

	public Verifications updateTransferredVerification(int verificationId, String transferReason,
			Hazard_Controls associatedControl) {
		Verifications verification = getVerificationByID(verificationId);
		verification.setVerificationDesc(transferReason);
		removeAssociationsVerificationToControl(verification.getID());
		if (associatedControl != null) {
			associateVerificationToControl(associatedControl, verification);
		}
		verification.setLastUpdated(new Date());
		verification.save();
		return verification;
	}

	public void removeAssociationsVerificationToControl(int verificationId) {
		ao.delete(ao.find(VerifcToControl.class, Query.select().where("VERIFICATION_ID=?", verificationId)));
	}
}