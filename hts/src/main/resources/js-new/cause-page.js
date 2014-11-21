console.log("=== cause-page.js ===");

var EXISTING_CAUSES_SERIALIZED = null;

function initializeCausePage() {
	if (INIT.CAUSES === true) {
		INIT.CAUSES = false;
		initCausePageClickEvents();
	}
	initCausePageDateModification();
	EXISTING_CAUSES_SERIALIZED = serializeExistingCauses();

	// Calling functions in shared-cookies.js file
	openHTSCookieOpenCauses(existingCausesCount);
	var existingCausesCount = AJS.$(".CauseTableToggle").length;
	renameCausePageExpandButton(existingCausesCount);
}

function initCausePageClickEvents() {
	// Clear new Cause form
	AJS.$("#CausePageClearNew").live("click", function() {
		var formElement = AJS.$("#CausePageFormAddNew");
		AJS.$(formElement).find("#causeTitle").val("");
		AJS.$(formElement).find("#causeOwner").val("");
		AJS.$(formElement).find("#causeRisk").val("").trigger('chosen:updated');
		AJS.$(formElement).find("#causeLikelihood").val("").trigger('chosen:updated');
		AJS.$(formElement).find("#causeDescription").val("");
		AJS.$(formElement).find("#causeEffects").val("");
		AJS.$(formElement).find("#causeAdditSafetyFeatures").val("");
	});

	// Clear new transfer Cause form
	AJS.$("#CausePageClearTransfer").live("click", function() {
		var formElement = AJS.$("#CausePageFormAddTransfer");
		AJS.$("#CausePageCauseTransferContainer").hide();
		AJS.$("#CausePageCauseTransferContainer").children().remove();
		AJS.$(formElement).find("#transferReason").val("");
		AJS.$(formElement).find("#hazardList").val("").trigger('chosen:updated');
	});

	// Add new cause click event
	AJS.$("#CausePageAddNewCause").live("click", function() {
		// Calling function in shared-cookies.js file
		toggleOpenCloseIcon(AJS.$(this), AJS.$("#CausePageNewContainer"));
	});

	// Add new transfer click event
	AJS.$("#CausePageAddTransfer").live("click", function() {
		// Calling function in shared-cookies.js file
		toggleOpenCloseIcon(AJS.$(this), AJS.$("#CausePageTransferContainer"));
	});

	// Open/close on existing cause
	AJS.$(".CauseTableToggle").live("click", function() {
		var elementID = AJS.$(this).parent().parent().attr("id");
		var causeID = elementID.split("CauseTableEntryID")[1];
		var displayElement = AJS.$("#CauseTableEntryContentID" + causeID);
		var operation = toggleOpenCloseIcon(AJS.$(this), displayElement);
		var existingCausesCount = AJS.$(".CauseTableToggle").length;
		// Calling function in shared-cookies.js file
		modifyHTSCookieOpenCauses(operation, causeID, existingCausesCount);
	});

	// Expand All/Close All causes
	AJS.$("#CausePageExpandAllButton").live("click", function() {
		var operation = AJS.$(this).val();
		var buttonElements = AJS.$(".CauseTableToggle");
		var existingCausesCount = AJS.$(".CauseTableToggle").length;
		if (operation === "Expand All") {
			buttonElements.each(function () {
				if (AJS.$(this).hasClass("aui-iconfont-add")) {
					AJS.$(this).removeClass("aui-iconfont-add");
					AJS.$(this).addClass("aui-iconfont-devtools-task-disabled");
					var elementID = AJS.$(this).parent().parent().attr("id");
					var causeID = elementID.split("CauseTableEntryID")[1];
					AJS.$("#CauseTableEntryContentID" + causeID).show();
					// Calling function in shared-cookies.js file
					modifyHTSCookieOpenCauses("open", causeID, null);
				}
			});
		} else {
			buttonElements.each(function () {
				if (AJS.$(this).hasClass("aui-iconfont-devtools-task-disabled")) {
					AJS.$(this).removeClass("aui-iconfont-devtools-task-disabled");
					AJS.$(this).addClass("aui-iconfont-add");
					var elementID = AJS.$(this).parent().parent().attr("id");
					var causeID = elementID.split("CauseTableEntryID")[1];
					AJS.$("#CauseTableEntryContentID" + causeID).hide();
					// Calling function in shared-cookies.js file
					modifyHTSCookieOpenCauses("close", causeID, null);
				}
			});
		}
		// Calling function in shared-cookies.js file
		renameCausePageExpandButton(existingCausesCount);
	});

	// Save new cause
	AJS.$(".CausePageSaveAllChanges").live("click", function() {
		var result = {
			existingPost: false,
			existingDelete: false,
			existingErrors: false,
			existingNoChanges: false,
			addNewPost: false,
			addNewErrors: false,
			addNewNoChanges: false,
			addTransferPost: false,
			addTransferErrors: false,
			addTransferNoChanges: false
		};

		var operation = AJS.$(this).data("ops");
		if (operation === "new" || operation === "all") {
			var addNewResult = addNewCauseFormValidation();
			if (addNewResult.dirty === true && addNewResult.validated === true) {
				//console.log("post form");
				result.addNewPost = true;
				postFormToCauseServlet(AJS.$("#CausePageFormAddNew"));
			} else if (addNewResult.dirty === true && addNewResult.validated === false) {
				//console.log("errors");
				result.addNewErrors = true;
			} else if (addNewResult.dirty === false && addNewResult.validated === false) {
				//console.log("no changes made");
				result.addNewNoChanges = true;
			}
		}

		if (operation === "transfer" || operation === "all") {
			var addTransferResult = addTransferCauseFormValidation();
			if (addTransferResult.dirty === true && addTransferResult.validated === true) {
				console.log("post form");
				result.addTransferPost = true;
				postFormToCauseServlet(AJS.$("#CausePageFormAddTransfer"));
			} else if (addTransferResult.dirty === true && addTransferResult.validated === false) {
				//console.log("errors");
				result.addTransferErrors = true;
			} else if (addTransferResult.dirty === false && addTransferResult.validated === false) {
				//console.log("no changes made");
				result.addTransferNoChanges = true;
			}
		}

		var existingResult = existingCauseFormValidation();
		if (existingResult.validated === true) {
			result.existingErrors = false;
		} else {
			result.existingErrors = true;
		}
		if (existingResult.validated === true &&
			existingResult.modifiedExistingCausesIDs.length === 0 &&
			existingResult.deleteExistingCausesIDs.length === 0 &&
			existingResult.modifiedDeleteExistingCausesIDs.length === 0) {
			result.existingNoChanges = true;
		}
		if (existingResult.modifiedExistingCausesIDs.length !== 0) {
			console.log("post existing");
			result.existingPost = true;
			for (var i = 0; i < existingResult.modifiedExistingCausesIDs.length; i++) {
				var formElement = AJS.$("input[name='causeID'][value='" + existingResult.modifiedExistingCausesIDs[i] +"']").closest("form");
				console.log(formElement);
				postFormToCauseServlet(formElement);
			}
		}
		if (existingResult.deleteExistingCausesIDs.length !== 0) {
			//console.log("delete existing");
			result.existingDelete = true;
			openDeleteCauseDialog(existingResult.deleteExistingCausesIDs, result);
		} else {
			// Display appropriate message and load the template again to see the changes
			displayAppropriateMessage(result);
		}
	});

	// Transfer click event
	AJS.$("#hazardList").live("change reset", function() {
		var causeContainer = AJS.$("#CausePageCauseTransferContainer");
		AJS.$(causeContainer).children().remove();
		var hazardID = AJS.$(this).val();
		if (hazardID !== "") {
			var causes = getAllCausesWithinHazard(hazardID);
			var html = "<label class='popupLabels' for='causeList'>Hazard Causes</label><select class='select long-field' name='causeList' id='causeList'>";
			if (causes.length !== 0) {
				html += "<option value='0'>-Link to all Causes in selected Hazard Report-</option>";
				for (var i = 0; i < causes.length; i++) {
					var optionText;
					if (causes[i].transfer === true) {
						optionText = causes[i].causeNumber + "-T - " + causes[i].title;
					} else {
						optionText = causes[i].causeNumber + " - " + causes[i].title;
					}
					html += "<option value=" + causes[i].causeID + ">" + manipulateTextLength(optionText, 85) + "</option>";
				}
				html += "</select>";
				AJS.$(causeContainer).append(html);
			} else {
				AJS.$(causeContainer).append("<label class='popupLabels' for='causeList'>Hazard Causes</label><div class='TransferNoProperties'>-Link to all Causes in selected Hazard Report- (Selected HR currently has no Causes)</div>");

			}
			AJS.$(causeContainer).show();
		} else {
			AJS.$(causeContainer).hide();
		}
	});

	AJS.$("#ConfirmDialogDuplBtnCauses").live("click", function() {
		var reasonTextFields = AJS.$(".ConfirmDialogReasonTextInput");
		var reasonToDuplicate;
		reasonTextFields.each(function (index) {
			if (index === 0) {
				reasonToDuplicate = AJS.$(this).val();
				if (reasonToDuplicate === "") {
					var causeID = AJS.$(this).attr("id").replace( /^\D+/g, '');
					var errorElement = AJS.$("#ConfirmDialogErrorTextForCauseID" + causeID);
					AJS.$(errorElement).text("For the Cause above, please provide a short delete reason.");
					AJS.$(errorElement).show();
					return false;
				}
			}
			else {
				AJS.$(this)[0].value = reasonToDuplicate;
			}
		});
	});
}

