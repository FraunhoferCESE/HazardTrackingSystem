function manipulateDates(dates) {
	if (dates.length > 0) {
		dates.each(function () {
			if (AJS.$(this)[0].innerText != "N/A") {
				AJS.$(this)[0].innerText = Date.parse(AJS.$(this)[0].innerText.substring(0,19)).toString("MM/dd/yyyy, HH:mm");
			}
		});
	}
}

function manipulateControlText(controlDescriptions) {
	if (controlDescriptions.length > 0) {
		controlDescriptions.each(function () {
			var shortend;
			if (AJS.$(this)[0].children.length === 0) {
				if (AJS.$(this)[0].innerText.length >= 128) {
					shortend = (AJS.$(this)[0].innerText).substring(0, 125) + "...";
					AJS.$(this)[0].innerText = shortend;
				}
			}
			else {
				var shortendArr = (AJS.$(this)[0].innerText).split(" - ");
				if (shortendArr.length === 2) {
					if (shortendArr[1].length >= 128) {
						shortend = shortendArr[1].substring(0, 125) + "...";
						AJS.$(this)[0].children[0].innerText = shortendArr[0] + " - " + shortend;
					}
				}
				else {
					if (AJS.$(this)[0].innerText.length >= 128) {
						shortend = (AJS.$(this)[0].innerText).substring(0, 125) + "...";
						AJS.$(this)[0].children[0].innerText = shortend;
					}
				}
			}
		});
	}
}

function manipulateTextForOptionInControls(theText) {
	if (theText.length >= 85){
		return theText.substring(0,82) + "...";
	}
	else {
		return theText;
	}
}

