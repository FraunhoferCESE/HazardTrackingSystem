function manipulateHazardTextForControls() {
	if (AJS.$("#HazardTitleForControl").text().length >= 128) {
		var shortend1 = AJS.$("#HazardTitleForControl").text().substring(0,125) + "...";
		AJS.$("#HazardTitleForControl").text(shortend1);
	}
	if (AJS.$("#HazardNumberForControl").text().length >= 128) {
		var shortend2 = AJS.$("#HazardNumberForControl").text().substring(0,125) + "...";
		AJS.$("#HazardNumberForControl").text(shortend2);
	}
}

function manipulateControlDates() {
	var dates = AJS.$(".ControlDate");
	if (dates.length > 0) {
		dates.each(function () {
			var dateToBeInserted = Date.parse(AJS.$(this).text().substring(0,19)).toString("MMMM dd, yyyy, HH:mm");
			AJS.$(this).text(dateToBeInserted);
		});
	}
}

function manipulateControlTextDescriptions() {
	var controlDescriptions = AJS.$(".ControlsTableDescriptionText");
	if (controlDescriptions.length > 0) {
		controlDescriptions.each(function () {
			var shortend;
			if (AJS.$(this).children().length === 0) {
				if (AJS.$(this).text().length >= 128) {
					shortend = AJS.$(this).text().substring(0, 125) + "...";
					AJS.$(this).text(shortend);
				}
			}
			else {
				var shortendArr = AJS.$(this).text().split(" - ");
				if (shortendArr.length === 2) {
					if (shortendArr[1].length >= 128) {
						shortend = shortendArr[1].substring(0, 125) + "...";
						AJS.$(this).children(":first").text(shortendArr[0] + " - " + shortend);
					}
				}
				else {
					if (AJS.$(this).text().length >= 128) {
						shortend = AJS.$(this).text().substring(0, 125) + "...";
						AJS.$(this).children(":first").text(shortendArr[0] + " - " + shortend);
					}
				}
			}
		});
	}
}

function manipulateControlTextVariableLength(theText, length) {
	if (theText.length >= length){
		return theText.substring(0, (length - 3)) + "...";
	}
	else {
		return theText;
	}
}

function manipulateTextForHazardSelectionInControls() {
	var hazardList = AJS.$("#controlHazardList");
	if (hazardList.children().length > 1) {
		(hazardList.children()).each(function () {
			if (AJS.$(this).text().length >= 85) {
				AJS.$(this).text(AJS.$(this).text().substring(0,82) + "...");
			}
		});
	}
}

function getSelectedControls() {
	var createdControls = AJS.$(".ControlsTableListOfCreatedControls");
	if (createdControls.length > 0) {
		var selectedControls = [];
		createdControls.each(function () {
			if (AJS.$(this).is(':checked')) {
				selectedControls.push(this.value);
			}
		});
		return selectedControls;
	}
}

function uncheckSelectedControls(arrayOfControlIDsToDelete) {
	if (arrayOfControlIDsToDelete === undefined) {
		// uncheck everything
		var checkboxes = AJS.$(".ControlsTableListOfCreatedControls");
		checkboxes.each(function () {
			if (AJS.$(this).is(":checked")) {
				AJS.$(this).prop("checked", false);
			}
		});
	}
	else {
		// uncheck specific
		for (var i = 0; i < arrayOfControlIDsToDelete.length; i++) {
			var checkboxElement = AJS.$("input[data-controlid='" + arrayOfControlIDsToDelete[i] + "']");
			if (AJS.$(checkboxElement).is(":checked")) {
				AJS.$(checkboxElement).prop("checked", false);
			}
		}
	}
}

function getSelectedControlsAndDeleteReasons(selectedControls) {
	var selectedControlsAndDeleteReasons = [];
	var skippedReasonControls = [];
	var skippedReason = false;
	for (var i = 0; i < selectedControls.length; i++) {
		var deleteReason = AJS.$("#ReasonTextForControlID" + selectedControls[i]).val();
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
			console.log("SUCCESS");
			var controlFormElement = AJS.$("form[data-key='" + controlID + "']");
			controlFormElement.removeDirtyWarning();
		},
		error: function(data) {
			console.log("error", arguments);
		}
	});
}

