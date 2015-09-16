console.log("=== cause-page.js ===");

var EXISTING_CAUSES_SERIALIZED = null;

function initializeCausePage() {
	AJS.toInit(function()  {
		if (INIT.CAUSES === true) {
			INIT.CAUSES = false;
			initCausePageClickEvents();
		}
		EXISTING_CAUSES_SERIALIZED = serializeExistingCauses();
	
		// Calling functions in shared-cookies.js file
		var existingCausesCount = AJS.$(".CauseTableToggle").length;
		openHTSCookieOpenCauses(existingCausesCount);
		renameCausePageExpandButton(existingCausesCount);
	});
}

function initCausePageClickEvents() {
	
	//when doing a cause transfer, automatically expand the targetcause on the cause page
	AJS.$(".transferLink").click(function(event) {
	    // Get the link that fired the click event
		var targetID = AJS.$(this).attr("targetID");
	
	    //initHTSCookie();
	    modifyHTSCookieOpenCauses("open", targetID, null);
	    // CAll the shared-cookes.js code that will set the user's cookie to expand Cause Number on HazardNumber 
	});
	
	// Clear new Cause form
	AJS.$("#CausePageClearNew").live("click", function() {
		var formElement = AJS.$("#CausePageFormAddNew");
		AJS.$(formElement).find("#causeOwner").val("");
		AJS.$(formElement).find("#causeRisk").val("").trigger('chosen:updated');
		AJS.$(formElement).find("#causeLikelihood").val("").trigger('chosen:updated');
		AJS.$(formElement).find("#causeDescription").val("");
		AJS.$(formElement).find("#causeEffects").val("");
		AJS.$(formElement).find("#causeAdditSafetyFeatures").val("");
	});

	// Clear new transfer Cause form
	AJS.$("#CausePageClearTransfer").live("click", function() {
		AJS.$("#CausePageCauseTransferContainer").hide();
		AJS.$("#CausePageCauseTransferContainer").children().remove();
		var formElement = AJS.$("#CausePageFormAddTransfer");
		AJS.$(formElement).find("#transferReason").val("");
		AJS.$(formElement).find("#causeHazardList").val("").trigger('chosen:updated');
	});

	// Add new Cause click event
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
				result.addNewPost = true;
				postFormToCauseServlet(AJS.$("#CausePageFormAddNew"));
			} else if (addNewResult.dirty === true && addNewResult.validated === false) {
				result.addNewErrors = true;
			} else if (addNewResult.dirty === false && addNewResult.validated === false) {
				result.addNewNoChanges = true;
			}
		}

		if (operation === "transfer" || operation === "all") {
			var addTransferResult = addTransferCauseFormValidation();
			if (addTransferResult.dirty === true && addTransferResult.validated === true) {
				result.addTransferPost = true;
				postFormToCauseServlet(AJS.$("#CausePageFormAddTransfer"));
			} else if (addTransferResult.dirty === true && addTransferResult.validated === false) {
				result.addTransferErrors = true;
			} else if (addTransferResult.dirty === false && addTransferResult.validated === false) {
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
			result.existingPost = true;
			for (var i = 0; i < existingResult.modifiedExistingCausesIDs.length; i++) {
				var formElement = AJS.$("input[name='causeID'][value='" + existingResult.modifiedExistingCausesIDs[i] +"']").closest("form");
				postFormToCauseServlet(formElement);
			}
		}
		if (existingResult.deleteExistingCausesIDs.length !== 0) {
			result.existingDelete = true;
			openDeleteCauseDialog(existingResult.deleteExistingCausesIDs, result);
		} else {
			// Display appropriate message and load the template again to see the changes
			displayAppropriateMessage(result, "Cause");
		}
	});

	// Transfer click event
	AJS.$("#causeHazardList").live("change reset", function() {
		var causeContainer = AJS.$("#CausePageCauseTransferContainer");
		AJS.$(causeContainer).children().remove();
		var hazardID = AJS.$(this).val();
		if (hazardID !== "") {
			var causes = getAllCausesWithinHazard(hazardID, false);
			var html = "<label class='popupLabels' for='causeList'>Hazard Causes</label><select class='select long-field' name='causeList' id='causeList'>";
			if (causes.length !== 0) {
				html += "<option value=''>-Link to all Causes in selected Hazard Report-</option>";
				for (var i = 0; i < causes.length; i++) {
					var optionText;
					if (causes[i].transfer != true) {
						optionText = causes[i].causeNumber + " - " + causes[i].text;
						html += "<option value=" + causes[i].causeID + ">" + manipulateTextLength(optionText, 85) + "</option>";
					}
					
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

	// Duplicate delete reason in delete dialog
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

	var hazardListElement = AJS.$(formElement).find("#causeHazardList").val();
	var transferReasonElement = AJS.$(formElement).find("#transferReason").val();

	if (hazardListElement !== undefined && transferReasonElement !== undefined) {
		if (hazardListElement !== "" || transferReasonElement !== "") {
			dirty = true;
		}
		if (dirty === true) {
			if (hazardListElement === "") {
				AJS.$(formElement).find("[data-error='causeHazardList']").show();
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
	AJS.$(formElement).ajaxSubmit({
		async: false,
		success: function(data) {
		},
		error: function(error) {
			// TODO:
			// Getting an object here which contains the error message
			// Display similar message as in success, but of the error kind
			console.log("ERROR");
		}
	});
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
	var html1 = "<span class='ConfirmDialogHeadingOne'>Hazard Title: <span class='ConfirmDialogHeadingOneContent'>" + AJS.$("#MissionHazardNavHazardTitle").text() + "</span></span>" +
				"<span class='ConfirmDialogHeadingOne'>Hazard #: <span class='ConfirmDialogHeadingOneContent'>" + AJS.$("#MissionHazardNavHazardNumber").text() + "</span></span>";
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
							"<th class='ConfirmDialogTableHeader ConfirmDialogTableCellThree'>Owner</th>" +
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
		
		var controls = getAllControlsWithinCause(causeIDsToDelete[i], true);
		if(controls.length > 0) {
			html3 += "<tr><td colspan='100%' class='ConfirmDialogTableNoBorder'><p class='ConfirmDialogErrorText'>Warning: This cause has "+controls.length+" control(s) that will be disassociated from this cause.</p></td></tr>";
		}
		
		var transferOrigins = getTransferOrigins(causeIDsToDelete[i], "cause");
		
		if(transferOrigins.causes.length > 0 || transferOrigins.controls.length > 0) {
			html3 += "<tr><td colspan='100%' class='ConfirmDialogTableNoBorder'><p class='ConfirmDialogErrorText'>Warning: This cause is the target of a transfer:</p></td></tr>";
		}
		
		for(var j = 0; j < transferOrigins.causes.length; j++) {
			html3 += "<tr>" +
				"<td colspan='100%' class='ConfirmDialogTableNoBorder'>" +
				"<p class='ConfirmDialogErrorText ConfirmDialogErrorTextHidden' id='ConfirmDialogTransferWarningForCauseID" + causeIDsToDelete[i] +"'>"+getTransferTargetDeleteWarning(transferOrigins.causes[j], "cause")+"</p>" +
				"</td>" +
				"</tr>";
		}
		
		for(var j = 0; j < transferOrigins.controls.length; j++) {
			html3 += "<tr>" +
				"<td colspan='100%' class='ConfirmDialogTableNoBorder'>" +
				"<p class='ConfirmDialogErrorText ConfirmDialogErrorTextHidden' id='ConfirmDialogTransferWarningForCauseID" + causeIDsToDelete[i] +"'>"+getTransferTargetDeleteWarning(transferOrigins.controls[j], "control")+"</p>" +
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
		id: "deleteDialog"
	});

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
		displayAppropriateMessage(result, "Cause");
	});

	dialog.addButton("Continue", function(dialog) {
		var validated = deleteCauseFormValidation(causeIDsToDelete);
		if (validated === true) {
			for (var i = 0; i < causeIDsToDelete.length; i++) {
				postDeleteToCauseServlet(causeIDsToDelete[i], AJS.$("#ReasonTextForCauseID" + causeIDsToDelete[i]).val());
			}
			dialog.hide();
			dialog.remove();
			displayAppropriateMessage(result, "Cause");
		}
	});

	dialog.show();
}

function postDeleteToCauseServlet(causeIDToDelete, reason) {
	AJS.$.ajax({
		type: "DELETE",
		async: false,
		url: "causes?id=" + causeIDToDelete + "&reason=" + reason,
		success: function(data) {
			modifyHTSCookieOpenCauses("close", causeIDToDelete, null);
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