function initCausePageDateModification() {
	AJS.$(".HTSDate").each(function() {
		var dateStrUnformatted = AJS.$(this).text();
		var dateStrFormatted = formatDate(dateStrUnformatted);
		AJS.$(this).text(dateStrFormatted);
	});
}

function serializeExistingCauses() {
	if (AJS.$("#CausePageTable").is(":visible")) {
		var serializedObj = {};
		AJS.$(".CausePageFormExisting").each(function () {
			var causeID = AJS.$(this).find("[name='causeID']").val();
			var serialized = AJS.$(this).serialize();
			serializedObj[causeID] = serialized;
		});
		return serializedObj;
	} else {
		return null;
	}
}

function existingCauseFormValidation() {
	var modifiedExistingCausesIDs = [];
	var deleteExistingCausesIDs = [];
	var modifiedDeleteExistingCausesIDs = [];
	var validated = true;

	AJS.$(".CausePageFormExisting").each(function () {
		var causeID = AJS.$(this).find("[name='causeID']").val();
		var deleteSelected = AJS.$(".CausePageDeleteBox[value='" + causeID + "']").is(':checked');
		var oldSerialized = EXISTING_CAUSES_SERIALIZED[causeID];
		var newSerialized = AJS.$(this).serialize();
		if (newSerialized !== oldSerialized) {
			if (newSerialized.indexOf("causeTitle=&") > -1) {
				if (deleteSelected === false) {
					AJS.$(this).find("[data-error='causeTitle']").show();
					validated = false;
				}
			} else {
				if (deleteSelected === false) {
					modifiedExistingCausesIDs.push(causeID);
				} else {
					modifiedDeleteExistingCausesIDs.push(causeID);
				}
			}
		}
		if (deleteSelected === true) {
			deleteExistingCausesIDs.push(causeID);
		}
	});

	return {"validated" : validated,
			"modifiedExistingCausesIDs" : modifiedExistingCausesIDs,
			"deleteExistingCausesIDs" : deleteExistingCausesIDs,
			"modifiedDeleteExistingCausesIDs" : modifiedDeleteExistingCausesIDs};
}

