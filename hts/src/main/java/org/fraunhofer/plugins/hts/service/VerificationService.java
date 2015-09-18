package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Transfers;
import org.fraunhofer.plugins.hts.model.VerifcToControl;
import org.fraunhofer.plugins.hts.model.VerifcToHazard;
import org.fraunhofer.plugins.hts.model.VerificationNumberComparator;
import org.fraunhofer.plugins.hts.model.VerificationStatus;
import org.fraunhofer.plugins.hts.model.VerificationType;
import org.fraunhofer.plugins.hts.model.Verifications;

import com.atlassian.activeobjects.external.ActiveObjects;

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

	public Verifications add(int hazardID, String description, VerificationStatus status, VerificationType type,
			String responsibleParty, Date estimatedCompletionDate, Hazard_Controls associatedControl) {
		Verifications verification = ao.create(Verifications.class, new DBParam("VERIFICATION_DESC", description));
		Hazards hazard = hazardService.getHazardById(hazardID);
		verification.setVerificationStatus(status);
		verification.setVerificationType(type);
		verification.setResponsibleParty(responsibleParty);
		verification.setEstCompletionDate(estimatedCompletionDate);
		verification.setVerificationNumber(updateAssociation(hazard, associatedControl, verification));
		verification.setLastUpdated(new Date());
		verification.save();

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

		verification.setVerificationNumber(
				updateAssociation(verification.getHazards()[0], associatedControl, verification));
		verification.save();

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

	private int updateAssociation(Hazards hazard, Hazard_Controls associatedControl, Verifications verification) {
		Hazard_Controls currentAssociation = null;
		if (verification.getControls() != null && verification.getControls().length > 0)
			currentAssociation = verification.getControls()[0];

		// 1. associated and current are both null
		// 2. associated not null, current == null
		// 3. associated == null, current not null
		// 4. associated not null, current not null and ids are equal
		// 5. associated not null, current not null and ids are NOT equal

		int verificationNum;
		if (associatedControl == null && currentAssociation == null) {
			verificationNum = verification.getVerificationNumber();
		} else if (associatedControl != null && currentAssociation == null) {
			Verifications[] verificationsForControl = associatedControl.getVerifications();
			verificationNum = 1;
			if (verificationsForControl != null && verificationsForControl.length > 0) {
				Arrays.sort(verificationsForControl, new VerificationNumberComparator());
				verificationNum = verificationsForControl[verificationsForControl.length - 1].getVerificationNumber()
						+ 1;
			}

			final VerifcToControl verifcToControl = ao.create(VerifcToControl.class);
			verifcToControl.setControl(associatedControl);
			verifcToControl.setVerification(verification);
			verifcToControl.save();
		}
		else if (associatedControl == null && currentAssociation != null) {
			List<Verifications> orphanVerifications = hazardService.getOrphanVerifications(hazard);
			verificationNum = 1;
			if (!orphanVerifications.isEmpty()) {
				Collections.sort(orphanVerifications, new VerificationNumberComparator());
				verificationNum = orphanVerifications.get(orphanVerifications.size() - 1).getVerificationNumber() + 1;
			}
			ao.delete(ao.find(VerifcToControl.class, Query.select().where("VERIFICATION_ID=?", verification.getID())));
		} else if (associatedControl.getID() == currentAssociation.getID()) {
			verificationNum = verification.getVerificationNumber();
		} else {
			Verifications[] verificationsForControl = associatedControl.getVerifications();
			verificationNum = 1;
			if (verificationsForControl != null && verificationsForControl.length > 0) {
				Arrays.sort(verificationsForControl, new VerificationNumberComparator());
				verificationNum = verificationsForControl[verificationsForControl.length - 1].getVerificationNumber()
						+ 1;
			}
			ao.delete(ao.find(VerifcToControl.class, Query.select().where("VERIFICATION_ID=?", verification.getID())));
			
			final VerifcToControl verifcToControl = ao.create(VerifcToControl.class);
			verifcToControl.setControl(associatedControl);
			verifcToControl.setVerification(verification);
			verifcToControl.save();
		}
		return verificationNum;
	}

	private int createTransfer(int originID, String originType, int targetID, String targetType) {
		Transfers transfer = transferService.add(originID, originType, targetID, targetType);
		return transfer.getID();
	}

	public Map<Integer, Verifications> getAllTransferredVerifications(Hazards hazard) {
		Map<Integer, Verifications> transferredVerifications = new HashMap<Integer, Verifications>();
		for (Verifications originVerification : hazard.getVerifications()) {
			if (originVerification.getTransfer() != 0) {
				Transfers transfer = transferService.getTransferByID(originVerification.getTransfer());
				Verifications target = getVerificationByID(transfer.getTargetID());
				transferredVerifications.put(originVerification.getID(), target);
			}
		}
		return transferredVerifications;
	}

	public Verifications updateTransferredVerification(int verificationId, String transferReason,
			Hazard_Controls associatedControl) {
		Verifications verification = getVerificationByID(verificationId);
		verification.setVerificationDesc(transferReason);
		verification.setVerificationNumber(
				updateAssociation(verification.getHazards()[0], associatedControl, verification));
		verification.setLastUpdated(new Date());
		verification.save();
		return verification;
	}

}