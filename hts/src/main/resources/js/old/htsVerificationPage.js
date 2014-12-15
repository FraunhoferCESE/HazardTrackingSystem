function manipulateVerificationDates(dates) {
	if (dates.length > 0) {
		dates.each(function () {
			var dateToBeInserted = Date.parse(AJS.$(this).text().substring(0,19)).toString("MMMM dd, yyyy, HH:mm");
			AJS.$(this).text(dateToBeInserted);
		});
	}
}

function manipulateEstComplDates() {
	var estCompletionDates = AJS.$(".VerificationEstComplDate");
	estCompletionDates.each(function () {
		var defaultDateArr = (AJS.$(this).data("date")).split(" ");
		var defaultDateStr = defaultDateArr[0];
		AJS.$(this)[0].value = defaultDateStr;
	});
}

function manipulateVerificationText(textElement, textCapNum) {
	if (textElement.length > 0) {
		textElement.each(function () {
			if (AJS.$(this).text().length >= textCapNum) {
				var shortend = AJS.$(this).text().substring(0, (textCapNum - 3)) + "...";
				AJS.$(this).text(shortend);
			}
		});
	}
}

function manipulateTextForVerificationDeleteDialog(theText, length) {
	if (theText.length >= length){
		return theText.substring(0, (length - 3)) + "...";
	}
	else {
		return theText;
	}
}

function addNewVerificationFormIsDirty() {
	if (AJS.$("#verificationDescriptionNew").val() === "" &&
		AJS.$("#verificationTypeNew").val() === "" &&
		AJS.$("#verificationRespPartyNew").val() === "" &&
		AJS.$("#verificationComplDateNew").val() === "" &&
		AJS.$("#verificationControlsNewms2side__dx").children().length === 0) {
		return false;
	}
	else {
		return true;
	}
}

function getHazardInformationInVerifications() {
	var hazardInformation = {};
	hazardInformation.theNumber = AJS.$("#HazardNumberForVerification").text();
	hazardInformation.theTitle = AJS.$("#HazardTitleForVerification").text();
	hazardInformation.theID = AJS.$("#hazardID").val();
	return hazardInformation;
}

function getVerificationsToBeDeleted() {
	var verificationsToBeDeleted = AJS.$(".VerificationsTableDeleteCheckbox");
	if (verificationsToBeDeleted.length > 0) {
		var verifications = [];
		verificationsToBeDeleted.each(function () {
			if (AJS.$(this).is(':checked')) {
				verifications.push(this.value);
			}
		});
		return verifications;
	}
}

function uncheckVerificationsToBeDeleted(arrayOfVerificationIDsToDelete) {
	if (arrayOfVerificationIDsToDelete === undefined) {
		// uncheck everything
		var checkboxes = AJS.$(".VerificationsTableDeleteCheckbox");
		checkboxes.each(function () {
			if (AJS.$(this).is(":checked")) {
				AJS.$(this).prop("checked", false);
			}
		});
	}
	else {
		// uncheck specific
		for (var i = 0; i < arrayOfVerificationIDsToDelete.length; i++) {
			var checkboxElement = AJS.$("input[data-verificationid='" + arrayOfVerificationIDsToDelete[i] + "']");
			if (AJS.$(checkboxElement).is(":checked")) {
				AJS.$(checkboxElement).prop("checked", false);
			}
		}
	}
}

function getVerificationsToBeDeletedAndDeleteReasons(selectedVerifications) {
	var selectedVerificationsAndDeleteReasons = [];
	var skippedReasonVerifications = [];
	var skippedReason = false;
	for (var i = 0; i < selectedVerifications.length; i++) {
		var deleteReason = AJS.$("#ReasonTextForVerificationID" + selectedVerifications[i]).val();
		if (deleteReason === "") {
			skippedReason = true;
			skippedReasonVerifications.push(selectedVerifications[i]);
		}
		else {
			selectedVerificationsAndDeleteReasons.push({
				verificationID: selectedVerifications[i],
				deleteReason: deleteReason
			});
		}
	}

	if (skippedReason) {
		return { allReasonsFilledOut: false, skippedReasonVerificationIDs: skippedReasonVerifications };
	}
	else {
		return { allReasonsFilledOut: true, verificationIDAndReasons: selectedVerificationsAndDeleteReasons };
	}
}