function addErrorMessageToSpecificControl(controlID, message) {
	AJS.$("#ConfirmDialogErrorTextForControlID" + controlID).text(message);
	AJS.$("#ConfirmDialogErrorTextForControlID" + controlID).show();
}

function removeErrorMessageFromSpecificControl(controlID) {
	if (AJS.$("#ConfirmDialogErrorTextForControlID" + controlID).is(":visible")) {
		AJS.$("#ConfirmDialogErrorTextForControlID" + controlID).hide();
		AJS.$("#ConfirmDialogErrorTextForControlID" + controlID).text("");
	}
}

function getHazardInformationInControls() {
	var hazardInformation = {};
	hazardInformation.theNumber = AJS.$("#HazardNumberForControl").text();
	hazardInformation.theTitle = AJS.$("#HazardTitleForControl").text();
	hazardInformation.theID = AJS.$("#hazardID").val();
	return hazardInformation;
}

function deleteSelectedControls(doRefresh, arrayOfControlIDsToDelete, arrayOfDirtyControlIDs) {
	// Hazard specific mark-up:
	var hazardInformation = getHazardInformationInControls();
	var dialogContent1 = "<span class='ConfirmDialogHeadingOne'>Hazard Title: <span class='ConfirmDialogHeadingOneContent'>" + manipulateControlTextVariableLength(hazardInformation.theTitle, 64) + "</span></span><span class='ConfirmDialogHeadingOne'>Hazard #: <span class='ConfirmDialogHeadingOneContent'>" + manipulateControlTextVariableLength(hazardInformation.theNumber, 64) + "</span></span>";
	// Controls specific mark-up:
	var dialogContent2;
	if (arrayOfControlIDsToDelete.length === 1) {
		dialogContent2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>The following control will be deleted from the above hazard report. In order to complete the deletion, you will need to provide a short delete reason.</span></div>";
	} else {
		dialogContent2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>The following controls will be deleted from the above hazard report. In order to complete the deletion, you will need to provide a short delete reason for each of the controls.</span></div>";
	}
	// Controls specific mark-up, list of controls to be deleted:
	var dialogContent3 = "<table><thead><tr><th class='ConfirmDialogTableHeader ConfirmDialogTableCellOneControls'>#</th><th class='ConfirmDialogTableHeader ConfirmDialogTableCellTwoControls'>Description</th><th class='ConfirmDialogTableHeader ConfirmDialogTableCellThreeControls'>Control group:</th></tr></thead><tbody>";
	for (var i = 0; i < arrayOfControlIDsToDelete.length; i++) {
		var controlElementFirstRow = AJS.$(".ControlsTableEntryControlID" + arrayOfControlIDsToDelete[i]).first();
		dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConformDialogTopRow'></div></td></tr>";
		dialogContent3 = dialogContent3 + "<tr><td>" + controlElementFirstRow.children(":nth-child(2)").text().replace("Control ", "") + "</td>";
		dialogContent3 = dialogContent3 + "<td><div class='ConfirmDialogDescriptionText'>" + controlElementFirstRow.children(":nth-child(3)").text() + "</div></td>";
		dialogContent3 = dialogContent3 + "<td>" + controlElementFirstRow.children(":nth-child(4)").text() + "</td></tr>";

		if (i === 0 && arrayOfControlIDsToDelete.length > 1) {
			dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConfirmDialogLabelContainer'><label for='ReasonTextForControl'>Reason<span class='aui-icon icon-required '>(required)</span></label></div><div class='ConfirmDialogReasonTextContainer'><input type='text' class='ConfirmDialogReasonTextControls' name='ReasonTextForControl' id='ReasonTextForControlID" + arrayOfControlIDsToDelete[i] + "'></div><div class='ConfirmDialogDuplButtonContainer'><button class='aui-button ConfirmDialogDuplButton' id='ConfirmDialogDuplBtnControls'>Apply to all</button></div></td></tr>";
		}
		else {
			dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConfirmDialogLabelContainer'><label for='ReasonTextForControl'>Reason<span class='aui-icon icon-required '>(required)</span></label></div><div class='ConfirmDialogReasonTextContainerNoButton'><input type='text' class='ConfirmDialogReasonTextControls' name='ReasonTextForControl' id='ReasonTextForControlID" + arrayOfControlIDsToDelete[i] + "'></div></td></tr>";
		}

		if (arrayOfDirtyControlIDs.indexOf(arrayOfControlIDsToDelete[i]) !== -1) {
			dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><p class='ConfirmDialogErrorText'>This control has been edited. All changes will be discarded.</p></td></tr>";
		}

		dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><p class='ConfirmDialogErrorText ConfirmDialogErrorTextHidden' id='ConfirmDialogErrorTextForControlID" + arrayOfControlIDsToDelete[i] +"'></p></td></tr>";
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
		uncheckSelectedControls(arrayOfControlIDsToDelete);
		dialog.hide();
		dialog.remove();
		if (doRefresh) {
			updateUpdateMessageCookie("updates");
			location.reload();
		}
	});

	dialog.addButton("Continue", function(dialog) {
		var result = getSelectedControlsAndDeleteReasons(arrayOfControlIDsToDelete);
		if (result.allReasonsFilledOut) {
			for (var i = 0; i < result.controlIDAndReasons.length; i++) {
				sendAjaxRequestToDeleteSpecificControl(result.controlIDAndReasons[i].controlID, result.controlIDAndReasons[i].deleteReason);
			}
			dialog.hide();
			updateUpdateMessageCookie("updates");
			location.reload();
		}
		else {
			for (var j = 0; j < result.skippedReasonControlIDs.length; j++) {
				addErrorMessageToSpecificControl(result.skippedReasonControlIDs[j], "For the control above, please provide a short delete reason.");
			}
		}
	});
}

