console.log("=== control-page.js ===");

var EXISTING_CONTROLS_SERIALIZED = null;

function initializeControlPage() {
	initControlPageClickEvents();
	
	EXISTING_CONTROLS_SERIALIZED = {};
	AJS.$(".ControlPageFormExisting").each(function () {
		var controlID = AJS.$(this).find("[name='controlID']").val();
		var serialized = AJS.$(this).serialize();
		EXISTING_CONTROLS_SERIALIZED[controlID] = serialized;
	});

	if (AJS.Cookie.read("HTS_COOKIE") !== undefined) {
		var htsCookieJson = JSON.parse(AJS.Cookie.read("HTS_COOKIE"));
		for (var i = 0; i < htsCookieJson.OPEN_CONTROLS.length; i++) {
			var controlID = htsCookieJson.OPEN_CONTROLS[i];
			
			var controlToggle = AJS.$("#ControlTableEntryID" + controlID + " > span.ControlTableToggle");
			if(controlToggle.length > 0) {
				var controlDisplay = AJS.$("#ControlTableEntryContentID" + controlID);
				openForm(controlToggle, controlDisplay);
			}
		}
		
		AJS.$(".ControlCauseTableToggle").each(function () {
			if(!AJS.$(this).parent().parent().find("[id^='ControlTableEntryContentID']:visible").length) {
				closeForm(AJS.$(this), AJS.$(this).parent().siblings('ul'));
			}
		});
	}
}