function addNewCauseFormValidation() {
	var formElement = AJS.$("#CausePageFormAddNew");
	var dirty = false;
	var validated = false;
	if (AJS.$(formElement).find("#causeTitle").val() !== "" ||
		AJS.$(formElement).find("#causeOwner").val() !== "" ||
		AJS.$(formElement).find("#causeRisk").val() !== "" ||
		AJS.$(formElement).find("#causeLikelihood").val() !== "" ||
		AJS.$(formElement).find("#causeEffects").val() !== "" ||
		AJS.$(formElement).find("#causeDescription").val() !== "") {
		dirty = true;
	}
	if (dirty === true) {
		if (AJS.$(formElement).find("#causeTitle").val() === "") {
			AJS.$(formElement).find("[data-error='causeTitle']").show();
		} else {
			validated = true;
		}
	}
	return {"dirty" : dirty, "validated" : validated};
}

function addTransferCauseFormValidation() {
	var formElement = AJS.$("#CausePageFormAddTransfer");
	var dirty = false;
	var validated = false;

	var hazardListElement = AJS.$(formElement).find("#hazardList").val();
	var transferReasonElement = AJS.$(formElement).find("#transferReason").val();

	if (hazardListElement !== undefined && transferReasonElement !== undefined) {
		if (hazardListElement !== "" ||
			transferReasonElement !== "") {
			dirty = true;
		}
		if (dirty === true) {
			if (hazardListElement === "") {
				AJS.$(formElement).find("[data-error='hazardList']").show();
			} else {
				validated = true;
			}
		}
		return {"dirty" : dirty, "validated" : validated};
	} else {
		return {"dirty" : false, "validated" : false};
	}
}

