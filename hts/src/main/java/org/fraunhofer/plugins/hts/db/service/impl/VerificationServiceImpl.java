package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Date;
import java.util.List;

import net.java.ao.DBParam;
import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Hazards;
import org.fraunhofer.plugins.hts.db.VerifcToHazard;
import org.fraunhofer.plugins.hts.db.VerificationStatus;
import org.fraunhofer.plugins.hts.db.VerificationType;
import org.fraunhofer.plugins.hts.db.Verifications;
import org.fraunhofer.plugins.hts.db.service.VerificationService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class VerificationServiceImpl implements VerificationService {
	
	private final ActiveObjects ao;
	
	public VerificationServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	
	@Override
	public Verifications getVerificationByID(String id) {
		final Verifications[] control = ao.find(Verifications.class, Query.select().where("ID=?", id));
		return control.length > 0 ? control[0] : null;
	}
	
	@Override
	public List<Verifications> all() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Verifications> getAllVerificationsWithinAHazard(Hazards hazard) {
		return newArrayList(hazard.getVerifications());
	}
	
	@Override
	public Verifications add(Hazards hazard, String description, VerificationType verificationType, 
			String responsibleParty, Date estCompletionDate, VerificationStatus verificationStatus) {
		final Verifications verification = ao.create(Verifications.class, new DBParam("VERIFICATION_DESC", description));
		verification.setVerificationType(verificationType);
		verification.setResponsibleParty(responsibleParty);
		verification.setEstCompletionDate(estCompletionDate);
		verification.setVerificationStatus(verificationStatus);
		verification.setVerificationNumber(getVerificationNumber(hazard));
		verification.setLastUpdated(new Date());
		verification.save();
		
		associateVerificationToHazard(hazard, verification);
		
		return verification;
	}
	
	@Override
	public Verifications update(Verifications verificationToEdit, String description, VerificationType verificationType, String responsibleParty,
			Date estCompletionDate, VerificationStatus verificationStatus) {
		if (!description.equals(verificationToEdit.getVerificationDesc())) {
			verificationToEdit.setVerificationDesc(description);
		}
		if (verificationType != null && verificationToEdit.getVerificationType() != null) {
			if (verificationType.getID() != verificationToEdit.getVerificationType().getID()) {
				verificationToEdit.setVerificationType(verificationType);
			}
		}
		else {
			verificationToEdit.setVerificationType(verificationType);
		}
		if (!responsibleParty.equals(verificationToEdit.getResponsibleParty())) {
			verificationToEdit.setResponsibleParty(responsibleParty);
		}
		if (estCompletionDate != verificationToEdit.getEstCompletionDate()) {
			verificationToEdit.setEstCompletionDate(estCompletionDate);
		}
		if (verificationStatus.getID() != verificationToEdit.getVerificationStatus().getID()) {
			verificationToEdit.setVerificationStatus(verificationStatus);
		}
		verificationToEdit.save();
		return verificationToEdit;
	}
	
	private void associateVerificationToHazard(Hazards hazard, Verifications verification) {
		final VerifcToHazard verifcToHazard = ao.create(VerifcToHazard.class);
		verifcToHazard.setHazard(hazard);
		verifcToHazard.setVerification(verification);
		verifcToHazard.save();
	}
	
	private int getVerificationNumber(Hazards hazard) {
		return hazard.getVerifications().length + 1;
	}
	
}