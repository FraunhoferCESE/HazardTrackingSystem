package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fraunhofer.plugins.hts.model.ControlGroups;
import org.fraunhofer.plugins.hts.model.ControlNumComparator;
import org.fraunhofer.plugins.hts.model.ControlToCause;
import org.fraunhofer.plugins.hts.model.ControlToHazard;
import org.fraunhofer.plugins.hts.model.Hazard_Causes;
import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Transfers;
import org.fraunhofer.plugins.hts.model.VerifcToControl;
import org.fraunhofer.plugins.hts.model.Verifications;
import org.fraunhofer.plugins.hts.rest.model.ControlJSON;
import org.fraunhofer.plugins.hts.view.model.ControlTransfer;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.google.common.base.Strings;

import net.java.ao.Query;

public class ControlService {
	private final ActiveObjects ao;
	private final HazardService hazardService;
	private final TransferService transferService;
	private final CauseService causeService;

	public ControlService(ActiveObjects ao, HazardService hazardService, TransferService transferService,
			CauseService hazardCauseService) {
		this.ao = checkNotNull(ao);
		this.hazardService = checkNotNull(hazardService);
		this.transferService = checkNotNull(transferService);
		this.causeService = hazardCauseService;
	}

	public Hazard_Controls add(int hazardID, String description, ControlGroups controlGroup,
			Hazard_Causes associatedCause) {
		Hazard_Controls control = ao.create(Hazard_Controls.class);
		Hazards hazard = hazardService.getHazardById(hazardID);

		control.setTransfer(0);
		control.setDescription(description);
		control.setControlGroup(controlGroup);
		control.setControlNumber(updateAssociation(hazard, control, associatedCause));

		control.setOriginalDate(new Date());
		control.setLastUpdated(new Date());
		control.save();

		final ControlToHazard controlToHazard = ao.create(ControlToHazard.class);
		controlToHazard.setHazard(hazard);
		controlToHazard.setControl(control);
		controlToHazard.save();
		return control;
	}

	public Hazard_Controls updateRegularControl(int controlID, String description, ControlGroups controlGroup,
			Hazard_Causes associatedCause) {
		Hazard_Controls control = getHazardControlByID(controlID);
		control.setDescription(description);
		control.setControlGroup(controlGroup);

		control.setControlNumber(updateAssociation(control.getHazard()[0], control, associatedCause));
		control.setLastUpdated(new Date());
		control.save();
		return control;
	}

	public Hazard_Controls updateTransferredControl(int controlID, String transferReason,
			Hazard_Causes associatedCause) {
		Hazard_Controls control = getHazardControlByID(controlID);
		control.setDescription(transferReason);

		control.setControlNumber(updateAssociation(control.getHazard()[0], control, associatedCause));

		control.setLastUpdated(new Date());
		control.save();
		return control;
	}

	private int updateAssociation(Hazards hazard, Hazard_Controls control, Hazard_Causes associatedCause) {
		Hazard_Causes currentAssociation = null;
		if (control.getCauses() != null && control.getCauses().length > 0)
			currentAssociation = control.getCauses()[0];

		int controlNum = 1;
		if (associatedCause != null) {
			Hazard_Controls[] controlsForCause = associatedCause.getControls();
			if (controlsForCause != null && controlsForCause.length > 0) {
				Arrays.sort(controlsForCause, new ControlNumComparator());
				controlNum = controlsForCause[controlsForCause.length - 1].getControlNumber() + 1;
			}

			if (currentAssociation != null && associatedCause != currentAssociation)
				ao.delete(ao.find(ControlToCause.class, Query.select().where("CONTROL_ID=?", control.getID())));

			final ControlToCause controlToCause = ao.create(ControlToCause.class);
			controlToCause.setCause(associatedCause);
			controlToCause.setControl(control);
			controlToCause.save();
		} else {
			List<Hazard_Controls> orphanControls = hazardService.getOrphanControls(hazard);
			if (!orphanControls.isEmpty() || orphanControls.size() == 1) {
				Collections.sort(orphanControls, new ControlNumComparator());
				controlNum = orphanControls.get(orphanControls.size() - 1).getControlNumber() + 1;
			}

			if (currentAssociation != null)
				ao.delete(ao.find(ControlToCause.class, Query.select().where("CONTROL_ID=?", control.getID())));
		}
		return controlNum;
	}

	public Hazard_Controls getHazardControlByID(int controlID) {
		final Hazard_Controls[] control = ao.find(Hazard_Controls.class, Query.select().where("ID=?", controlID));
		return control.length > 0 ? control[0] : null;
	}

