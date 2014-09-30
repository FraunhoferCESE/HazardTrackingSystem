	/**********************************************************
	*                                                         *
	*            Form - editing and saving related            *
	*                                                         *
	***********************************************************/

function getCookieValue(id) {
	return AJS.$.cookie("show-" + getTheHazardNumber() + "-" + id);
}

function createCookie(id, type) {
	AJS.$.cookie("show-" + getTheHazardNumber() + "-" + id, type, { expires: 1 });
}

function createAssociatedCauseCookie() {
	if (AJS.Cookie.read("ASSOCIATED_CAUSE") === undefined) {
		AJS.Cookie.save("ASSOCIATED_CAUSE", "none");
	}
}

function updateAssociatedCauseCookie(theCauseID) {
	AJS.Cookie.save("ASSOCIATED_CAUSE", theCauseID);
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

function createRiskMatrixCauseCookie() {
	if (AJS.Cookie.read("RISK_MATRIX_CAUSE") === undefined) {
		AJS.Cookie.save("RISK_MATRIX_CAUSE", "none");
	}
}

function updateRiskMatrixCauseCookie(theCauseID) {
	AJS.Cookie.save("RISK_MATRIX_CAUSE", theCauseID);
}

function checkRiskMatrixCauseCookie() {
	if (AJS.Cookie.read("RISK_MATRIX_CAUSE") !== undefined) {
		var idOfCauseOpen = AJS.Cookie.read("RISK_MATRIX_CAUSE");
		if (idOfCauseOpen !== "none") {
			if(idOfCauseOpen) {
				closeAllDivs();
				createCookie(idOfCauseOpen, "expanded");
				openDivOnReload();
			}

			AJS.$('html, body').animate({
				scrollTop: AJS.$("#CausesTableEntry" + idOfCauseOpen).offset().top
			}, 50);

			AJS.Cookie.save("RISK_MATRIX_CAUSE", "none");
		}
	}
}

function checkIfElementIsVisible(element) {
	return element.is(":visible");
}

function changeButtonText() {
	if(checkIfAllDivsAreOpen()) {
		AJS.$("#expandAll").html("Close all");
	}
	else {
		AJS.$("#expandAll").html("Expand all");
	}
}

function addExpandedClass(element) {
	AJS.$(element).removeClass().addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
}

function addCollapsedClass(element) {
	AJS.$(element).removeClass().addClass("aui-icon aui-icon-small aui-iconfont-add");
}

function openDivOnReload() {
	AJS.$(".formContainer").each(function() {
		var spanElement = AJS.$(this).parent().find(".trigger").children();
		if(getCookieValue(this.id) != "collapsed" && typeof(getCookieValue(this.id))!="undefined") {
			addExpandedClass(spanElement);
			AJS.$(this).show();
		}
		else {
			addCollapsedClass(spanElement);
			AJS.$(this).hide();
		}
	});
	changeButtonText();
}

function checkIfAllDivsAreOpen() {
	return (AJS.$(".formContainer").length === AJS.$(".formContainer:visible").length);
}


function openAllDivs() {
	AJS.$(".rowGroup .formContainer").each(function() {
		createCookie(this.id, "expanded");
	});
}

function closeAllDivs() {
	AJS.$(".rowGroup .formContainer").each(function() {
		createCookie(this.id, "collapsed");
	});
}

function dateLayout() {
	var lastUpdated = AJS.$(".lastUpdated");
	if(lastUpdated.length > 0) {
		lastUpdated.each(function () {
			var dateToBeInserted = Date.parse(AJS.$(this).text().substring(0,19)).toString("MMMM dd, yyyy, HH:mm");
			AJS.$(this).text(dateToBeInserted);
		});
	}
}

function manipulateHazardTextForCauses() {
	if (AJS.$("#HazardTitleForCause").text().length >= 128) {
		var shortend1 = AJS.$("#HazardTitleForCause").text().substring(0,125) + "...";
		AJS.$("#HazardTitleForCause").text(shortend1);
	}
	if (AJS.$("#HazardNumberForCause").text().length >= 128) {
		var shortend2 = AJS.$("#HazardNumberForCause").text().substring(0,125) + "...";
		AJS.$("#HazardNumberForCause").text(shortend2);
	}
}

function getTheHazardNumber() {
	return AJS.$("#HazardNumberForCause").text();
}

function uncheckCauses(arrayOfCauseIDs) {
	if (arrayOfCauseIDs === undefined) {
		// uncheck everything
		var checkboxes = AJS.$(".deleteCause");
		checkboxes.each(function () {
			if (AJS.$(this).is(":checked")) {
				AJS.$(this).prop("checked", false);
			}
		});
	}
	else {
		// uncheck specific
		for (var i = 0; i < arrayOfCauseIDs.length; i++) {
		var checkboxElement = AJS.$("input[data-causeid='" + arrayOfCauseIDs[i] + "']");
			if (AJS.$(checkboxElement).is(":checked")) {
				AJS.$(checkboxElement).prop("checked", false);
			}
		}
	}
}

function getSelectedCausesAndDeleteReasons(arrayOfCauseIDs) {
	var selectedCausesAndDeleteReasons = [];
	var skippedReasonCauses = [];
	var skippedReason = false;
	for (var i = 0; i < arrayOfCauseIDs.length; i++) {
		var deleteReason = AJS.$("#ReasonTextForCauseID" + arrayOfCauseIDs[i]).val();
		if (deleteReason === "") {
			skippedReason = true;
			skippedReasonCauses.push(arrayOfCauseIDs[i]);
		} else {
			selectedCausesAndDeleteReasons.push({
				causeID: arrayOfCauseIDs[i],
				deleteReason: deleteReason
			});
		}
	}

	if (skippedReason) {
		return { allReasonsFilledOut: false, skippedReasonCauseIDs: skippedReasonCauses };
	} else {
		return { allReasonsFilledOut: true, causeIDsAndReasons: selectedCausesAndDeleteReasons };
	}
}

function sendAjaxRequestToDeleteSpecificCause(causeID, deleteReason) {
	AJS.$.ajax({
		type: "DELETE",
		async: false,
		url: "causeform?key=" + causeID + "&reason=" + deleteReason,
		success: function(data) {
			console.log("SUCCESS");
			var causeFormElement = AJS.$("form[data-key='" + causeID + "']");
			causeFormElement.removeDirtyWarning();
		},
		error: function(data) {
			console.log("error", arguments);
		}
	});
}

function addErrorMessageToSpecificCause(causeID, message) {
	AJS.$("#ConfirmDialogErrorTextForCauseID" + causeID).text(message);
	AJS.$("#ConfirmDialogErrorTextForCauseID" + causeID).show();
}

function removeErrorMessageFromSpecificCause(causeID) {
	if (AJS.$("#ConfirmDialogErrorTextForCauseID" + causeID).is(":visible")) {
		AJS.$("#ConfirmDialogErrorTextForCauseID" + causeID).hide();
		AJS.$("#ConfirmDialogErrorTextForCauseID" + causeID).text("");
	}
}

function getHazardInformationInCauses() {
	var hazardInformation = {};
	hazardInformation.theNumber = AJS.$("#HazardNumberForCause").text();
	hazardInformation.theTitle = AJS.$("#HazardTitleForCause").text();
	hazardInformation.theID = AJS.$("#hazardID").val();
	return hazardInformation;
}

function checkCauseAssociation(arrayOfCauseIDs) {
	var arrayOfCauseIDsStr = arrayOfCauseIDs.toString();
	var associatedCauses = [];
	AJS.$.ajax({
		type: "GET",
		async: false,
		url: AJS.params.baseURL + "/rest/htsrest/1.0/report/causeAssociations/" + arrayOfCauseIDsStr,
		success: function(data) {
			console.log("SUCCESS");
			associatedCauses = data;
		},
		error: function() {
			console.log("ERROR");
		}
	});
	console.log(associatedCauses);
	return associatedCauses;
}

function associatedCausesArrayContains(associatedCauses, causeID) {
	var rtn = null;
	for (var i = 0; i < associatedCauses.length; i++) {
		if (associatedCauses[i].targetCauseID.toString() === causeID) {
			rtn = associatedCauses[i];
		}
	}
	return rtn;
}

function deleteSelectedCauses(doRefresh, arrayOfCauseIDs, arrayOfDirtyCauseIDs) {
	var associatedCauses = checkCauseAssociation(arrayOfCauseIDs);
	// Hazard specific mark-up:
	var hazardInformation = getHazardInformationInControls();
	var dialogContent1 = "<span class='ConfirmDialogHeadingOne'>Hazard Title: <span class='ConfirmDialogHeadingOneContent'>" +
						manipulateCauseTextVariableLength(hazardInformation.theTitle, 64) +
						"</span></span><span class='ConfirmDialogHeadingOne'>Hazard #: <span class='ConfirmDialogHeadingOneContent'>" +
						manipulateCauseTextVariableLength(hazardInformation.theNumber, 64) +
						"</span></span>";
	// Cause specific mark-up:
	var dialogContent2;
	if (arrayOfCauseIDs === 1) {
		dialogContent2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>The following cause will be deleted from the above hazard report. In order to complete the deletion, you will need to provide a short delete reason.</span></div>";
	}
	else {
		dialogContent2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>The following causes will be deleted from the above hazard report. In order to complete the deletion, you will need to provide a short delete reason for each of the causes.</span></div>";
	}
	// Causes specific mark-up, list of causes to be deleted:
	var dialogContent3 = "<table><thead><tr><th class='ConfirmDialogTableHeader ConfirmDialogTableCellOneCauses'>#</th><th class='ConfirmDialogTableHeader ConfirmDialogTableCellTwoCauses'>Title:</th><th class='ConfirmDialogTableHeader ConfirmDialogTableCellThreeCauses'>Owner:</th></tr></thead><tbody>";
	for (var i = 0; i < arrayOfCauseIDs.length; i++) {
		var causeElement = AJS.$("#CausesTableEntry" + arrayOfCauseIDs[i]);
		dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConformDialogTopRow'></div></td></tr>";
		dialogContent3 = dialogContent3 + "<tr><td>" + AJS.$(causeElement).find(".trigger").text().replace("Cause ", "") + "</td>";
		dialogContent3 = dialogContent3 + "<td><div class='ConfirmDialogDescriptionText'>" + AJS.$(causeElement).find(".CausesTableTitleText").text() + "</div></td>";
		dialogContent3 = dialogContent3 + "<td>" + AJS.$(causeElement).find(".CausesTableOwnerText").text() + "</td></tr>";

		if (i === 0 && arrayOfCauseIDs.length > 1) {
			dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConfirmDialogLabelContainer'><label for='ReasonTextForCause'>Reason<span class='aui-icon icon-required '>(required)</span></label></div><div class='ConfirmDialogReasonTextContainer'><input type='text' class='ConfirmDialogReasonTextCauses' name='ReasonTextForCause' id='ReasonTextForCauseID" +
							arrayOfCauseIDs[i] + "'></div><div class='ConfirmDialogDuplButtonContainer'><button class='aui-button ConfirmDialogDuplButton' id='ConfirmDialogDuplBtnCauses'>Apply to all</button></div></td></tr>";
		}
		else {
			dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConfirmDialogLabelContainer'><label for='ReasonTextForCause'>Reason<span class='aui-icon icon-required '>(required)</span></label></div><div class='ConfirmDialogReasonTextContainerNoButton'><input type='text' class='ConfirmDialogReasonTextCauses' name='ReasonTextForCause' id='ReasonTextForCauseID" +
							arrayOfCauseIDs[i] + "'></div></td></tr>";
		}

		var associatedCause = associatedCausesArrayContains(associatedCauses, arrayOfCauseIDs[i]);
		if (associatedCause !== null) {
			dialogContent3 = dialogContent3 +
							"<tr>" +
								"<td colspan='100%'>" +
									"<p class='ConfirmDialogErrorText'>Warning: This cause is the target of a transfer:</p>" +
								"</td>" +
							"</tr>";
			dialogContent3 = dialogContent3 +
							"<tr>" +
								"<td colspan='100%'>" +
									"<p class='ConfirmDialogErrorText ConfirmDialogHazardAssociationText'>" +
										"<a href='hazardlist?edit=y&key=" + associatedCause.hazardID + "'>Hazard " + associatedCause.hazardNumber + "</a>" +
										" (owned by " + associatedCause.hazardOwner + "): ";
			var whichLoops = {
				causes: false,
				controls: false
			};
			if (associatedCause.transferType === "BOTH") {
				whichLoops.causes = true;
				whichLoops.controls = true;
			}
			else if (associatedCause.transferType === "CAUSE-CAUSE" ) {
				whichLoops.causes = true;
			}
			else {
				whichLoops.controls = true;
			}

			if (whichLoops.causes) {
				for (var k = 0; k < associatedCause.originCauses.length; k++) {
					var comma1 = ", ";
					if (k === (associatedCause.originCauses.length - 1)) {
						comma1 = "";
					}

					dialogContent3 = dialogContent3 +
									"<a href='causeform?edit=y&key=" + associatedCause.hazardID + "' class='openAssociatedCause' data-causeid='" + associatedCause.originCauses[k].originCauseID + "'>" +
											"Cause " + associatedCause.originCauses[k].originCauseNumber +
									"</a>" + comma1;
				}
			}
			if (whichLoops.controls) {
				if (whichLoops.causes) {
					dialogContent3 = dialogContent3 + ", ";
				}
				for (var l = 0; l < associatedCause.originControls.length; l++) {
					var comma2 = ", ";
					if (l === (associatedCause.originControls.length - 1)) {
						comma2 = "";
					}

					dialogContent3 = dialogContent3 +
									"<a href='controlform?edit=y&key=" + associatedCause.hazardID + "' class='openAssociatedControl' data-controlid='" + associatedCause.originControls[l].originControlID + "'>" +
										"Control " + associatedCause.originControls[l].originControlNumber +
									"</a>" + comma2;
				}
			}

			dialogContent3 = dialogContent3 + "</p></td></tr>";
		}

		if (arrayOfDirtyCauseIDs.indexOf(arrayOfCauseIDs[i]) !== -1) {
			dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><p class='ConfirmDialogErrorText'>This cause has been edited. All changes will be discarded.</p></td></tr>";
		}

		dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><p class='ConfirmDialogErrorText ConfirmDialogErrorTextHidden' id='ConfirmDialogErrorTextForCauseID" + arrayOfCauseIDs[i] +"'></p></td></tr>";
	}
	dialogContent3 = dialogContent3 + "<tr><td colspan='100%'><div class='ConformDialogTopRow'></div></td></tr></tbody></table>";

	var dialog = new AJS.Dialog({
		width: 600,
		height: 475,
		id: "deleteDialog",
	});

	dialog.show();
	dialog.addHeader("Confirm");
	dialog.addPanel("Panel 1",
		"<div class='panelBody'>" + dialogContent1 + dialogContent2 + dialogContent3 + "</div>",
		"panel-body");
	dialog.get("panel:0").setPadding(0);

	dialog.addButton("Cancel", function(dialog) {
		uncheckCauses(arrayOfCauseIDs);
		dialog.hide();
		dialog.remove();
		if (doRefresh) {
			updateUpdateMessageCookie("updates");
			location.reload();
		}
	});

	dialog.addButton("Continue", function(dialog) {
		var result = getSelectedCausesAndDeleteReasons(arrayOfCauseIDs);
		if (result.allReasonsFilledOut) {
			for (var i = 0; i < result.causeIDsAndReasons.length; i++) {
				sendAjaxRequestToDeleteSpecificCause(result.causeIDsAndReasons[i].causeID, result.causeIDsAndReasons[i].deleteReason);
			}
			dialog.hide();
			updateUpdateMessageCookie("updates");
			location.reload();
		}
		else {
			for (var j = 0; j < result.skippedReasonCauseIDs.length; j++) {
				addErrorMessageToSpecificCause(result.skippedReasonCauseIDs[j], "For the cause above, please provide a short delete reason.");
			}
		}
	});
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

function checkForUpdatesToExistingCauses(originalCreatedCauses) {
	var modifiedCreatedCauses = serializeCreatedCauses();
	var result = {
		didUpdate: false,
		arrayOfCauseIDsToDelete: [],
		arrayOfDirtyCauseIDs: [],
		validationError: false
	};

	AJS.$("form.causeForms, form.transferredForms").each(function(){
		var causeID = AJS.$(this).find("[name='key']").val();
		var rowGroup = AJS.$(this).parent().parent().parent();
		var originalSerialized;
		var modifiedSerialized;
		if(rowGroup.find(".deleteCause").is(':checked')) {
			result.arrayOfCauseIDsToDelete.push(causeID);
			originalSerialized = originalCreatedCauses[causeID];
			modifiedSerialized = modifiedCreatedCauses[causeID];
			if (originalSerialized !== modifiedSerialized) {
				result.arrayOfDirtyCauseIDs.push(causeID);
			}
		}
		else {
			originalSerialized = originalCreatedCauses[causeID];
			modifiedSerialized = modifiedCreatedCauses[causeID];
			if (originalSerialized !== modifiedSerialized) {
				AJS.$(this).trigger("submit");
				if (checkForValidationError()) {
					result.validationError = true;
					result.didUpdate = false;
					result.arrayOfCauseIDsToDelete = [];
					return result;
				}
				else {
					result.didUpdate = true;
				}
			}
		}
	});

	return result;
}

function addNewCauseFormIsDirty() {
	var formElement = AJS.$("#addNewCauseForm");
	if (AJS.$(formElement).find("#causeTitle").val() === "" &&
		AJS.$(formElement).find("#causeOwner").val() === "" &&
		AJS.$(formElement).find("#causeRisk").val() === "" &&
		AJS.$(formElement).find("#causeLikelihood").val() === "" &&
		AJS.$(formElement).find("#causeEffects").val() === "" &&
		AJS.$(formElement).find("#causeDescription").val() === "") {
		return false;
	}
	else {
		return true;
	}
}

function checkForNewCauseAddition(newCauseRequired) {
	var result = {
		didNew: false,
		validationError: false
	};

	if (newCauseRequired) {
		AJS.$("#addNewCauseForm").trigger("submit");
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
		if (addNewCauseFormIsDirty()) {
			AJS.$("#addNewCauseForm").trigger("submit");
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

function checkForNewCauseTransfer() {
	var hazardID = AJS.$("#hazardList").val();
	var result = {
		didTransfer: false,
		validationError: false
	};

	if (hazardID !== undefined) {
		if (hazardID !== "") {
			AJS.$("#transferForm").trigger("submit");
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

function foldable(element, containerClass) {
	var spanElement = AJS.$(element);
	var formCont = AJS.$("." + containerClass);
	if(!(checkIfElementIsVisible(formCont))) {
		addExpandedClass(spanElement);
		formCont.show();
	}
	else {
		addCollapsedClass(spanElement);
		formCont.hide();
	}
}

	/**********************************************************
	*                                                         *
	*               Cause transfer related.                   *
	*                                                         *
	***********************************************************/

function transfer() {
	AJS.$("#transferForm").live("reset", function() {
		AJS.$(".container").hide();
		AJS.$("div.container").children().remove();
	});

	AJS.$("#hazardList").live("change reset", function() {
		var elements = AJS.$("div.container").children().remove();
		var value = AJS.$(this).val();
		var causeList;
		if (value.length) {
			AJS.$.ajax({
				type:"GET",
				async: false,
				url: AJS.params.baseURL + "/rest/htsrest/1.0/report/allcauses/" + value,
				success: function(data) {
					console.log("SUCCESS");
					causeList = data;
				},
				error: function() {
					console.log("ERROR");
				}
			});

			AJS.$(".container").show();
			var temp = "<label class='popupLabels' for='causeList'>Hazard Causes</label><select class='select long-field' name='causeList' id='causeList'>";
			if (causeList.length > 0) {
				temp += "<option value=''>-Link to all Causes in selected Hazard Report-</option>";
				AJS.$(causeList).each(function() {
					var causeNumberAndTitle;
					if (this.transfer === true) {
						causeNumberAndTitle = this.causeNumber + "-T - " + this.title;
					}
					else {
						causeNumberAndTitle = this.causeNumber + " - " + this.title;
					}

					temp += "<option value=" + this.causeID + ">" + manipulateCauseTextVariableLength(causeNumberAndTitle, 85) + "</option>";
				});
				temp += "</select>";
				AJS.$("div.container").append(temp);
			}
			else {
				AJS.$("div.container").append("<label class='popupLabels' for='causeList'>Hazard Causes</label><div class='TransferNoProperties'>-Link to all Causes in selected Hazard Report- (Selected HR currently has no Causes)</div>");
			}
		}
		else {
			AJS.$(".container").hide();
		}

	}).trigger('change');
}

function manipulateCausesTitles() {
	var causesTitles = AJS.$(".CausesTableTitleText");
	if (causesTitles.length > 0) {
		causesTitles.each(function () {
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
						AJS.$(this).children(":first").text(shortend);
					}
				}
			}
		});
	}
}

function manipulateTextForHazardSelectionInCauses(theHazardList) {
	if (theHazardList.children().length > 0) {
		theHazardList.children().each(function (index) {
			if (AJS.$(this).text().length >= 85) {
				AJS.$(this).text(AJS.$(this).text().substring(0,82) + "...");
			}
		});
	}
}

function manipulateCauseTextVariableLength(theText, length) {
	if (theText.length >= length){
		return theText.substring(0, (length - 3)) + "...";
	}
	else {
		return theText;
	}
}

function serializeCreatedCauses() {
	var rtn = {};
	var createdCauses = AJS.$(".causeForms, .transferredForms");
	createdCauses.each(function () {
		var causeID = AJS.$(this).find("[name='key']").val();
		var serialized = AJS.$(this).serialize();
		rtn[causeID] = serialized;
	});
	return rtn;
}

function clearNewAndTransferForm() {
	var formElement = AJS.$("#addNewCauseForm");
	AJS.$(formElement).find("#causeTitle").val("");
	AJS.$(formElement).find("#causeOwner").val("");
	AJS.$(formElement).find("#causeRisk").val("").trigger('chosen:updated');
	AJS.$(formElement).find("#causeLikelihood").val("").trigger('chosen:updated');
	AJS.$(formElement).find("#causeDescription").val("");
	AJS.$(formElement).find("#causeEffects").val("");
	AJS.$(formElement).find("#causeAdditSafetyFeatures").val("");
}

AJS.$(document).ready(function() {
	manipulateHazardTextForCauses();
	manipulateCausesTitles();
	getTheHazardNumber();
	dateLayout();
	openDivOnReload();
	transfer();
	uncheckCauses();
	clearNewAndTransferForm();

	createAssociatedCauseCookie();
	createUpdateMessageCookie();
	checkUpdateMessageCookie();
	createRiskMatrixCauseCookie();
	checkRiskMatrixCauseCookie();

	var createdCauses = serializeCreatedCauses();

	AJS.$(".newFormContainer").hide();
	AJS.$(".transferFormContainer").hide();

	AJS.$("#expandAll").live("click", function() {
		if(AJS.$(this).html() === "Close all") {
			AJS.$(".rowGroup .formContainer").hide();
			var spanElement = AJS.$(".trigger").children();
			addCollapsedClass(spanElement);
			closeAllDivs();
		}
		else {
			AJS.$(".rowGroup .formContainer").show();
			var spanElement = AJS.$(".trigger").children();
			addExpandedClass(spanElement);
			openAllDivs();
		}
		changeButtonText();
	});

	AJS.$(".trigger").live("click", function() {
		var spanElement = AJS.$(this).children();
		var spanClass = spanElement.attr("class");
		var formCont = AJS.$(this).parent().parent().find(".formContainer");
		if(!(checkIfElementIsVisible(formCont))) {
			addExpandedClass(spanElement);
			formCont.show();
			createCookie(formCont.attr("id"), "expanded");
		}
		else {
			addCollapsedClass(spanElement);
			formCont.hide();
			createCookie(formCont.attr("id"), "collapsed");
		}
		changeButtonText();
	});

	AJS.$("#newCauseFormTrigger").live("click", function() {
		foldable(this, "newFormContainer");
	});

	AJS.$("#transferFormTrigger").live("click", function() {
		var causesHazardList = AJS.$("#hazardList");
		manipulateTextForHazardSelectionInCauses(causesHazardList);
		foldable(this, "transferFormContainer");
	});

	AJS.$("#causeAddControl").live("click", function() {
		var causeIDAndHazardIDArr = AJS.$(this).data("key").split("-");
		var causeID = causeIDAndHazardIDArr[0];
		var hazardID = causeIDAndHazardIDArr[1];
		updateAssociatedCauseCookie(causeID);
		window.location.href = AJS.params.baseURL + "/plugins/servlet/controlform?edit=y&key=" + hazardID;
	});

	AJS.$("#ConfirmDialogDuplBtnCauses").live("click", function() {
		var reasonTextFields = AJS.$(".ConfirmDialogReasonTextCauses");
		var reasonToDuplicate;
		var noReasonGiven = false;
		var causeID;
		reasonTextFields.each(function (index) {
			if (index === 0) {
				reasonToDuplicate = AJS.$(this).val();
				if (reasonToDuplicate === "") {
					causeID = AJS.$(this).attr("id").replace( /^\D+/g, '');
					addErrorMessageToSpecificCause(causeID, "For the cause above, please provide a short delete reason.");
					noReasonGiven = true;
				}
			}
			else {
				if (noReasonGiven) {
					causeID = AJS.$(this).attr("id").replace( /^\D+/g, '');
					removeErrorMessageFromSpecificCause(causeID);
				}
				else {
					AJS.$(this)[0].value = reasonToDuplicate;
				}
			}
		});
	});

	AJS.$(".ConfirmDialogReasonTextCauses").live("input", function() {
		var causeID = AJS.$(this).attr("id").replace( /^\D+/g, '');
		removeErrorMessageFromSpecificCause(causeID);
	});

	AJS.$(".causeSaveAllChanges").live('click', function() {
		var newCauseRequired = AJS.$(this).data("new");

		var updateExistingCausesResult = checkForUpdatesToExistingCauses(createdCauses);
		if (updateExistingCausesResult.validationError) { return; }

		var newCauseResult = checkForNewCauseAddition(newCauseRequired);
		if (newCauseResult.validationError) { return; }

		var transferCauseResult = checkForNewCauseTransfer();
		if (transferCauseResult.validationError) { return; }

		if (updateExistingCausesResult.arrayOfCauseIDsToDelete.length !== 0) {
			var doRefreshAfterDelete = false;
			if (updateExistingCausesResult.didUpdate || newCauseResult.didNew || transferCauseResult.didTransfer) {
				doRefreshAfterDelete = true;
			}
			deleteSelectedCauses(doRefreshAfterDelete, updateExistingCausesResult.arrayOfCauseIDsToDelete,
								updateExistingCausesResult.arrayOfDirtyCauseIDs);
		}
		else {
			if (updateExistingCausesResult.didUpdate || newCauseResult.didNew || transferCauseResult.didTransfer) {
				updateUpdateMessageCookie("updates");
				location.reload();
			}
			else {
				JIRA.Messages.showWarningMsg("No changes have been made.", {closeable: true});
			}
		}
	});

	AJS.$(".openAssociatedCause").live('click', function() {
		var causeID = AJS.$(this).data("causeid");
		updateRiskMatrixCauseCookie(causeID);
	});

	var whichForm;
	if (AJS.$.url().data.seg.path.length === 4) {
		whichForm = AJS.$.url().data.seg.path[3];
	}
	else {
		whichForm = AJS.$.url().data.seg.path[2];
	}

	if (whichForm === "causeform") {
		var idOfCauseOpen = AJS.$.url().param("id");
		if(idOfCauseOpen) {
			closeAllDivs();
			createCookie(idOfCauseOpen, "expanded");
			openDivOnReload();
		}
		if (AJS.$.url().param("trans") === "y") {
			AJS.$('html, body').animate({
				scrollTop: AJS.$("#CausesTableEntry" + idOfCauseOpen).offset().top
			}, 50);
		}
	}

});