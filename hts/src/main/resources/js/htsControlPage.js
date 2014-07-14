function manipulateDates(dates) {
	if (dates.length > 0) {
		dates.each(function () {
			if (AJS.$(this)[0].innerText != "N/A") {
				AJS.$(this)[0].innerText = Date.parse(AJS.$(this)[0].innerText.substring(0,19)).toString("MM/dd/yyyy, HH:mm");
			}
		});
	}
}

function getSelectedControls(createdControls) {
	if (createdControls.length > 0) {
		var selectedControls = [];
		createdControls.each(function () {
			if (AJS.$(this)[0].checked === true) {
				selectedControls.push(this.value);
			}
		});
		return selectedControls;
	}
}

function uncheckSelectedControls() {
	var createdControls = AJS.$(".ControlsTableListOfCreatedControls");
	createdControls.each(function () {
		if (AJS.$(this)[0].checked === true) {
			this.checked = false;
		}
	});
}

function getSelectedControlsAndDeleteReasons(selectedControls) {
	var selectedControlsAndDeleteReasons = [];
	var skippedReasonControls = [];
	var skippedReason = false;
	for (var i = 0; i < selectedControls.length; i++) {
		var deleteReason = AJS.$("#ReasonTextForControlID" + selectedControls[i])[0].value;
		if (deleteReason === "") {
			skippedReason = true;
			skippedReasonControls.push(selectedControls[i]);
		} else {
			selectedControlsAndDeleteReasons.push({
				controlID: selectedControls[i],
				deleteReason: deleteReason
			});
		}
	}

	if (skippedReason) {
		return { allReasonsFilledOut: false, skippedReasonControlIDs: skippedReasonControls };
	} else {
		return { allReasonsFilledOut: true, controlIDAndReasons: selectedControlsAndDeleteReasons };
	}
}

function sendAjaxRequestToDeleteSpecificControl(controlID, deleteReason) {
	AJS.$.ajax({
		type: "DELETE",
		async: false,
		url: "controlform?controlID=" + controlID + "&reason=" + deleteReason,
		success: function(data) {
			var controlElementAllRows = AJS.$(".ControlsTableEntryControlID" + controlID);
			controlElementAllRows[0].remove();
			controlElementAllRows[1].remove();
		},
		error: function(data) {
			console.log("error", arguments);
		}
	});
}

function addErrorMessageToSpecificControl(controlID) {
	AJS.$("#ConfirmDialogErrorTextForControlID" + controlID).show();
}

function getHazardInformation() {
	var hazardInformation = {};
	hazardInformation.theNumber = AJS.$("#HazardNumberForControl").text();
	hazardInformation.theTitle = AJS.$("#HazardTitleForControl").text();
	return hazardInformation;
}