	public List<ControlJSON> getAllNonDeletedControlsWithinCauseMinimalJson(int causeID, boolean includeTransfers) {
		Hazard_Causes cause = causeService.getHazardCauseByID(causeID);
		List<ControlJSON> controls = new ArrayList<ControlJSON>();
		if (cause != null) {
			for (Hazard_Controls control : cause.getControls()) {
				if (Strings.isNullOrEmpty(control.getDeleteReason())) {
					if (control.getTransfer() == 0) {
						// Regular Control
						controls.add(new ControlJSON(control.getID(), control.getControlNumber(),
								control.getDescription(), false, true, "CONTROL"));
					} else if (includeTransfers) {
						// Transferred Control
						Transfers transfer = transferService.getTransferByID(control.getTransfer());
						if (transfer.getTargetType().equals("CONTROL")) {
							Hazard_Controls targetControl = getHazardControlByID(transfer.getTargetID());
							controls.add(new ControlJSON(control.getID(), control.getControlNumber(),
									targetControl.getDescription(), true, true, "CONTROL"));
						} else if (transfer.getTargetType().equals("CAUSE")) {
							Hazard_Causes targetCause = causeService.getHazardCauseByID(transfer.getTargetID());
							controls.add(new ControlJSON(control.getID(), control.getControlNumber(),
									targetCause.getTitle(), true, true, "CONTROL"));
						}
					}
				}
			}
		}
		return controls;
	}

	public Map<Integer, ControlTransfer> getAllTransferredControls(Hazards hazard) {
		Map<Integer, ControlTransfer> transferredControls = new HashMap<Integer, ControlTransfer>();
		for (Hazard_Controls originControl : hazard.getHazardControls()) {
			if (originControl.getTransfer() != 0) {
				Transfers transfer = transferService.getTransferByID(originControl.getTransfer());
				if (transfer.getTargetType().equals("CONTROL")) {
					// ControlToControl transfer
					Hazard_Controls targetControl = getHazardControlByID(transfer.getTargetID());
					if (targetControl.getHazard()[0].getProjectID() == hazard.getProjectID()) {
						transferredControls.put(originControl.getID(),
								ControlTransfer.createControlToControl(transfer, originControl, targetControl));
					} else {
						transferredControls
								.put(originControl.getID(),
										ControlTransfer.createMovedProjectTransfer(transfer,
												ComponentAccessor.getProjectManager()
														.getProjectObj(targetControl.getHazard()[0].getProjectID())
														.getName()));
					}
				} else {
					// ControlToCause
					Hazard_Causes targetCause = causeService.getHazardCauseByID(transfer.getTargetID());
					if (targetCause.getHazards()[0].getProjectID() == hazard.getProjectID())
						transferredControls.put(originControl.getID(),
								ControlTransfer.createControlToCause(transfer, originControl, targetCause));
					else
						transferredControls
								.put(originControl.getID(),
										ControlTransfer.createMovedProjectTransfer(transfer,
												ComponentAccessor.getProjectManager()
														.getProjectObj(targetCause.getHazards()[0].getProjectID())
														.getName()));
				}
			}
		}
		return transferredControls;
	}

	public Hazard_Controls deleteControl(int controlID, String deleteReason) {
		Hazard_Controls control = getHazardControlByID(controlID);
		if (control != null) {
			control.setDeleteReason(deleteReason == null ? null : deleteReason.trim());
			if (control.getTransfer() != 0) {
				Transfers transfer = transferService.getTransferByID(control.getTransfer());
				removeTransfer(transfer.getID());
				transfer.save();
				control.setTransfer(0);
			}

			List<Verifications> orphanVerifications = hazardService
					.getOrphanVerifications(control.getHazard()[0]);
			int verificationNum = orphanVerifications.get(orphanVerifications.size() - 1).getVerificationNumber();
			for (Verifications verification : control.getVerifications()) {
				ao.delete(ao.find(VerifcToControl.class,
						Query.select().where("VERIFICATION_ID=? AND CONTROL_ID=?", verification.getID(), controlID)));
				verification.setVerificationNumber(++verificationNum);
				verification.save();
			}

			control.save();
		}
		return control;
	}

	public Hazard_Controls addControlTransfer(int originHazardID, int targetControlID, String transferReason,
			Hazard_Causes associatedCause) {
		Hazard_Controls control = add(originHazardID, transferReason, null, associatedCause);
		int transferID = createTransfer(control.getID(), "CONTROL", targetControlID, "CONTROL");
		control.setTransfer(transferID);
		control.save();
		return control;
	}

	public Hazard_Controls addCauseTransfer(int originHazardID, int targetCauseID, String transferReason,
			Hazard_Causes associatedCause) {
		Hazard_Controls control = add(originHazardID, transferReason, null, associatedCause);
		int transferID = createTransfer(control.getID(), "CONTROL", targetCauseID, "CAUSE");
		control.setTransfer(transferID);
		control.save();
		return control;
	}

	public Hazard_Controls[] getHazardControlsByID(Integer[] id) {
		if (id == null) {
			return null;
		} else {
			Hazard_Controls[] controlsArr = new Hazard_Controls[id.length];
			for (int i = 0; i < id.length; i++) {
				controlsArr[i] = ao.get(Hazard_Controls.class, id[i]);
			}
			return controlsArr;
		}
	}

	private int createTransfer(int originID, String originType, int targetID, String targetType) {
		Transfers transfer = transferService.add(originID, originType, targetID, targetType);
		return transfer.getID();
	}

	private void removeTransfer(int id) {
		ao.delete(ao.find(Transfers.class, Query.select().where("ID=?", id)));
	}

}