function checkIfExpandButtonNeedsRenaming(expanding) {
	var numberOfNotHidden = 0;
	var numberOfCreatedControls = AJS.$('.ControlsTableCellToggle').length;
	var createdControls = AJS.$('.ControlsTableCellToggle');
	createdControls.each(function (index) {
		var entry = AJS.$(this);
		var entryFullID = entry.attr("id");
		var entryID = entryFullID.replace( /^\D+/g, '');
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

function createControlsCookie() {
	if (AJS.Cookie.read("CONTROLS_COOKIE_CREATED") === undefined) {
		AJS.Cookie.save("CONTROLS_COOKIE_CREATED", true);
		AJS.Cookie.save("OPEN_CONTROLS", "none");
	}
}

function updateControlsCookie(operation, entryID) {
	var openControls = AJS.Cookie.read("OPEN_CONTROLS");
	if (operation === "add") {
		if (openControls === "none") {
			openControls = entryID;
		}
		else {
			openControls = openControls + "," + entryID;
		}
		AJS.Cookie.save("OPEN_CONTROLS", openControls);
	}
	else {
		if (openControls !== "none") {
			openControlsArray = openControls.split(',');
			var indexOfEntry = openControlsArray.indexOf(entryID);
			if (indexOfEntry > -1) {
				openControlsArray.splice(indexOfEntry, 1);
				if (openControlsArray.length === 0) {
					AJS.Cookie.save("OPEN_CONTROLS", "none");
				}
				else {
					AJS.Cookie.save("OPEN_CONTROLS", openControlsArray.toString());
				}
			}
		}
	}
}

function openControlsInCookie() {
	var openControls = AJS.Cookie.read("OPEN_CONTROLS");
	if (openControls !== "none") {
		openControlsArray = openControls.split(',');
		for (var i = 0; i < openControlsArray.length; i++) {
			var entryEdit = AJS.$("#ControlsTableEditEntry" + openControlsArray[i]);
			if (entryEdit.hasClass("ControlsTableEditEntryHidden")) {
				entryEdit.removeClass("ControlsTableEditEntryHidden");
				var entry = AJS.$("#ControlsTableEntry" + openControlsArray[i]);
				entry.removeClass("aui-icon aui-icon-small aui-iconfont-add");
				entry.addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
			}
		}
	}
}

function getAssociatedCauseCookie() {
	return AJS.Cookie.read("ASSOCIATED_CAUSE");
}

function updateAssociatedCauseCookie(theCauseID) {
	AJS.Cookie.save("ASSOCIATED_CAUSE", theCauseID);
}

function createAssociatedControlCookie() {
	if (AJS.Cookie.read("ASSOCIATED_CONTROL") === undefined) {
		AJS.Cookie.save("ASSOCIATED_CONTROL", "none");
	}
}

function updateAssociatedControlCookie(theControlID) {
	AJS.Cookie.save("ASSOCIATED_CONTROL", theControlID);
}

function createUpdateMessageCookie() {
	if (AJS.Cookie.read("UPDATE_MESSAGE") === undefined) {
		AJS.Cookie.save("UPDATE_MESSAGE", "none");
	}
}

function updateUpdateMessageCookie(update) {
	AJS.Cookie.save("UPDATE_MESSAGE", update);
}

function checkUpdateMessageCookie() {
	if (AJS.Cookie.read("UPDATE_MESSAGE") !== "none") {
		JIRA.Messages.showSuccessMsg("All changes were successfully saved.", {closeable: true});
		updateUpdateMessageCookie("none");
	}
}

function checkForValidationError() {
	if (AJS.$(".validationError").is(":visible")) {
		JIRA.Messages.showWarningMsg("Not all changes have been saved. See invalid forms below.", {closeable: true});
		return true;
	}
	else {
		return false;
	}
}

function checkForUpdatesToExistingControls(originalCreatedControls) {
	var modifiedCreatedControls = serializeCreatedControls();
	var selectedControls = getSelectedControls();
	var createdControlsForms = AJS.$(".editControlForm, .editTransferredControlForm");
	var result = {
		didUpdate: false,
		arrayOfControlIDsToDelete: [],
		arrayOfDirtyControlIDs: [],
		validationError: false
	};

	createdControlsForms.each(function () {
		var controlID = AJS.$(this).find("#controlID").val();
		var originalSerialized = originalCreatedControls[controlID];
		var modifiedSerialized = modifiedCreatedControls[controlID];
		if (selectedControls.indexOf(controlID) === -1) {
			if (originalSerialized !== modifiedSerialized) {
				AJS.$(this).trigger("submit");
				if (checkForValidationError()) {
					result.validationError = true;
					result.didUpdate = false;
					result.arrayOfControlIDsToDelete = [];
					return result;
				}
				else {
					result.didUpdate = true;
				}
			}
		}
		else {
			result.arrayOfControlIDsToDelete.push(controlID);
			if (originalSerialized !== modifiedSerialized) {
				result.arrayOfDirtyControlIDs.push(controlID);
			}
		}
	});

	return result;
}

function addNewControlFormIsDirty() {
	if (AJS.$("#controlDescriptionNew").val() === "" &&
		AJS.$("#controlGroupNew").val() === "1" &&
		AJS.$("#controlCausesNewms2side__dx").children().length === 0) {
		return false;
	}
	else {
		return true;
	}
}

function checkForNewControlAddition(newControlRequired) {
	var result = {
		didNew: false,
		validationError: false
	};

	if (newControlRequired) {
		AJS.$("#addNewControlForm").trigger("submit");
		if (checkForValidationError()) {
			result.validationError = true;
			result.didNew = false;
			return result;
		}
		else {
			result.didNew = true;
		}
	}
	else {
		if (addNewControlFormIsDirty()) {
			AJS.$("#addNewControlForm").trigger("submit");
			if (checkForValidationError()) {
				result.validationError = true;
				result.didNew = false;
				return result;
			}
			else {
				result.didNew = true;
			}
		}
	}

	return result;
}

function checkForNewControlTransfer() {
	var hazardID = AJS.$("#controlHazardList").val();
	var controlID = AJS.$("#controlCauseList").val();
	var result = {
		didTransfer: false,
		validationError: false
	};

	if (hazardID !== undefined && controlID !== undefined) {
		if (hazardID !== "" && controlID !== "") {
			AJS.$("#transferControlForm").trigger("submit");
			if (checkForValidationError()) {
				result.validationError = true;
				result.didTransfer = false;
				return result;
			}
			else {
				result.didTransfer = true;
			}
		}
	}

	return result;
}

function serializeCreatedControls() {
	var rtn = {};
	var createdControls = AJS.$(".editControlForm, .editTransferredControlForm");
	createdControls.each(function () {
		var controlID = AJS.$(this).find("[name='controlID']").val();
		var serialized = AJS.$(this).serialize();
		rtn[controlID] = serialized;
	});
	return rtn;
}

AJS.$(document).ready(function(){
	createControlsCookie();
	openControlsInCookie();
	createAssociatedControlCookie();
	createUpdateMessageCookie();
	checkUpdateMessageCookie();
	uncheckSelectedControls();

	var expanding = true;
	expanding = checkIfExpandButtonNeedsRenaming(expanding);
	var createdControls = serializeCreatedControls();

	/* Text manipulation code begins */
	manipulateHazardTextForControls();
	manipulateControlDates();
	manipulateControlTextDescriptions();
	/* Text manipulation code ends */

	/* Expand functionality code begins */
	AJS.$(".ControlsTableCellToggle").live("click", function() {
		var entry = AJS.$(this);
		var entryFullID = entry.attr("id");
		var entryID = entryFullID.replace( /^\D+/g, '');
		var entryEdit = AJS.$("#ControlsTableEditEntry" + entryID);
		if (entryEdit.hasClass("ControlsTableEditEntryHidden")) {
			entryEdit.removeClass("ControlsTableEditEntryHidden");
			entry.removeClass("aui-icon aui-icon-small aui-iconfont-add");
			entry.addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
			updateControlsCookie("add", entryID);
		}
		else {
			entryEdit.addClass("ControlsTableEditEntryHidden");
			entry.removeClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
			entry.addClass("aui-icon aui-icon-small aui-iconfont-add");
			updateControlsCookie("delete", entryID);
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
				var entryID = entryFullID.replace( /^\D+/g, '');
				var entryEdit = AJS.$("#ControlsTableEditEntry" + entryID);
				if (entryEdit.hasClass("ControlsTableEditEntryHidden")) {
					entryEdit.removeClass("ControlsTableEditEntryHidden");
					entry.removeClass("aui-icon aui-icon-small aui-iconfont-add");
					entry.addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
					updateControlsCookie("add", entryID);
				}
			});
			expanding = false;
		}
		else {
			AJS.$(this).html("Expand all");
			createdControls.each(function () {
				var entry = AJS.$(this);
				var entryFullID = entry.attr("id");
				var entryID = entryFullID.replace( /^\D+/g, '');
				var entryEdit = AJS.$("#ControlsTableEditEntry" + entryID);
				if (!entryEdit.hasClass("ControlsTableEditEntryHidden")) {
					entryEdit.addClass("ControlsTableEditEntryHidden");
					entry.removeClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
					entry.addClass("aui-icon aui-icon-small aui-iconfont-add");
					updateControlsCookie("delete", entryID);
				}
			});
			expanding = true;
		}
	});

	AJS.$("#addNewControl").live("click", function() {
		if (AJS.$(this).hasClass("aui-iconfont-add")) {
			AJS.$(this).removeClass("aui-iconfont-add");
			AJS.$(this).addClass("aui-iconfont-devtools-task-disabled");
			AJS.$(".ControlsNewContainer").show();
		}
		else {
			AJS.$(this).removeClass("aui-iconfont-devtools-task-disabled");
			AJS.$(this).addClass("aui-iconfont-add");
			AJS.$(".ControlsNewContainer").hide();
		}
	});

	AJS.$("#addTransferControl").live("click", function() {
		if (AJS.$(this).hasClass("aui-iconfont-add")) {
			manipulateTextForHazardSelectionInControls();
			AJS.$(this).removeClass("aui-iconfont-add");
			AJS.$(this).addClass("aui-iconfont-devtools-task-disabled");
			AJS.$(".ControlsTransferContainer").show();
		}
		else {
			AJS.$(this).removeClass("aui-iconfont-devtools-task-disabled");
			AJS.$(this).addClass("aui-iconfont-add");
			AJS.$(".ControlsTransferContainer").hide();
		}
	});
	/* Expand functionality code ends */

	/* Updating existing controls functinality begins */
	AJS.$(".SaveAllChanges").live("click", function() {
		var newControlRequired = AJS.$(this).data("new");

		var updateExistingControlsResults = checkForUpdatesToExistingControls(createdControls);
		if (updateExistingControlsResults.validationError) { return; }

		var newControlResult = checkForNewControlAddition(newControlRequired);
		if (newControlResult.validationError) { return; }

		var transferControlResult = checkForNewControlTransfer();
		if (transferControlResult.validationError) { return; }

		if (updateExistingControlsResults.arrayOfControlIDsToDelete.length !== 0 ||
			updateExistingControlsResults.arrayOfDirtyControlIDs.length !== 0) {
			var doRefreshAfterDelete = false;
			if (updateExistingControlsResults.didUpdate || newControlResult.didNew) {
				doRefreshAfterDelete = true;
			}
			deleteSelectedControls(doRefreshAfterDelete, updateExistingControlsResults.arrayOfControlIDsToDelete,
				updateExistingControlsResults.arrayOfDirtyControlIDs);
		}
		else {
			if (updateExistingControlsResults.didUpdate || newControlResult.didNew ||
				transferControlResult.didTransfer) {
				updateUpdateMessageCookie("updates");
				location.reload();
			}
			else {
				JIRA.Messages.showWarningMsg("No changes have been made.", {closeable: true});
			}
		}
	});

	AJS.$("#ConfirmDialogDuplBtnControls").live("click", function() {
		var reasonTextFields = AJS.$(".ConfirmDialogReasonTextControls");
		var reasonToDuplicate;
		var noReasonGiven = false;
		var controlID;
		reasonTextFields.each(function (index) {
			if (index === 0) {
				reasonToDuplicate = AJS.$(this).val();
				if (reasonToDuplicate === "") {
					controlID = AJS.$(this).attr("id").replace( /^\D+/g, '');
					addErrorMessageToSpecificControl(controlID, "For the control above, please provide a short delete reason.");
					noReasonGiven = true;
				}
			}
			else {
				if (noReasonGiven) {
					controlID = AJS.$(this).attr("id").replace( /^\D+/g, '');
					removeErrorMessageFromSpecificControl(controlID);
				}
				else {
					AJS.$(this)[0].value = reasonToDuplicate;
				}
			}
		});
	});

	AJS.$(".ConfirmDialogReasonTextControls").live("input", function() {
		var controlID = AJS.$(this).attr("id").replace( /^\D+/g, '');
		removeErrorMessageFromSpecificControl(controlID);
	});
	/* Updating existing controls functinality ends */

	/* Clearing controls functionality begins */
	AJS.$("#clearNewControlButton").live("click", function() {
		AJS.$("#controlDescriptionNew").val("");
		AJS.$("#controlGroupNew").val('').trigger('chosen:updated');

		var multiSelectElement = AJS.$("#addNewControlMultiSelect");
		var removeAllBtn = AJS.$(multiSelectElement).find(".RemoveAll");
		AJS.$(removeAllBtn).trigger("click");
	});
	AJS.$("#clearNewControlButton").trigger("click");

	AJS.$("#clearTransferControlButton").live("click", function() {
		AJS.$(".TransferControlCauseContainer").hide();
		AJS.$(".TransferControlCauseContainer").children().remove();
		AJS.$(".TransferControlControlContainer").hide();
		AJS.$(".TransferControlControlContainer").children().remove();
	});
	/* Clearing controls functionality end */

	/* Transfer control functionality begins */
	AJS.$("#controlHazardList").live("change reset", function() {
		AJS.$("div.TransferControlCauseContainer").children().remove();
		AJS.$("div.TransferControlControlContainer").hide();
		AJS.$("div.TransferControlControlContainer").children().remove();

		var selectedHazardID = AJS.$(this).val();
		if(selectedHazardID.length) {
			var causeListForSelectedHazard;
			AJS.$.ajax({
				type:"GET",
				async: false,
				url: AJS.params.baseURL + "/rest/htsrest/1.0/report/allcauses/" + selectedHazardID,
				success: function(data) {
					causeListForSelectedHazard = data;
				}
			});
			AJS.$(".TransferControlCauseContainer").show();
			var temp = "<label class='popupLabels' for='controlCauseList'>Hazard Causes</label><select class='select long-field' name='controlCauseList' id='controlCauseList'>";
			if(causeListForSelectedHazard.length > 0) {
				temp += "<option value=''>-Select Cause-</option>";
				AJS.$(causeListForSelectedHazard).each(function() {
					var causeNumberAndTitle;
					if (this.transfer === true) {
						causeNumberAndTitle = this.causeNumber + "-T - " + this.title;
					}
					else {
						causeNumberAndTitle = this.causeNumber + " - " + this.title;
					}
					temp += "<option value=" + this.causeID + ">" + manipulateControlTextVariableLength(causeNumberAndTitle, 85) + "</option>";
				});
				temp += "</select>";
				AJS.$("div.TransferControlCauseContainer").append(temp);
			}
			else {
				AJS.$("div.TransferControlCauseContainer").append("<span class='TransferNotPossibleText'>The Hazard Report has no Causes. No Control Transfer can be created.</span>");
			}
		}
		else {
			AJS.$(".TransferControlCauseContainer").hide();
			AJS.$(".TransferControlControlContainer").hide();
		}
	}).trigger('change');

	AJS.$("#controlCauseList").live("change reset", function() {
		AJS.$("div.TransferControlControlContainer").children().remove();
		AJS.$("div.TransferControlControlContainer").show();
		var selectedCauseID = AJS.$(this).val();
		if(selectedCauseID.length) {
			var controlListForSelectedCause;
			AJS.$.ajax({
				type:"GET",
				async: false,
				url: AJS.params.baseURL + "/rest/htsrest/1.0/report/cause/allcontrols/" + selectedCauseID,
				success: function(data) {
					controlListForSelectedCause = data;
				}
			});
			AJS.$(".TransferControlControlContainer").show();
			var temp = "<label class='popupLabels' for='controlControlList'>Hazard Controls</label><select class='select long-field' name='controlControlList' id='controlControlList'>";
			if(controlListForSelectedCause.length > 0) {
				temp += "<option value=''>-Link to all controls in selected cause-</option>";
				AJS.$(controlListForSelectedCause).each(function() {
					var controlNumberAndDescription = this.controlNumber + " - " + this.description;
					temp += "<option value=" + this.controlID + ">" + manipulateControlTextVariableLength(controlNumberAndDescription, 85) + "</option>";
				});
				temp += "</select>";
				AJS.$("div.TransferControlControlContainer").append(temp);
				AJS.$(".TransferControlControlContainer option").tsort();
			}
			else {
				AJS.$("div.TransferControlControlContainer").append("<label class='popupLabels' for='controlControlList'>Hazard Controls</label><div class='TransferNoProperties'>-Link to all Controls in selected Cause- (Selected Cause currently has no Controls)</div>");
			}
		}
		else {
			AJS.$(".TransferControlControlContainer").hide();
		}
	}).trigger('change');
	/* Transfer control functionality ends */

	/* Expand / scroll functionality begins */
	var whichForm;
	if (AJS.$.url().data.seg.path.length === 4) {
		whichForm = AJS.$.url().data.seg.path[3];
	}
	else {
		whichForm = AJS.$.url().data.seg.path[2];
	}

	if (whichForm === "controlform") {
		var IDOfControlToBeOpen = AJS.$.url().param("id");
		if (IDOfControlToBeOpen) {
			var controlToBeOpened = AJS.$(".ControlsTableEntryControlID" + IDOfControlToBeOpen)[1].children[0];
			if (controlToBeOpened.classList.contains("ControlsTableEditEntryHidden")) {
				controlToBeOpened.classList.remove("ControlsTableEditEntryHidden");
				var buttonForTheControl = AJS.$(".ControlsTableEntryControlID" + IDOfControlToBeOpen)[0].children[0];
				buttonForTheControl.classList.remove("aui-iconfont-add");
				buttonForTheControl.classList.add("aui-iconfont-devtools-task-disabled");
			}
		}

		if (AJS.$.url().param("trans") === "y") {
			AJS.$('html, body').animate({
				scrollTop: AJS.$(".ControlsTableEntryControlID" + IDOfControlToBeOpen).offset().top
			}, 50);
		}

		var associatedCause = getAssociatedCauseCookie();
		if (associatedCause !== "none") {
			var associatedCauses = AJS.$("#controlCausesNewms2side__sx").children();
			associatedCauses.each(function () {
				AJS.$(this).prop("selected", false);
				if (AJS.$(this).val() === associatedCause) {
					AJS.$(this).prop("selected", true);
					AJS.$(this).trigger("dblclick");
				}
			});

			AJS.$('html, body').animate({
				scrollTop: AJS.$("#addNewControl").offset().top
			}, 50);
			AJS.$("#addNewControl").trigger("click");

			updateAssociatedCauseCookie("none");
		}
	}
	/* Expand / scroll functionality ends */

	AJS.$("#controlAddVerification").live("click", function(e) {
		var controlIDAndHazardIDArr = AJS.$(this).data("key").split("-");
		var controlID = controlIDAndHazardIDArr[0];
		var hazardID = controlIDAndHazardIDArr[1];
		updateAssociatedControlCookie(controlID);
		window.location.href = AJS.params.baseURL + "/plugins/servlet/verificationform?edit=y&key=" + hazardID;
	});

});