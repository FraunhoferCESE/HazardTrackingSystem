package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fraunhofer.plugins.hts.model.ControlGroups;
import org.fraunhofer.plugins.hts.model.ControlToCause;
import org.fraunhofer.plugins.hts.model.ControlToHazard;
import org.fraunhofer.plugins.hts.model.Hazard_Causes;
import org.fraunhofer.plugins.hts.model.Hazard_Controls;
import org.fraunhofer.plugins.hts.model.Hazards;
import org.fraunhofer.plugins.hts.model.Transfers;
import org.fraunhofer.plugins.hts.rest.model.ControlJSON;
import org.fraunhofer.plugins.hts.view.model.ControlTransfer;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import net.java.ao.Query;

public class ControlService {
	private final ActiveObjects ao;
	private final HazardService hazardService;
	private final TransferService transferService;
	private final CauseService hazardCauseService;

	public ControlService(ActiveObjects ao, HazardService hazardService, TransferService transferService,
			CauseService hazardCauseService) {
		this.ao = checkNotNull(ao);
		this.hazardService = checkNotNull(hazardService);
		this.transferService = checkNotNull(transferService);
		this.hazardCauseService = hazardCauseService;
	}

	public List<Hazard_Controls> getOrphanControls(Hazards hazard) {
		List<Hazard_Controls> orphanControls = Lists.newArrayList();
		List<Hazard_Controls> controls = getAllNonDeletedControlsWithinAHazard(hazard);
		for (Hazard_Controls control : controls) {
			Hazard_Causes[] causes = control.getCauses();
			if (causes == null || causes.length == 0)
				orphanControls.add(control);
		}

		return orphanControls;
	}

	public Hazard_Controls add(int hazardID, String description, ControlGroups controlGroup, Hazard_Causes cause) {
		Hazard_Controls control = ao.create(Hazard_Controls.class);
		Hazards hazard = hazardService.getHazardById(hazardID);

		control.setTransfer(0);
		control.setDescription(description);
		control.setControlGroup(controlGroup);
		if (cause != null) {
			Hazard_Controls[] controls = cause.getControls();
			control.setControlNumber(controls.length == 0 ? 1 : controls[controls.length - 1].getControlNumber() + 1);
			associateControlToCause(control, cause);
		} else {
			control.setControlNumber(getOrphanControls(hazard).size() + 1);
		}
		control.setOriginalDate(new Date());
		control.setLastUpdated(new Date());
		control.save();
		associateControlToHazard(hazard, control);
		return control;
	}

	public Hazard_Controls updateRegularControl(int controlID, String description, ControlGroups controlGroup,
			Hazard_Causes causes) {
		Hazard_Controls control = getHazardControlByID(controlID);
		control.setDescription(description);
		control.setControlGroup(controlGroup);

		removeAssociationsControlToCause(control.getID());
		if (causes != null) {
			associateControlToCause(control, causes);
		}
		control.setLastUpdated(new Date());
		control.save();
		return control;
	}

	public Hazard_Controls updateTransferredControl(int controlID, String transferReason,
			Hazard_Causes associatedCause) {
		Hazard_Controls control = getHazardControlByID(controlID);
		control.setDescription(transferReason);
		removeAssociationsControlToCause(control.getID());
		if (associatedCause != null) {
			associateControlToCause(control, associatedCause);
		}
		control.setLastUpdated(new Date());
		control.save();
		return control;
	}

	public Hazard_Controls getHazardControlByID(int controlID) {
		final Hazard_Controls[] control = ao.find(Hazard_Controls.class, Query.select().where("ID=?", controlID));
		return control.length > 0 ? control[0] : null;
	}

	public List<Hazard_Controls> getAllNonDeletedControlsWithinAHazard(Hazards hazard) {
		List<Hazard_Controls> nonDeleted = new ArrayList<Hazard_Controls>();
		if (hazard != null) {
			for (Hazard_Controls current : hazard.getHazardControls()) {
				if (Strings.isNullOrEmpty(current.getDeleteReason())) {
					nonDeleted.add(current);
				}
			}
		}
		return nonDeleted;
	}

	public List<ControlJSON> getAllNonDeletedControlsWithinCauseMinimalJson(int causeID, boolean includeTransfers) {
		Hazard_Causes cause = hazardCauseService.getHazardCauseByID(causeID);
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
							Hazard_Causes targetCause = hazardCauseService.getHazardCauseByID(transfer.getTargetID());
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
					Hazard_Causes targetCause = hazardCauseService.getHazardCauseByID(transfer.getTargetID());
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

	private void associateControlToHazard(Hazards hazard, Hazard_Controls control) {
		final ControlToHazard controlToHazard = ao.create(ControlToHazard.class);
		controlToHazard.setHazard(hazard);
		controlToHazard.setControl(control);
		controlToHazard.save();
	}

	private void associateControlToCause(Hazard_Controls control, Hazard_Causes cause) {
		ControlToCause[] find = ao.find(ControlToCause.class,
				Query.select().where("CONTROL_ID=? AND CAUSE_ID=?", control.getID(), cause.getID()));
		if (find.length == 0) {
			final ControlToCause controlToCause = ao.create(ControlToCause.class);
			controlToCause.setCause(cause);
			controlToCause.setControl(control);
			controlToCause.save();
		}
	}

	private void removeAssociationsControlToCause(int id) {
		ao.delete(ao.find(ControlToCause.class, Query.select().where("CONTROL_ID=?", id)));
	}

	private int createTransfer(int originID, String originType, int targetID, String targetType) {
		Transfers transfer = transferService.add(originID, originType, targetID, targetType);
		return transfer.getID();
	}

	private void removeTransfer(int id) {
		ao.delete(ao.find(Transfers.class, Query.select().where("ID=?", id)));
	}

}