function postFormToCauseServlet(formElement) {
	console.log(formElement);
	AJS.$(formElement).ajaxSubmit({
		async: false,
		success: function(data) {
			console.log("SUCCESS");
			console.log(data);
		},
		error: function(error) {
			// TODO:
			// Getting an object here which contains the error message
			// Display similar message as in success, but of the error kind
			console.log("ERROR");
		}
	});
}

function displayAppropriateMessage(result) {
	if (result.existingNoChanges === true && result.addNewNoChanges === true &&
		result.addTransferNoChanges === true) {
		JIRA.Messages.showWarningMsg("No changes were made.", {closeable: true});
	}
	if (result.existingPost === true || result.addNewPost === true ||
		result.addTransferPost === true || result.existingDelete) {
		var successMessage = "The following changes were made:<br>";
		if (result.addNewPost === true) {
			successMessage += "<b> &#149; Created a new Cause<b><br>";
		}
		if (result.addTransferPost === true) {
			successMessage += "<b> &#149; Created a new transferred Cause<b><br>";
		}
		if (result.existingPost === true) {
			successMessage += "<b> &#149; Updated existing Cause(s)<b><br>";
		}
		if (result.existingDelete === true) {
			successMessage += "<b> &#149; Deleted existing Cause(s)<b><br>";
		}
		JIRA.Messages.showSuccessMsg(successMessage, {closeable: true});
		var path = AJS.$.url().data.attr.relative;
		loadTemplate(path);

	}
	if (result.existingErrors === true || result.addNewErrors === true ||
		result.addTransferErrors === true) {
		var errorMessage = "There was a problem with the following:<br>";
		if (result.addNewErrors === true) {
			errorMessage += "<b> &#149; Creating a new Cause<b><br>";
		}
		if (result.addTransferErrors === true) {
			errorMessage += "<b> &#149; Creating a new transferred Cause.<b><br>";
		}
		if (result.existingErrors === true) {
			errorMessage += "<b> &#149; Updating existing Cause(s)<b><br>";
		}
		JIRA.Messages.showErrorMsg(errorMessage, {closeable: true});
	}
}

function removeAnyVisibleErrorsFromForm(formElement) {
	var errors = AJS.$(formElement).find(".HTSRequiredContainer");
	errors.each(function() {
		if (AJS.$(this).is(":visible")) {
			AJS.$(this).hide();
		}
	});
}