function deleteSelectedControls(selectedControls, hazardInformation){
	// Hazard specific mark-up:
	var dialogContent1 = "<span class='ConfirmDialogHeadingOne'>Hazard Title: <span class='ConfirmDialogHeadingOneContent'>" + hazardInformation.theTitle + "</span></span><span class='ConfirmDialogHeadingOne'>Hazard #: <span class='ConfirmDialogHeadingOneContent'>" + hazardInformation.theNumber + "</span></span>";
	// Controls specific mark-up:
	var dialogContent2;
	if (selectedControls.length === 1) {
		dialogContent2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>The following control will be deleted from the above hazard report. In order to complete the deletion, you will need to provide a short delete reason.</span></div>";
	} else {
		dialogContent2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>The following controls will be deleted from the above hazard report. In order to complete the deletion, you will need to provide a short delete reason for each of the controls.</span></div>";
	}
	// Controls specific mark-up, list of controls to be deleted:
	var dialogContent3 = "<table><thead><tr><th class='ConfirmDialogTableHeader ConfirmDialogTableCellOne'>#</th><th class='ConfirmDialogTableHeader ConfirmDialogTableCellTwo'>Description</th><th class='ConfirmDialogTableHeader ConfirmDialogTableCellThree'>Control group:</th></tr></thead><tbody>";
	for (var i = 0; i < selectedControls.length; i++) {
		var controlElementFirstRow = AJS.$(".ControlsTableEntryControlID" + selectedControls[i])[0];
		dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConformDialogTopRow'></div></td></tr>";
		dialogContent3 = dialogContent3 + "<tr><td>" + controlElementFirstRow.children[1].innerText + "</td>";
		dialogContent3 = dialogContent3 + "<td><div class='ConfirmDialogDescriptionText'>" + controlElementFirstRow.children[2].innerText + "</div></td>";
		dialogContent3 = dialogContent3 + "<td>" + controlElementFirstRow.children[3].innerText + "</td></tr>";
		dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConfirmDialogLabelContainer'><label for='ReasonTextForControlID'>Reason<span class='aui-icon icon-required '>(required)</span></label></div><div class='ConfirmDialogReasonTextContainer'><input type='text' class='ConfirmDialogReasonText' name='ReasonTextForControlID' id='ReasonTextForControlID" + selectedControls[i] + "'></div></td></tr>";
		dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><p class='ConfirmDialogErrorText ConfirmDialogErrorTextHidden' id='ConfirmDialogErrorTextForControlID" + selectedControls[i] +"'>For the control above, please provide a short delete reason.</p></td></tr>";
	}
	dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConformDialogTopRow'></div></td></tr></tbody></table>";

	var dialog = new AJS.Dialog({
		width: 600,
		height: 450,
		id: "deleteDialog",
	});

	dialog.show();
	dialog.addHeader("Confirm");
	dialog.addPanel("Panel 1",
		"<div class='panelBody'>" + dialogContent1 + dialogContent2 + dialogContent3 + "</div>",
		"panel-body");
	dialog.get("panel:0").setPadding(0);

	dialog.addButton("Cancel", function(dialog) {
		uncheckSelectedControls();
		dialog.hide();
		dialog.remove();
	});
	dialog.addButton("Continue", function(dialog) {
		var result = getSelectedControlsAndDeleteReasons(selectedControls);
		if (result.allReasonsFilledOut) {
			for (var i = 0; i < result.controlIDAndReasons.length; i++) {
				sendAjaxRequestToDeleteSpecificControl(result.controlIDAndReasons[i].controlID, result.controlIDAndReasons[i].deleteReason);
			}
			dialog.hide();
			location.reload();
		}
		else {
			oldSelectedControls = result.skippedReasonControlIDs;
			for (var j = 0; j < result.skippedReasonControlIDs.length; j++) {
				addErrorMessageToSpecificControl(result.skippedReasonControlIDs[j]);
			}
		}
	});
}

function isElementIsVisible(element) {
	return element.is(":visible");
}

function checkIfExpandButtonNeedsRenaming(expanding) {
	var numberOfNotHidden = 0;
	var numberOfCreatedControls = AJS.$('.ControlsTableCellToggle').length;
	var createdControls = AJS.$('.ControlsTableCellToggle');
		createdControls.each(function (index) {
		var entry = AJS.$(this);
		var entryFullID = entry.attr("id");
		var entryID = entryFullID.slice(-1);
		var entryEdit = AJS.$("#ControlsTableEditEntry" + entryID);
		if (!entryEdit.hasClass("ControlsTableEditEntryHidden")) {
			numberOfNotHidden++;
		}
	});

	if (numberOfNotHidden < numberOfCreatedControls) {
		AJS.$("#ExpandAllControls").html("Expand all");
		expanding = true;
	}
	else {
		AJS.$("#ExpandAllControls").html("Close all");
		expanding = false;
	}
	return expanding;
}

function findControlWithSpecificID(causesAssociatedWithControl, controlID){
	return AJS.$.grep(causesAssociatedWithControl, function(item){
		if (item.controlID === controlID) {
			return item;
		}
	});
}

function checkIfControlWasModified(oldCauses, newCauses) {
	if (oldCauses.length === newCauses.length) {
		for (var j = 0; j < newCauses.length; j++) {
			if (oldCauses.indexOf(newCauses[j]) === -1) {
				return true;
			}
		}
		return false;
	}
	else {
		return true;
	}
}

function getCurrentControlAndCausesAssociation() {
	var causesAssociatedWithControl = [];
	var multiSelectForCauses = AJS.$(".controlCausesEdit");
	multiSelectForCauses.each(function () {
		var controlIDWithText = AJS.$(this)[0].id;
		var controlIDOnly = controlIDWithText.replace("controlCauseEditForControlID", "");
		var causesIDs = [];
		var numberOfSelectedCauses = AJS.$(this)[0].children.length;
		for (var i = 0; i < numberOfSelectedCauses; i++) {
			if (AJS.$(this)[0].children[i].selected === true) {
				causesIDs.push(AJS.$(this)[0].children[i].value);
			}
		}
		causesAssociatedWithControl.push({
			controlID: controlIDOnly,
			causesIDs: causesIDs
		});
	});
	return causesAssociatedWithControl;
}