function initControlPageClickEvents() {
	
	initializeFormToggles();
	
	// Make sure cause is opened when user clicks on a cause link
	AJS.$("div.causeHeader > span.causeNumber > a").click(function() {
		var causeID = AJS.$(this).attr("causeID");
		modifyHTSCookieOpenCauses("open", causeID, null);
	});
	
	//when doing a control transfer, automatically expand the targetcause on the cause page
	AJS.$(".controlTransferLink").click(function(event) {
	    // Get the link that fired the click event
		var targetID = AJS.$(this).attr("targetID");
		var targetType = AJS.$(this).attr("targetType");
		
	    initHTSCookie();
	    if(targetType === "CAUSE"){
	    	modifyHTSCookieOpenCauses("open", targetID, null);
	    }
	    else{
	    	modifyHTSCookieOpenControls("open", targetID);
	    }
	    
	    // CAll the shared-cookes.js code that will set the user's cookie to expand Cause Number on HazardNumber 
	});
	
	// links to associated verifications to a cause
	AJS.$(".verificationLink").click(function(event) {
	    // Get the link that fired the click event
		var targetID = AJS.$(this).attr("targetID");

	    initHTSCookie();
	    modifyHTSCookieOpenVerifications("open", targetID, null);
	});
	
	// Clear new Control form
	AJS.$("#ControlPageClearNew").live("click", function() {
		var formElement = AJS.$("#ControlPageFormAddNew");
		AJS.$(formElement).find("#controlDescription").val("");
		AJS.$(formElement).find("#controlGroup").val("").trigger('chosen:updated');
	});

	// Clear new transfer Control form
	AJS.$("#ControlPageClearTransfer").live("click", function() {
		AJS.$("#ControlPageCauseTransferContainer").hide();
		AJS.$("#ControlPageCauseTransferContainer").children().remove();
		AJS.$("#ControlPageControlTransferContainer").hide();
		AJS.$("#ControlPageControlTransferContainer").children().remove();
		var formElement = AJS.$("#ControlPageFormAddTransfer");
		AJS.$(formElement).find("#transferReason").val("");
		AJS.$(formElement).find("#controlHazardList").val("").trigger('chosen:updated');
	});

	// Add new control click event
	AJS.$("#ControlPageAddNewControl").live("click", function() {
		if(!isOpen(AJS.$(this))) {
			openForm(AJS.$(this), AJS.$("#ControlPageNewContainer"));
		}
		else {
			closeForm(AJS.$(this), AJS.$("#ControlPageNewContainer"));
		}
	});

	// Add new transfer click event
	AJS.$("#ControlPageAddTransfer").live("click", function() {
		if(!isOpen(AJS.$(this))) {
			openForm(AJS.$(this), AJS.$("#ControlPageTransferContainer"));
		}
		else {
			closeForm(AJS.$(this), AJS.$("#ControlPageTransferContainer"));
		}
	});
	
	// Save new control
	AJS.$(".ControlPageSaveAllChanges").live("click", function() {
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
			var addNewResult = addNewControlFormValidation();
			if (addNewResult.dirty === true && addNewResult.validated === true) {
				result.addNewPost = true;
				postFormToControlServlet(AJS.$("#ControlPageFormAddNew"));
			} else if (addNewResult.dirty === true && addNewResult.validated === false) {
				result.addNewErrors = true;
			} else if (addNewResult.dirty === false && addNewResult.validated === false) {
				result.addNewNoChanges = true;
			}
		}

		if (operation === "transfer" || operation === "all") {
			var addTransferResult = addTransferControlFormValidation();
			if (addTransferResult.dirty === true && addTransferResult.validated === true) {
				result.addTransferPost = true;
				postFormToControlServlet(AJS.$("#ControlPageFormAddTransfer"));
			} else if (addTransferResult.dirty === true && addTransferResult.validated === false) {
				result.addTransferErrors = true;
			} else if (addTransferResult.dirty === false && addTransferResult.validated === false) {
				result.addTransferNoChanges = true;
			}
		}

		var existingResult = existingControlFormValidation();
		if (existingResult.validated === true) {
			result.existingErrors = false;
		} else {
			result.existingErrors = true;
		}
		if (existingResult.validated === true &&
			existingResult.modifiedExistingControlsIDs.length === 0 &&
			existingResult.deleteExistingControlsIDs.length === 0 &&
			existingResult.modifiedDeleteExistingControlsIDs.length === 0) {
			result.existingNoChanges = true;
		}
		if (existingResult.modifiedExistingControlsIDs.length !== 0) {
			result.existingPost = true;
			for (var i = 0; i < existingResult.modifiedExistingControlsIDs.length; i++) {
				var formElement = AJS.$("input[name='controlID'][value='" + existingResult.modifiedExistingControlsIDs[i] +"']").closest("form");
				postFormToControlServlet(formElement);
			}
		}
		if (existingResult.deleteExistingControlsIDs.length !== 0) {
			result.existingDelete = true;
			openDeleteControlDialog(existingResult.deleteExistingControlsIDs, result);
		} else {
			// Display appropriate message and load the template again to see the changes
			displayAppropriateMessage(result, "Control");
		}
	});

	// Transfer click event (gets causes belonging to selected hazard)
	AJS.$("#controlHazardList").live("change reset", function() {
		var causeContainer = AJS.$("#ControlPageCauseTransferContainer");
		AJS.$(causeContainer).children().remove();
		var controlContainer = AJS.$("#ControlPageControlTransferContainer");
		AJS.$(controlContainer).hide();
		AJS.$(controlContainer).children().remove();

		var hazardID = AJS.$(this).val();
		if (hazardID !== "") {
			var causes = getAllCausesWithinHazard(hazardID, false);
			var html = "<label class='popupLabels' for='controlCauseList'>Transfer to Cause</label><select class='select long-field' name='controlCauseList' id='controlCauseList'>";
			if (causes.length !== 0) {
				html += "<option value=''>-Select Cause-</option>";
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
				AJS.$(causeContainer).append("<span class='ConfirmDialogErrorText'>This Hazard has no non-transferred Causes. No transfer can be created.</span>");

			}
			AJS.$(causeContainer).show();
		} else {
			AJS.$(causeContainer).hide();
		}
	});

	// Transfer click event (gets controls belonging to selected cause)
	AJS.$("#controlCauseList").live("change reset", function() {
		var controlContainer = AJS.$("#ControlPageControlTransferContainer");
		AJS.$(controlContainer).children().remove();
		var causeID = AJS.$(this).val();
		if (causeID !== "") {
			var controls = getAllControlsWithinCause(causeID, false);
			var html = "<label class='popupLabels' for='controlControlList'>Transfer to Control</label><select class='select long-field' name='controlControlList' id='controlControlList'>";
			if (controls.length !== 0) {
				html += "<option value=''>-Link to all Controls in selected Cause-</option>";
				for (var i = 0; i < controls.length; i++) {
					var optionText;
					if (controls[i].transfer != true) {
						optionText = controls[i].controlNumber + " - " + controls[i].text;
						html += "<option value=" + controls[i].controlID + ">" + manipulateTextLength(optionText, 85) + "</option>";
					}
				}
				html += "</select>";
				AJS.$(controlContainer).append(html);
			} else {
				AJS.$(controlContainer).append("<label class='popupLabels' for='controlControlList'>Hazard Controls</label><div>-Link to all Controls in selected Cause- (Selected Cause currently has no non-transferred Controls)</div>");
			}
			AJS.$(controlContainer).show();
		} else {
			AJS.$(controlContainer).hide();
		}
	});

	// Duplicate delete reason in delete dialog
	AJS.$("#ConfirmDialogDuplBtnControls").live("click", function() {
		var reasonTextFields = AJS.$(".ConfirmDialogReasonTextInput");
		var reasonToDuplicate;
		reasonTextFields.each(function (index) {
			if (index === 0) {
				reasonToDuplicate = AJS.$(this).val();
				if (reasonToDuplicate === "") {
					var controlID = AJS.$(this).attr("id").replace( /^\D+/g, '');
					var errorElement = AJS.$("#ConfirmDialogErrorTextForControlID" + controlID);
					AJS.$(errorElement).text("For the Control above, please provide a short delete reason.");
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

function initializeFormToggles() {
	// Open/close on a cause header
	AJS.$(".ControlCauseTableToggle").click( function() {
		var displayElement = AJS.$(this).parent().siblings('ul');
		if(!isOpen(AJS.$(this))) {			
			openForm(AJS.$(this), displayElement);
		}
		else {
			AJS.$(this).parent().parent().find(".ControlTableToggle").each(function () {
				var controlID = AJS.$(this).parent().attr("id").split("ControlTableEntryID")[1];
				var controlDisplayElement = AJS.$("#ControlTableEntryContentID" + controlID);
				console.log("closing " + controlID);
				closeForm(AJS.$(this), controlDisplayElement);
				modifyHTSCookieOpenControls("close", controlID);
			});
			
			closeForm(AJS.$(this), displayElement);
		}
	});
	
	// Open/close on existing control
	AJS.$(".ControlTableToggle").click( function() {
		var controlID = AJS.$(this).parent().attr("id").split("ControlTableEntryID")[1];
		var displayElement = AJS.$("#ControlTableEntryContentID" + controlID);
		if(!isOpen(AJS.$(this))) {
			openForm(AJS.$(this),displayElement);
			modifyHTSCookieOpenControls("open", controlID);
		}
		else {			
			closeForm(AJS.$(this),displayElement);
			modifyHTSCookieOpenControls("close", controlID);
		}
	});

	// Expand All button click
	AJS.$("#ControlPageExpandAllButton").click( function() {
		AJS.$(".ControlCauseTableToggle").each(function() {
			if(!isOpen(AJS.$(this))) {
				openForm(AJS.$(this), AJS.$(this).parent().siblings('ul'));
			}
		});	
		
		AJS.$(".ControlTableToggle").each(function() {
			var controlID = AJS.$(this).parent().attr("id").split("ControlTableEntryID")[1];
			if(!isOpen(AJS.$(this))) {
				openForm(AJS.$(this), AJS.$("#ControlTableEntryContentID" + controlID));
				modifyHTSCookieOpenControls("open", controlID);
			}
		});
	});
	
	AJS.$("#ControlPageCloseAllButton").click( function() {
		AJS.$(".ControlTableToggle").each(function (index) {
			var controlID = AJS.$(this).parent().attr("id").split("ControlTableEntryID")[1];
			var controlDisplayElement = AJS.$("#ControlTableEntryContentID" + controlID);
			closeForm(AJS.$(this), controlDisplayElement);
			modifyHTSCookieOpenControls("close", controlID);
		});
		
		AJS.$(".ControlCauseTableToggle").each(function() {
			if(isOpen(AJS.$(this))) {
				closeForm(AJS.$(this), AJS.$(this).parent().siblings('ul'));
			}
		});	
	});
	
}

function existingControlFormValidation() {
	var modifiedExistingControlsIDs = [];
	var deleteExistingControlsIDs = [];
	var modifiedDeleteExistingControlsIDs = [];
	var validated = true;

	AJS.$(".ControlPageFormExisting").each(function () {
		var controlID = AJS.$(this).find("[name='controlID']").val();
		var deleteSelected = AJS.$(".ControlPageDeleteBox[value='" + controlID + "']").is(':checked');
		var oldSerialized = EXISTING_CONTROLS_SERIALIZED[controlID];
		var newSerialized = AJS.$(this).serialize();
		if (newSerialized !== oldSerialized) {
			if (newSerialized.indexOf("controlDescription=&") > -1) {
				if (deleteSelected === false) {
					AJS.$(this).find("[data-error='controlDescription']").show();
					validated = false;
				}
			} else {
				if (deleteSelected === false) {
					modifiedExistingControlsIDs.push(controlID);
				} else {
					modifiedDeleteExistingControlsIDs.push(controlID);
				}
			}
		}
		if (deleteSelected === true) {
			deleteExistingControlsIDs.push(controlID);
		}
	});

	return {"validated" : validated,
		"modifiedExistingControlsIDs" : modifiedExistingControlsIDs,
		"deleteExistingControlsIDs" : deleteExistingControlsIDs,
		"modifiedDeleteExistingControlsIDs" : modifiedDeleteExistingControlsIDs};
}

function addNewControlFormValidation() {
	var formElement = AJS.$("#ControlPageFormAddNew");
	var dirty = false;
	var validated = false;
	if (AJS.$(formElement).find("#controlDescription").val() !== "" ||
		AJS.$(formElement).find("#controlGroup").val() !== "" ||
		AJS.$(formElement).find("#controlCausesms2side__dx").children().length !== 0) {
		dirty = true;
	}
	if (dirty === true) {
		if (AJS.$(formElement).find("#controlDescription").val() === "") {
			AJS.$(formElement).find("[data-error='controlDescription']").show();
		} else {
			validated = true;
		}
	}
	return {"dirty" : dirty, "validated" : validated};
}

function addTransferControlFormValidation() {
	var formElement = AJS.$("#ControlPageFormAddTransfer");
	var dirty = false;
	var validated = false;

	var hazardListElement = AJS.$(formElement).find("#controlHazardList").val();
	var causeListElement = AJS.$(formElement).find("#controlCauseList").val();
	var transferReasonElement = AJS.$(formElement).find("#transferReason").val();

	if (hazardListElement !== undefined && causeListElement !== undefined && transferReasonElement !== undefined) {
		if (hazardListElement !== "" || causeListElement !== "" || transferReasonElement !== "") {
			dirty = true;
		}
		if (dirty === true) {
			if (causeListElement === "") {
				AJS.$(formElement).find("[data-error='controlCauseList']").show();
			} else {
				validated = true;
			}
		}
		return {"dirty" : dirty, "validated" : validated};
	} else {
		return {"dirty" : false, "validated" : false};
	}
}

function postFormToControlServlet(formElement) {
	AJS.$(formElement).ajaxSubmit({		
		async: false,
		success: function(data) {
			console.log("SUCCESS");
			if(data.newControlID) {
				modifyHTSCookieOpenControls("open", data.newControlID);
			}
		},
		error: function(error) {
			// TODO:
			// Getting an object here which contains the error message
			// Display similar message as in success, but of the error kind
			console.log("ERROR");
		}
	});
}

// The following functions have to do with deleting Controls
function openDeleteControlDialog(controlIDsToDelete, result) {
	var html1 = "<span class='ConfirmDialogHeadingOne'>Hazard Title: <span class='ConfirmDialogHeadingOneContent'>" + AJS.$("#MissionHazardNavHazardTitle").text() + "</span></span>" +
				"<span class='ConfirmDialogHeadingOne'>Hazard #: <span class='ConfirmDialogHeadingOneContent'>" + AJS.$("#MissionHazardNavHazardNumber").text() + "</span></span>";
	var html2;
	if (controlIDsToDelete.length === 1) {
		html2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>" +
					"The following Control will be deleted from the above Hazard Report. In order to complete the deletion, you will need to provide a short delete reason." +
				"</span></div>";
	} else {
		html2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>" +
					"The following Controls will be deleted from the above Hazard Report. In order to complete the deletion, you will need to provide a short delete reason for each of the Controls." +
				"</span></div>";
	}
	var html3 = "<table>" +
					"<thead>" +
						"<tr>" +
							"<th class='ConfirmDialogTableHeader ConfirmDialogTableCellOne'>#</th>" +
							"<th class='ConfirmDialogTableHeader ConfirmDialogTableCellTwo'>Description</th>" +
							"<th class='ConfirmDialogTableHeader ConfirmDialogTableCellThree'>Control group</th>" +
						"</tr>" +
					"</thead>" +
					"<tbody>";
	for (var i = 0; i < controlIDsToDelete.length; i++) {
		var controlFirstRow = AJS.$("#ControlTableEntryID" + controlIDsToDelete[i]);
		html3 += "<tr><td colspan='100%' class='ConfirmDialogTableNoBorder'><div class='ConformDialogEmptyRow'></div></td></tr>";
		html3 += "<tr><td class='ConfirmDialogTableNoBorder'>" + controlFirstRow.children(":nth-child(2)").text().replace("Control ", "") + "</td>";
		html3 += "<td class='ConfirmDialogTableNoBorder'><div class='ConfirmDialogDescriptionText'>" + controlFirstRow.children(":nth-child(3)").text() + "</div></td>";
		html3 += "<td class='ConfirmDialogTableNoBorder'>" + controlFirstRow.children(":nth-child(4)").text() + "</td></tr>";

		if (i === 0 && controlIDsToDelete.length > 1) {
			html3 += "<tr>" +
						"<td colspan='100%' class='ConfirmDialogTableNoBorder'>" +
							"<div class='ConfirmDialogLabelContainer'>" +
								"<label for='ReasonTextForControl'><span class='HTSRequired'>* </span>Reason</label>" +
							"</div>" +
							"<div class='ConfirmDialogReasonTextContainer'>" +
								"<input type='text' class='ConfirmDialogReasonTextInput' name='ReasonTextForControl' id='ReasonTextForControlID" + controlIDsToDelete[i] + "'>" +
							"</div>" +
							"<div class='ConfirmDialogDuplButtonContainer'>" +
								"<button class='aui-button ConfirmDialogDuplButton' id='ConfirmDialogDuplBtnControls'>Apply to all</button>" +
							"</div>" +
						"</td>" +
					"</tr>";
		} else {
			html3 += "<tr>" +
						"<td colspan='100%' class='ConfirmDialogTableNoBorder'>" +
							"<div class='ConfirmDialogLabelContainer'>" +
								"<label for='ReasonTextForControl'><span class='HTSRequired'>* </span>Reason</label>" +
							"</div>" +
							"<div class='ConfirmDialogReasonTextContainerNoButton'>" +
								"<input type='text' class='ConfirmDialogReasonTextInput' name='ReasonTextForControl' id='ReasonTextForControlID" + controlIDsToDelete[i] + "'>" +
							"</div>" +
						"</td>" +
					"</tr>";
		}
		var transferOrigins = getTransferOrigins(controlIDsToDelete[i], "control");
		
		if(transferOrigins.controls.length > 0) {
			html3 += "<tr><td colspan='100%' class='ConfirmDialogTableNoBorder'><p class='ConfirmDialogErrorText'>Warning: This control is the target of a transfer:</p></td></tr>";
		}
		
		for(var j = 0; j < transferOrigins.controls.length; j++) {
			html3 += "<tr>" +
				"<td colspan='100%' class='ConfirmDialogTableNoBorder'>" +
				"<p class='ConfirmDialogErrorText ConfirmDialogErrorTextHidden' id='ConfirmDialogTransferWarningForControlID" + controlIDsToDelete[i] +"'>"+getTransferTargetDeleteWarning(transferOrigins.controls[j], "control")+"</p>" +
				"</td>" +
				"</tr>";
		}		
		


		html3 += "<tr>" +
				"<td colspan='100%' class='ConfirmDialogTableNoBorder'>" +
				"<p class='ConfirmDialogErrorText ConfirmDialogErrorTextHidden' id='ConfirmDialogErrorTextForControlID" + controlIDsToDelete[i] +"'></p>" +
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
		deselectAllControls();
		result.existingDelete = false;
		displayAppropriateMessage(result, "Control");
	});

	dialog.addButton("Continue", function(dialog) {
		var validated = deleteControlFormValidation(controlIDsToDelete);
		if (validated === true) {
			for (var i = 0; i < controlIDsToDelete.length; i++) {
				postDeleteToControlServlet(controlIDsToDelete[i], AJS.$("#ReasonTextForControlID" + controlIDsToDelete[i]).val());
			}
			dialog.hide();
			dialog.remove();
			displayAppropriateMessage(result, "Control");
		}
	});

	dialog.show();
}

function postDeleteToControlServlet(controlIDToDelete, reason) {
	AJS.$.ajax({
		type: "DELETE",
		async: false,
		url: "controls?id=" + controlIDToDelete + "&reason=" + reason,
		success: function(data) {
			console.log("SUCCESS");
			modifyHTSCookieOpenControls("close", controlIDToDelete);
		},
		error: function(data) {
			console.log("ERROR");
		}
	});
}

function deleteControlFormValidation(controlIDsToDelete) {
	var validated = true;
	for (var i = 0; i < controlIDsToDelete.length; i++) {
		var deleteReason = AJS.$("#ReasonTextForControlID" + controlIDsToDelete[i]).val();
		var errorElement = AJS.$("#ConfirmDialogErrorTextForControlID" + controlIDsToDelete[i]);
		if (deleteReason === "") {
			AJS.$(errorElement).text("For the Control above, please provide a short delete reason.");
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

function deselectAllControls() {
	var checkboxes = AJS.$(".ControlPageDeleteBox:checked");
	checkboxes.each(function () {
		AJS.$(this).attr("checked", false);
	});
}