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
			// Call function to do CSS magic, send the controlID...
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
		}
		else {
			oldSelectedControls = result.skippedReasonControlIDs;
			for (var j = 0; j < result.skippedReasonControlIDs.length; j++) {
				addErrorMessageToSpecificControl(result.skippedReasonControlIDs[j]);
			}
		}
	});
}

AJS.$(document).ready(function(){

	// CSS fixes (on predefined JIRA element)>
	AJS.$('form.aui').css({'margin':'0'});

	/* Expand functionality code begins */
	AJS.$('.ControlsTableCellToggle').click(function() {
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
	});
	/* Expand functionality code ends */

	/* Text manipulation code begins */
	var dates = AJS.$(".ControlDate");
	manipulateDates(dates);
	/* Text manipulation code ends */

	AJS.$(".ControlsTableListOfCreatedControls").live("click", function() {
		var createdControls = AJS.$(".ControlsTableListOfCreatedControls");
		var selectedControls = getSelectedControls(createdControls);
		if (selectedControls.length >= 1) {
			AJS.$("#DeleteControl").prop('disabled', false);
		}
		else {
			AJS.$("#DeleteControl").prop('disabled', true);
		}
	});

	AJS.$("#DeleteControl").live("click", function() {
		var createdControls = AJS.$(".ControlsTableListOfCreatedControls");
		var selectedControls = getSelectedControls(createdControls);
		var hazardInformation = getHazardInformation();
		deleteSelectedControls(selectedControls, hazardInformation);
	});

});