AJS.$(document).ready(function(){
	var expanding = true;
	var oldCausesAssociatedWithControl = getCurrentControlAndCausesAssociation();

	/* Text manipulation code begins */
	var dates = AJS.$(".ControlDate");
	manipulateDates(dates);
	/* Text manipulation code ends */

	/* Expand functionality code begins */
	AJS.$(".ControlsTableCellToggle").live("click", function() {
		var entry = AJS.$(this);
		var entryFullID = entry.attr("id");
		var entryID = entryFullID.slice(-1);
		var entryEdit = AJS.$("#ControlsTableEditEntry" + entryID);
		if (entryEdit.hasClass("ControlsTableEditEntryHidden")) {
			entryEdit.removeClass("ControlsTableEditEntryHidden");
			entry.removeClass("aui-icon aui-icon-small aui-iconfont-add");
			entry.addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
		}
		else {

			entryEdit.addClass("ControlsTableEditEntryHidden");
			entry.removeClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
			entry.addClass("aui-icon aui-icon-small aui-iconfont-add");
		}
		expanding = checkIfExpandButtonNeedsRenaming(expanding);
	});

	AJS.$("#ExpandAllControls").live("click", function() {
		var createdControls = AJS.$('.ControlsTableCellToggle');
		if (expanding) {
			AJS.$(this).html("Close all");
			createdControls.each(function () {
				var entry = AJS.$(this);
				var entryFullID = entry.attr("id");
				var entryID = entryFullID.slice(-1);
				var entryEdit = AJS.$("#ControlsTableEditEntry" + entryID);
				if (entryEdit.hasClass("ControlsTableEditEntryHidden")) {
					entryEdit.removeClass("ControlsTableEditEntryHidden");
					entry.removeClass("aui-icon aui-icon-small aui-iconfont-add");
					entry.addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
				}
			});
			expanding = false;
		}
		else {
			AJS.$(this).html("Expand all");
			createdControls.each(function () {
				var entry = AJS.$(this);
				var entryFullID = entry.attr("id");
				var entryID = entryFullID.slice(-1);
				var entryEdit = AJS.$("#ControlsTableEditEntry" + entryID);
				if (!entryEdit.hasClass("ControlsTableEditEntryHidden")) {
					entryEdit.addClass("ControlsTableEditEntryHidden");
					entry.removeClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
					entry.addClass("aui-icon aui-icon-small aui-iconfont-add");
				}
			});
			expanding = true;
		}
	});
	/* Expand functionality code ends */

	/* Updating existing controls functinality begins */
	AJS.$(".SaveAllChanges").live("click", function(e) {
		e.preventDefault();
		var newCausesAssociatedWithControl = getCurrentControlAndCausesAssociation();
		var createdControls = AJS.$(".ControlsTableListOfCreatedControls");
		var selectedControls = getSelectedControls(createdControls);
		var createdControlsForms = AJS.$(".editControlForm");
		var hazardInformation = getHazardInformation();
		var doDelete = false;
		var doUpdate = false;
		var doNew = false;
		var doRefresh = false;
		var validationError = false;

		// Check for updates do controls or delete requests
		createdControlsForms.each(function () {
			var controlID = this.children[2].value;
			if (selectedControls.indexOf(controlID) === -1) {
				var oldCauses = findControlWithSpecificID(oldCausesAssociatedWithControl, controlID);
				var newCauses = findControlWithSpecificID(newCausesAssociatedWithControl, controlID);
				if (checkIfControlWasModified(oldCauses[0].causesIDs, newCauses[0].causesIDs) || AJS.$(this).isDirty()) {
					AJS.$(this).trigger("submit");
					doUpdate = true;
				}
			}
			else {
				doDelete = true;
			}
		});

		validationError = isElementIsVisible(AJS.$(".validationError"));
		if (validationError) {
			JIRA.Messages.showWarningMsg("Not all changes have been saved. See invalid forms below.", {closeable: true});
			return;
		}

		// Check for new control
		if (AJS.$("#addNewControlForm").isDirty() || (AJS.$("#addNewControlForm")[0][6].children.length !== 0)) {
			AJS.$("#addNewControlForm").trigger("submit");
			doNew = true;
		}

		validationError = isElementIsVisible(AJS.$(".validationError"));
		if (validationError) {
			JIRA.Messages.showWarningMsg("Not all changes have been saved. See invalid forms below.", {closeable: true});
			return;
		}

		if (doDelete) {
			deleteSelectedControls(selectedControls, hazardInformation);
			return;
		}
		if (doUpdate || doNew) {
			location.reload();
			return;
		}
	});
	/* Updating existing controls functinality ends */

	AJS.$("#clearNewControlButton").live("click", function() {
		AJS.$("#controlDescriptionNew").val("");
		AJS.$("#controlGroupNew").val('').trigger('chosen:updated');
		AJS.$(".RemoveAll").trigger("click");
	});
});