function sendAjaxRequestToDeleteSpecificVerification(verificationID, deleteReason) {
	AJS.$.ajax({
		type: "DELETE",
		async: false,
		url: "verificationform?verificationID=" + verificationID + "&reason=" + deleteReason,
		success: function(data) {
			console.log("SUCCESS");
			var verificationFormElement = AJS.$("form[data-key='" + verificationID + "']");
			verificationFormElement.removeDirtyWarning();
		},
		error: function(data) {
			console.log("ERROR");
		}
	});
}

function addErrorMessageToSpecificVerification(verificationID, message) {
	AJS.$("#ConfirmDialogErrorTextForVerificationID" + verificationID).text(message);
	AJS.$("#ConfirmDialogErrorTextForVerificationID" + verificationID).show();
}

function removeErrorMessageFromSpecificVerification(verificationID) {
	if (AJS.$("#ConfirmDialogErrorTextForVerificationID" + verificationID).is(":visible")) {
		AJS.$("#ConfirmDialogErrorTextForVerificationID" + verificationID).hide();
		AJS.$("#ConfirmDialogErrorTextForVerificationID" + verificationID).text("");
	}
}

function deleteSelectedVerifications(doRefresh, arrayOfVerificationIDsToDelete, arrayOfDirtyVerificationIDs) {
	// Hazard specific mark-up:
	var hazardInformation = getHazardInformationInVerifications();
	var dialogContent1 = "<span class='ConfirmDialogHeadingOne'>Hazard Title: <span class='ConfirmDialogHeadingOneContent'>" + manipulateTextForVerificationDeleteDialog(hazardInformation.theTitle, 64) + "</span></span><span class='ConfirmDialogHeadingOne'>Hazard #: <span class='ConfirmDialogHeadingOneContent'>" + manipulateTextForVerificationDeleteDialog(hazardInformation.theNumber, 64) + "</span></span>";
	// Controls specific mark-up:
	var dialogContent2;
	if (arrayOfVerificationIDsToDelete.length === 1) {
		dialogContent2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>The following verification will be deleted from the above hazard report. In order to complete the deletion, you will need to provide a short delete reason.</span></div>";
	} else {
		dialogContent2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>The following verifications will be deleted from the above hazard report. In order to complete the deletion, you will need to provide a short delete reason for each of the verifications.</span></div>";
	}
	// Controls specific mark-up, list of controls to be deleted:
	var dialogContent3 = "<table><thead><tr><th class='ConfirmDialogTableHeader ConfirmDialogTableCellOneVerifications'>#</th><th class='ConfirmDialogTableHeader ConfirmDialogTableCellTwoVerifications'>Description:</th><th class='ConfirmDialogTableHeader ConfirmDialogTableCellThreeVerifications'>Verification Status:</th></tr></thead><tbody>";
	for (var i = 0; i < arrayOfVerificationIDsToDelete.length; i++) {
		var verificationRowElement = AJS.$(".VerificationsTableEntryID" + arrayOfVerificationIDsToDelete[i]).first();
		dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConformDialogTopRow'></div></td></tr>";
		dialogContent3 = dialogContent3 + "<tr><td class='ConfirmDialogTableAllCells'>" + verificationRowElement.children(":nth-child(2)").text().replace("Verification ", "") + "</td>";
		dialogContent3 = dialogContent3 + "<td class='ConfirmDialogTableAllCells'><div class='ConfirmDialogDescriptionText'>" + verificationRowElement.children(":nth-child(3)").text() + "</div></td>";
		dialogContent3 = dialogContent3 + "<td class='ConfirmDialogTableAllCells'>" + verificationRowElement.children(":nth-child(4)").text() + "</td></tr>";

		if (i === 0 && arrayOfVerificationIDsToDelete.length > 1) {
			dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConfirmDialogLabelContainer'><label for='ReasonTextForVerification'>Reason<span class='aui-icon icon-required '>(required)</span></label></div><div class='ConfirmDialogReasonTextContainer'><input type='text' class='ConfirmDialogReasonTextVerifications' name='ReasonTextForVerifiction' id='ReasonTextForVerificationID" + arrayOfVerificationIDsToDelete[i] + "'></div><div class='ConfirmDialogDuplButtonContainer'><button class='aui-button ConfirmDialogDuplButton' id='ConfirmDialogDuplBtnVerifc'>Apply to all</button></div></td></tr>";
		}
		else {
			dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConfirmDialogLabelContainer'><label for='ReasonTextForVerification'>Reason<span class='aui-icon icon-required '>(required)</span></label></div><div class='ConfirmDialogReasonTextContainerNoButton'><input type='text' class='ConfirmDialogReasonTextVerifications' name='ReasonTextForVerification' id='ReasonTextForVerificationID" + arrayOfVerificationIDsToDelete[i] + "'></div></td></tr>";
		}

		if (arrayOfDirtyVerificationIDs.indexOf(arrayOfVerificationIDsToDelete[i]) !== -1) {
			dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><p class='ConfirmDialogErrorText'>This verification has been edited. All changes will be discarded.</p></td></tr>";
		}

		dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><p class='ConfirmDialogErrorText ConfirmDialogErrorTextHidden' id='ConfirmDialogErrorTextForVerificationID" + arrayOfVerificationIDsToDelete[i] +"'></p></td></tr>";
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
		uncheckVerificationsToBeDeleted(arrayOfVerificationIDsToDelete);
		dialog.hide();
		dialog.remove();
		if (doRefresh) {
			updateUpdateMessageCookie("updates");
			location.reload();
		}
	});
	dialog.addButton("Continue", function(dialog) {
		var result = getVerificationsToBeDeletedAndDeleteReasons(arrayOfVerificationIDsToDelete);
		if (result.allReasonsFilledOut) {
			for (var i = 0; i < result.verificationIDAndReasons.length; i++) {
				sendAjaxRequestToDeleteSpecificVerification(result.verificationIDAndReasons[i].verificationID, result.verificationIDAndReasons[i].deleteReason);
			}
			dialog.hide();
			updateUpdateMessageCookie("updates");
			location.reload();
		}
		else {
			for (var j = 0; j < result.skippedReasonVerificationIDs.length; j++) {
				addErrorMessageToSpecificVerification(result.skippedReasonVerificationIDs[j], "For the verification above, please provide a short delete reason.");
			}
		}
	});
}