function manipulateTextForHazardSelectionInControls(theHazardList) {
	if (theHazardList[0].children.length > 1) {
		(theHazardList.children()).each(function (index) {
			if ((AJS.$(this)[0].innerText).length >= 85) {
				AJS.$(this)[0].text = (AJS.$(this)[0].innerText).substring(0,82) + "...";
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

function addErrorMessageToSpecificControl(controlID, message) {
	AJS.$("#ConfirmDialogErrorTextForControlID" + controlID)[0].innerHTML = message;
	AJS.$("#ConfirmDialogErrorTextForControlID" + controlID).show();
}

function removeErrorMessageFromSpecificControl(controlID) {
	if (AJS.$("#ConfirmDialogErrorTextForControlID" + controlID).is(":visible")) {
		AJS.$("#ConfirmDialogErrorTextForControlID" + controlID).hide();
		AJS.$("#ConfirmDialogErrorTextForControlID" + controlID)[0].innerHTML = "";
	}
}

function getHazardInformation() {
	var hazardInformation = {};
	hazardInformation.theNumber = AJS.$("#HazardNumberForControl").text();
	hazardInformation.theTitle = AJS.$("#HazardTitleForControl").text();
	hazardInformation.theID = AJS.$("#hazardID").val();
	return hazardInformation;
}

function deleteSelectedControls(selectedControls, hazardInformation, doRefresh){
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
	var dialogContent3 = "<table><thead><tr><th class='ConfirmDialogTableHeader ConfirmDialogTableCellOneControls'>#</th><th class='ConfirmDialogTableHeader ConfirmDialogTableCellTwoControls'>Description</th><th class='ConfirmDialogTableHeader ConfirmDialogTableCellThreeControls'>Control group:</th></tr></thead><tbody>";
	for (var i = 0; i < selectedControls.length; i++) {
		var controlElementFirstRow = AJS.$(".ControlsTableEntryControlID" + selectedControls[i])[0];
		dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConformDialogTopRow'></div></td></tr>";
		dialogContent3 = dialogContent3 + "<tr><td>" + (controlElementFirstRow.children[1].innerText).replace("Control ", "") + "</td>";
		dialogContent3 = dialogContent3 + "<td><div class='ConfirmDialogDescriptionText'>" + controlElementFirstRow.children[2].innerText + "</div></td>";
		dialogContent3 = dialogContent3 + "<td>" + controlElementFirstRow.children[3].innerText + "</td></tr>";

		if (i === 0 && selectedControls.length > 1) {
			dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConfirmDialogLabelContainer'><label for='ReasonTextForControl'>Reason<span class='aui-icon icon-required '>(required)</span></label></div><div class='ConfirmDialogReasonTextContainer'><input type='text' class='ConfirmDialogReasonTextControls' name='ReasonTextForControl' id='ReasonTextForControlID" + selectedControls[i] + "'></div><div class='ConfirmDialogDuplButtonContainer'><button class='aui-button ConfirmDialogDuplButton' id='ConfirmDialogDuplBtnControls'>Apply to all</button></div></td></tr>";
		}
		else {
			dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConfirmDialogLabelContainer'><label for='ReasonTextForControl'>Reason<span class='aui-icon icon-required '>(required)</span></label></div><div class='ConfirmDialogReasonTextContainerNoButton'><input type='text' class='ConfirmDialogReasonTextControls' name='ReasonTextForControl' id='ReasonTextForControlID" + selectedControls[i] + "'></div></td></tr>";
		}
		dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><p class='ConfirmDialogErrorText ConfirmDialogErrorTextHidden' id='ConfirmDialogErrorTextForControlID" + selectedControls[i] +"'></p></td></tr>";
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
		if (doRefresh) {
			location.reload();
		}
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

AJS.$(document).ready(function(){
	createControlsCookie();
	openControlsInCookie();
	createAssociatedControlCookie();

	var expanding = true;
	expanding = checkIfExpandButtonNeedsRenaming(expanding);
	var oldCausesAssociatedWithControl = getCurrentControlAndCausesAssociation();

	/* Text manipulation code begins */
	var dates = AJS.$(".ControlDate");
	manipulateDates(dates);
	var controlDescriptions = AJS.$(".ControlsTableDescriptionText");
	manipulateControlText(controlDescriptions);
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
			var controlsHazardList = AJS.$("#controlHazardList");
			//manipulateTextForHazardSelectionInControls(controlsHazardList);
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
	AJS.$(".SaveAllChanges").live("click", function(e) {
		e.preventDefault();
		var newCausesAssociatedWithControl = getCurrentControlAndCausesAssociation();
		var createdControls = AJS.$(".ControlsTableListOfCreatedControls");
		var selectedControls = getSelectedControls(createdControls);
		var createdControlsForms = AJS.$(".editControlForm");
		var createdControlTransfersForms = AJS.$(".editTransferredControlForm");
		var hazardInformation = getHazardInformation();
		var doDelete = false;
		var doUpdate = false;
		var doNew = false;
		var doTransfer = false;
		var doRefresh = false;
		var validationError = false;

		// Check for updates to controls or delete requests
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

		// Check for updates to transferred controls or delete requests
		createdControlTransfersForms.each(function () {
			var controlID = this.children[1].value;
			if (selectedControls.indexOf(controlID) === -1) {
				if (AJS.$(this).isDirty()) {
					AJS.$(this).trigger("submit");
					doUpdate = true;
				}
			}
			else {
				doDelete = true;
			}
		});

		validationError = AJS.$(".validationError").is(":visible");
		if (validationError) {
			JIRA.Messages.showWarningMsg("Not all changes have been saved. See invalid forms below.", {closeable: true});
			return;
		}

		// Check for new control

		if (AJS.$("#addNewControlForm").isDirty() || (AJS.$("#addNewControlForm")[0][6].children.length !== 0)) {
			AJS.$("#addNewControlForm").trigger("submit");
			doNew = true;
		}

		validationError = AJS.$(".validationError").is(":visible");
		if (validationError) {
			JIRA.Messages.showWarningMsg("Not all changes have been saved. See invalid forms below.", {closeable: true});
			return;
		}

		// Check for control transfer
		var hazardID = AJS.$("#controlHazardList").val();
		var controlID = AJS.$("#controlCauseList").val();
		if(hazardID !== undefined && controlID !== undefined) {
			AJS.$("#transferControlForm").trigger("submit");
			doTransfer = true;
		}

		if (doDelete) {
			if (doUpdate || doNew || doTransfer) {
				doRefresh = true;
			}
			deleteSelectedControls(selectedControls, hazardInformation, doRefresh);
			return;
		}

		if (doUpdate || doNew || doTransfer) {
			location.reload();
			return;
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
		AJS.$(".RemoveAll").trigger("click");
	});

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
					var causeNumberAndTitle = this.causeNumber + " - " + this.title;
					temp += "<option value=" + this.causeID + ">" + manipulateTextForOptionInControls(causeNumberAndTitle) + "</option>";
				});
				AJS.$("div.TransferControlCauseContainer").append(temp);
			}
			else {
				AJS.$("div.TransferControlCauseContainer").append("<p>This Hazard Report has no Causes.</p>");
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
					temp += "<option value=" + this.controlID + ">" + manipulateTextForOptionInControls(controlNumberAndDescription) + "</option>";
				});
				AJS.$("div.TransferControlControlContainer").append(temp);
			}
			else {
				AJS.$("div.TransferControlControlContainer").append("<p>This Cause has no Controls.</p>");
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