// The following functions have to do with deleting Causes
function openDeleteCauseDialog(causeIDsToDelete, result) {
	var html1 = "<span class='ConfirmDialogHeadingOne'>Hazard Title: <span class='ConfirmDialogHeadingOneContent'>" + "Title" + "</span></span>" +
				"<span class='ConfirmDialogHeadingOne'>Hazard #: <span class='ConfirmDialogHeadingOneContent'>" + "Number" + "</span></span>";
	var html2;
	if (causeIDsToDelete.length === 1) {
		html2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>" +
					"The following Cause will be deleted from the above Hazard Report. In order to complete the deletion, you will need to provide a short delete reason." +
				"</span></div>";
	} else {
		html2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>" +
					"The following Causes will be deleted from the above Hazard Report. In order to complete the deletion, you will need to provide a short delete reason for each of the Causes." +
				"</span></div>";
	}
	var html3 = "<table>" +
					"<thead>" +
						"<tr>" +
							"<th class='ConfirmDialogTableHeader ConfirmDialogTableCellOne'>#</th>" +
							"<th class='ConfirmDialogTableHeader ConfirmDialogTableCellTwo'>Title</th>" +
							"<th class='ConfirmDialogTableHeader ConfirmDialogTableCellThree'>Owner:</th>" +
						"</tr>" +
					"</thead>" +
					"<tbody>";
	for (var i = 0; i < causeIDsToDelete.length; i++) {
		var causeFirstRow = AJS.$("#CauseTableEntryID" + causeIDsToDelete[i]);
		html3 += "<tr><td colspan='100%' class='ConfirmDialogTableNoBorder'><div class='ConformDialogEmptyRow'></div></td></tr>";
		html3 += "<tr><td class='ConfirmDialogTableNoBorder'>" + causeFirstRow.children(":nth-child(2)").text().replace("Cause ", "") + "</td>";
		html3 += "<td class='ConfirmDialogTableNoBorder'><div class='ConfirmDialogDescriptionText'>" + causeFirstRow.children(":nth-child(3)").text() + "</div></td>";
		html3 += "<td class='ConfirmDialogTableNoBorder'>" + causeFirstRow.children(":nth-child(4)").text() + "</td></tr>";

		if (i === 0 && causeIDsToDelete.length > 1) {
			html3 += "<tr>" +
						"<td colspan='100%' class='ConfirmDialogTableNoBorder'>" +
							"<div class='ConfirmDialogLabelContainer'>" +
								"<label for='ReasonTextForCause'><span class='HTSRequired'>* </span>Reason</label>" +
							"</div>" +
							"<div class='ConfirmDialogReasonTextContainer'>" +
								"<input type='text' class='ConfirmDialogReasonTextInput' name='ReasonTextForCause' id='ReasonTextForCauseID" + causeIDsToDelete[i] + "'>" +
							"</div>" +
							"<div class='ConfirmDialogDuplButtonContainer'>" +
								"<button class='aui-button ConfirmDialogDuplButton' id='ConfirmDialogDuplBtnCauses'>Apply to all</button>" +
							"</div>" +
						"</td>" +
					"</tr>";
		} else {
			html3 += "<tr>" +
						"<td colspan='100%' class='ConfirmDialogTableNoBorder'>" +
							"<div class='ConfirmDialogLabelContainer'>" +
								"<label for='ReasonTextForCause'><span class='HTSRequired'>* </span>Reason</label>" +
							"</div>" +
							"<div class='ConfirmDialogReasonTextContainerNoButton'>" +
								"<input type='text' class='ConfirmDialogReasonTextInput' name='ReasonTextForCause' id='ReasonTextForCauseID" + causeIDsToDelete[i] + "'>" +
							"</div>" +
						"</td>" +
					"</tr>";
		}
		html3 += "<tr>" +
					"<td colspan='100%' class='ConfirmDialogTableNoBorder'>" +
						"<p class='ConfirmDialogErrorText ConfirmDialogErrorTextHidden' id='ConfirmDialogErrorTextForCauseID" + causeIDsToDelete[i] +"'></p>" +
					"</td>" +
				"</tr>";
	}
	html3 += "<tr><td colspan='100%' class='ConfirmDialogTableNoBorder'><div class='ConformDialogEmptyRow'></div></td></tr></tbody></table>";

	var dialog = new AJS.Dialog({
		width: 600,
		height: 475,
		id: "deleteDialog",
	});

	dialog.show();
	dialog.addHeader("Confirm");
	dialog.addPanel("Panel 1",
		"<div class='panelBody'>" + html1 + html2 + html3 + "</div>",
		"panel-body");
	dialog.get("panel:0").setPadding(10);

	dialog.addButton("Cancel", function(dialog) {
		dialog.hide();
		dialog.remove();
		deselectAllCauses();
		result.existingDelete = false;
		displayAppropriateMessage(result);
	});

	dialog.addButton("Continue", function(dialog) {
		var validated = deleteCauseFormValidation(causeIDsToDelete);
		if (validated === true) {
			for (var i = 0; i < causeIDsToDelete.length; i++) {
				postDeleteToCauseServlet(causeIDsToDelete[i]);
			}
			dialog.hide();
			dialog.remove();
			displayAppropriateMessage(result);
		}
	});

	dialog.show();
}

function postDeleteToCauseServlet(causeIDToDelete) {
	AJS.$.ajax({
		type: "DELETE",
		async: false,
		url: "causes?id=" + causeIDToDelete + "&reason=" + "FOOBAR",
		success: function(data) {
			console.log("SUCCESS");
		},
		error: function(data) {
			console.log("ERROR");
		}
	});
}

function deleteCauseFormValidation(causeIDsToDelete) {
	var validated = true;
	for (var i = 0; i < causeIDsToDelete.length; i++) {
		var deleteReason = AJS.$("#ReasonTextForCauseID" + causeIDsToDelete[i]).val();
		var errorElement = AJS.$("#ConfirmDialogErrorTextForCauseID" + causeIDsToDelete[i]);
		if (deleteReason === "") {
			AJS.$(errorElement).text("For the Cause above, please provide a short delete reason.");
			AJS.$(errorElement).show();
			validated = false;
		} else {
			if (AJS.$(errorElement).is(":visible")) {
				AJS.$(errorElement).text("");
				AJS.$(errorElement).hide();
			}
		}
	}
	return validated;
}

function deselectAllCauses() {
	var checkboxes = AJS.$(".CausePageDeleteBox:checked");
	checkboxes.each(function () {
		AJS.$(this).attr("checked", false);
	});
}