function checkExpandStatus() {
	var allToggleElements = AJS.$(".VerificationsTableCellToggle");
	var numberOfCreatedVerifications = allToggleElements.length;
	var numberOfOpen = 0;
	var numberOfClosed = 0;

	allToggleElements.each(function () {
		var entry = AJS.$(this);
		var entryFullID = entry.attr("id");
		var entryID = entryFullID.replace( /^\D+/g, '');
		var entryEdit = AJS.$("#VerificationsTableEditEntryID" + entryID);
		if (entryEdit.hasClass("VerificationsTableEditEntryHidden")) {
			numberOfClosed++;
		}
		else {
			numberOfOpen++;
		}
	});

	if (numberOfOpen === numberOfCreatedVerifications) {
		AJS.$("#ExpandAllVerifications").html("Close all");
		return "close all";
	}
	else {
		AJS.$("#ExpandAllVerifications").html("Expand all");
		return "expand all";
	}
}

function createVerificationsCookie() {
	if (AJS.Cookie.read("OPEN_VERIFICATIONS") === undefined) {
		AJS.Cookie.save("OPEN_VERIFICATIONS", "none");
	}
}

function updateVerificationsCookie(operation, entryID) {
	var openVerifications = AJS.Cookie.read("OPEN_VERIFICATIONS");
	if (operation === "add") {
		if (openVerifications === "none") {
			openVerifications = entryID;
		}
		else {
			openVerifications = openVerifications + "," + entryID;
		}
		AJS.Cookie.save("OPEN_VERIFICATIONS", openVerifications);
	}
	else {
		if (openVerifications !== "none") {
			openVerificationsArray = openVerifications.split(',');
			var indexOfEntry = openVerificationsArray.indexOf(entryID);
			if (indexOfEntry > -1) {
				openVerificationsArray.splice(indexOfEntry, 1);
				if (openVerificationsArray.length === 0) {
					AJS.Cookie.save("OPEN_VERIFICATIONS", "none");
				}
				else {
					AJS.Cookie.save("OPEN_VERIFICATIONS", openVerificationsArray.toString());
				}
			}
		}
	}
}

function openVerificationsInCookie() {
	var openVerifications = AJS.Cookie.read("OPEN_VERIFICATIONS");
	if (openVerifications !== "none") {
		openVerificationsArray = openVerifications.split(',');
		for (var i = 0; i < openVerificationsArray.length; i++) {
			var entryEdit = AJS.$("#VerificationsTableEditEntryID" + openVerificationsArray[i]);
			if (entryEdit.hasClass("VerificationsTableEditEntryHidden")) {
				entryEdit.removeClass("VerificationsTableEditEntryHidden");
				var entry = AJS.$("#VerificationsToggleID" + openVerificationsArray[i]);
				entry.removeClass("aui-icon aui-icon-small aui-iconfont-add");
				entry.addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
			}
		}
	}
}

function getAssociatedControlCookie() {
	return AJS.Cookie.read("ASSOCIATED_CONTROL");
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

function checkForUpdatesToExistingVerifications(originalCreatedVerifications) {
	var modifiedCreatedVerifications = serializeCreatedVerifications();
	var verificationsToDelete = getVerificationsToBeDeleted();
	var createdVerificationForms = AJS.$(".editVerificationForm");
	var result = {
		didUpdate: false,
		arrayOfVerificationIDsToDelete: [],
		arrayOfDirtyVerificationIDs: [],
		validationError: false
	};

	createdVerificationForms.each(function () {
		var verificationID = AJS.$(this).find("#verificationID").val();
		var originalSerialized = originalCreatedVerifications[verificationID];
		var modifiedSerialized = modifiedCreatedVerifications[verificationID];
		if (verificationsToDelete.indexOf(verificationID) === -1) {
			console.log(originalSerialized);
			console.log(modifiedSerialized);
			if (originalSerialized !== modifiedSerialized) {
				AJS.$(this).trigger("submit");
				if (checkForValidationError()) {
					result.validationError = true;
					result.didUpdate = false;
					result.arrayOfVerificationIDsToDelete = [];
					return result;
				}
				else {
					result.didUpdate = true;
				}
			}
		}
		else {
			result.arrayOfVerificationIDsToDelete.push(verificationID);
			if (originalSerialized !== modifiedSerialized) {
				result.arrayOfDirtyVerificationIDs.push(verificationID);
			}
		}
	});

	return result;
}

function checkForNewVerificationAddition(newVerificationRequired) {
	var result = {
		didNew: false,
		validationError: false
	};

	if (newVerificationRequired) {
		AJS.$("#addNewVerificationForm").trigger("submit");
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
		if (addNewVerificationFormIsDirty()) {
			AJS.$("#addNewVerificationForm").trigger("submit");
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

function serializeCreatedVerifications() {
	var rtn = {};
	var createdControls = AJS.$(".editVerificationForm");
	createdControls.each(function () {
		var verificationID = AJS.$(this).find("[name='verificationID']").val();
		var serialized = AJS.$(this).serialize();
		rtn[verificationID] = serialized;
	});
	return rtn;
}

AJS.$(document).ready(function() {
	createVerificationsCookie();
	openVerificationsInCookie();
	createUpdateMessageCookie();
	checkUpdateMessageCookie();
	uncheckVerificationsToBeDeleted();

	var expand = checkExpandStatus();

	/* Text manipulation code begins */
	var dates = AJS.$(".VerificationDate");
	manipulateVerificationDates(dates);

	manipulateEstComplDates();

	var descriptionTexts = AJS.$(".VerificationsTableDescriptionText");
	manipulateVerificationText(descriptionTexts, 128);
	/* Text manipulation code ends */

	var createdVerifications = serializeCreatedVerifications();

	/* Updating existing verifications functinality begins */
	AJS.$(".SaveAllVerificationChanges").live("click", function() {
		var newVerificationRequired = AJS.$(this).data("new");

		var updateExistingVerificationsResults = checkForUpdatesToExistingVerifications(createdVerifications);
		if (updateExistingVerificationsResults.validationError) { return; }

		var newVerificationResult = checkForNewVerificationAddition(newVerificationRequired);
		if (newVerificationResult.validationError) { return; }

		if (updateExistingVerificationsResults.arrayOfVerificationIDsToDelete.length !== 0 ||
			updateExistingVerificationsResults.arrayOfDirtyVerificationIDs.length !== 0) {
			var doRefreshAfterDelete = false;
			if (updateExistingVerificationsResults.didUpdate || newVerificationResult.didNew) {
				doRefreshAfterDelete = true;
			}
			deleteSelectedVerifications(doRefreshAfterDelete, updateExistingVerificationsResults.arrayOfVerificationIDsToDelete,
				updateExistingVerificationsResults.arrayOfDirtyVerificationIDs);
		}
		else {
			if (updateExistingVerificationsResults.didUpdate || newVerificationResult.didNew) {
				updateUpdateMessageCookie("updates");
				location.reload();
			}
			else {
				JIRA.Messages.showWarningMsg("No changes have been made.", {closeable: true});
			}
		}
	});

	AJS.$("#clearNewVerificationButton").live("click", function() {
		AJS.$("#verificationDescriptionNew").val("");
		AJS.$("#verificationStatusNew").val("").trigger('chosen:updated');
		AJS.$("#verificationTypeNew").val("").trigger('chosen:updated');
		AJS.$("#verificationRespPartyNew").val("");
		AJS.$("#verificationComplDateNew").val("");

		var multiSelectElement = AJS.$("#addNewVerificationMultiSelect");
		var removeAllBtn = AJS.$(multiSelectElement).find(".RemoveAll");
		AJS.$(removeAllBtn).trigger("click");
	});
	AJS.$("#clearNewVerificationButton").trigger("click");

	AJS.$("#ConfirmDialogDuplBtnVerifc").live("click", function() {
		var reasonTextFields = AJS.$(".ConfirmDialogReasonTextVerifications");
		var reasonToDuplicate;
		var noReasonGiven = false;
		var verificationID;
		reasonTextFields.each(function (index) {
			if (index === 0) {
				reasonToDuplicate = AJS.$(this).val();
				if (reasonToDuplicate === "") {
					verificationID = AJS.$(this).attr("id").replace( /^\D+/g, '');
					addErrorMessageToSpecificVerification(verificationID, "For the verification above, please provide a short delete reason.");
					noReasonGiven = true;
				}
			}
			else {
				if (noReasonGiven) {
					verificationID = AJS.$(this).attr("id").replace( /^\D+/g, '');
					removeErrorMessageFromSpecificVerification(verificationID);
				}
				else {
					AJS.$(this)[0].value = reasonToDuplicate;
				}
			}
		});
	});

	AJS.$(".ConfirmDialogReasonTextVerifications").live("input", function() {
		var verificationID = AJS.$(this).attr("id").replace( /^\D+/g, '');
		removeErrorMessageFromSpecificVerification(verificationID);
	});
	/* Updating existing verifications functinality ends */

	/* Expand functionality code begins */
	AJS.$("#addNewVerification").live("click", function() {
		if (AJS.$(this).hasClass("aui-iconfont-add")) {
			AJS.$(this).removeClass("aui-iconfont-add");
			AJS.$(this).addClass("aui-iconfont-devtools-task-disabled");
			AJS.$(".VerificationsNewContainer").show();
		}
		else {
			AJS.$(this).removeClass("aui-iconfont-devtools-task-disabled");
			AJS.$(this).addClass("aui-iconfont-add");
			AJS.$(".VerificationsNewContainer").hide();
		}
	});

	AJS.$(".VerificationsTableCellToggle").live("click", function() {
		var entry = AJS.$(this);
		var entryFullID = entry.attr("id");
		var entryID = entryFullID.replace( /^\D+/g, '');
		var entryEdit = AJS.$("#VerificationsTableEditEntryID" + entryID);
		if (entryEdit.hasClass("VerificationsTableEditEntryHidden")) {
			entryEdit.removeClass("VerificationsTableEditEntryHidden");
			entry.removeClass("aui-iconfont-add");
			entry.addClass("aui-iconfont-devtools-task-disabled");
			updateVerificationsCookie("add", entryID);
		}
		else {
			entryEdit.addClass("VerificationsTableEditEntryHidden");
			entry.removeClass("aui-iconfont-devtools-task-disabled");
			entry.addClass("aui-iconfont-add");
			updateVerificationsCookie("delete", entryID);
		}
		expand = checkExpandStatus();
	});

	AJS.$("#ExpandAllVerifications").live("click", function() {
		var allToggleElements = AJS.$(".VerificationsTableCellToggle");
		allToggleElements.each(function () {
			var entry = AJS.$(this);
			var entryFullID = entry.attr("id");
			var entryID = entryFullID.replace( /^\D+/g, '');
			var entryEdit = AJS.$("#VerificationsTableEditEntryID" + entryID);

			if (expand === "close all") {
				if (!entryEdit.hasClass("VerificationsTableEditEntryHidden")) {
					entryEdit.addClass("VerificationsTableEditEntryHidden");
					entry.removeClass("aui-iconfont-devtools-task-disabled");
					entry.addClass("aui-iconfont-add");
					updateVerificationsCookie("delete", entryID);
					updateVerificationsCookie("add", entryID);
				}
			}

			if (expand === "expand all") {
				if (entryEdit.hasClass("VerificationsTableEditEntryHidden")) {
					entryEdit.removeClass("VerificationsTableEditEntryHidden");
					entry.removeClass("aui-iconfont-add");
					entry.addClass("aui-iconfont-devtools-task-disabled");
				}
			}
		});

		if (expand === "expand all") {
			expand = "close all";
			AJS.$("#ExpandAllVerifications").html("Close all");
			return;
		}

		if (expand === "close all") {
			expand = "expand all";
			AJS.$("#ExpandAllVerifications").html("Expand all");
			return;
		}
	});

	var whichForm;
	if (AJS.$.url().data.seg.path.length === 4) {
		whichForm = AJS.$.url().data.seg.path[3];
	}
	else {
		whichForm = AJS.$.url().data.seg.path[2];
	}

	if (whichForm === "verificationform" && getAssociatedControlCookie() !== "none") {
		var selectedControlID = getAssociatedControlCookie();
		updateAssociatedControlCookie("none");
		var associatedControls = AJS.$("#verificationControlsNewms2side__sx").children();
		associatedControls.each(function () {
			AJS.$(this).prop("selected", false);
			if (AJS.$(this).val() === selectedControlID) {
				AJS.$(this).prop("selected", true);
				AJS.$(this).trigger("dblclick");
			}
		});

		AJS.$('html, body').animate({
			scrollTop: AJS.$("#addNewVerification").offset().top
		}, 50);
		AJS.$("#addNewVerification").trigger("click");
	}

	/* Expand functionality code ends */

});