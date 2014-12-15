console.log("=== control-page.js ===");

var EXISTING_CONTROLS_SERIALIZED = null;

function initializeControlPage() {
	if (INIT.CONTROLS === true) {
		INIT.CONTROLS = false;
		initControlPageClickEvents();
	}
	initControlPageMultiSelectes();
	initControlPageDateModification();
	EXISTING_CONTROLS_SERIALIZED = serializeExistingControls();

	// Calling functions in shared-cookies.js file
	var existingControlsCount = AJS.$(".ControlTableToggle").length;
	openHTSCookieOpenControls(existingControlsCount);
	renameControlPageExpandButton(existingControlsCount);
}

function initControlPageClickEvents() {
	// Clear new Control form
	AJS.$("#ControlPageClearNew").live("click", function() {
		var formElement = AJS.$("#ControlPageFormAddNew");
		AJS.$(formElement).find("#controlDescription").val("");
		AJS.$(formElement).find("#controlGroup").val("").trigger('chosen:updated');
		var multiSelectElement = AJS.$("#ControlPageFormAddNewMultiSelect");
		AJS.$(multiSelectElement).find(".RemoveAll").trigger("click");
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
		// Calling function in shared-cookies.js file
		toggleOpenCloseIcon(AJS.$(this), AJS.$("#ControlPageNewContainer"));
	});

	// Add new transfer click event
	AJS.$("#ControlPageAddTransfer").live("click", function() {
		// Calling function in shared-cookies.js file
		toggleOpenCloseIcon(AJS.$(this), AJS.$("#ControlPageTransferContainer"));
	});

	// Open/close on existing cause
	AJS.$(".ControlTableToggle").live("click", function() {
		var elementID = AJS.$(this).parent().parent().attr("id");
		var controlID = elementID.split("ControlTableEntryID")[1];
		var displayElement = AJS.$("#ControlTableEntryContentID" + controlID);
		var operation = toggleOpenCloseIcon(AJS.$(this), displayElement);
		var existingControlCount = AJS.$(".ControlTableToggle").length;
		// Calling function in shared-cookies.js file
		modifyHTSCookieOpenControls(operation, controlID, existingControlCount);
	});

	// Expand All/Close All causes
	AJS.$("#ControlPageExpandAllButton").live("click", function() {
		var operation = AJS.$(this).val();
		var buttonElements = AJS.$(".ControlTableToggle");
		var existingControlsCount = AJS.$(".ControlTableToggle").length;

		if (operation === "Expand All") {
			buttonElements.each(function () {
				if (AJS.$(this).hasClass("aui-iconfont-add")) {
					AJS.$(this).removeClass("aui-iconfont-add");
					AJS.$(this).addClass("aui-iconfont-devtools-task-disabled");
					var elementID = AJS.$(this).parent().parent().attr("id");
					var controlID = elementID.split("ControlTableEntryID")[1];
					AJS.$("#ControlTableEntryContentID" + controlID).show();
					// Calling function in shared-cookies.js file
					modifyHTSCookieOpenControls("open", controlID, null);
				}
			});
		} else {
			buttonElements.each(function () {
				if (AJS.$(this).hasClass("aui-iconfont-devtools-task-disabled")) {
					AJS.$(this).removeClass("aui-iconfont-devtools-task-disabled");
					AJS.$(this).addClass("aui-iconfont-add");
					var elementID = AJS.$(this).parent().parent().attr("id");
					var controlID = elementID.split("ControlTableEntryID")[1];
					AJS.$("#ControlTableEntryContentID" + controlID).hide();
					// Calling function in shared-cookies.js file
					modifyHTSCookieOpenControls("close", controlID, null);
				}
			});
		}
		// Calling function in shared-cookies.js file
		renameControlPageExpandButton(existingControlsCount);
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
			var causes = getAllCausesWithinHazard(hazardID);
			var html = "<label class='popupLabels' for='controlCauseList'>Hazard Causes</label><select class='select long-field' name='controlCauseList' id='controlCauseList'>";
			if (causes.length !== 0) {
				html += "<option value=''>-Select Cause-</option>";
				for (var i = 0; i < causes.length; i++) {
					var optionText;
					if (causes[i].transfer === true) {
						optionText = causes[i].causeNumber + "-T - " + causes[i].text;
					} else {
						optionText = causes[i].causeNumber + " - " + causes[i].text;
					}
					html += "<option value=" + causes[i].causeID + ">" + manipulateTextLength(optionText, 85) + "</option>";
				}
				html += "</select>";
				AJS.$(causeContainer).append(html);
			} else {
				AJS.$(causeContainer).append("<span>This Hazard Report has no Causes. No Control Transfer can be created.</span>");

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
			var controls = getAllControlsWithinCause(causeID);
			var html = "<label class='popupLabels' for='controlControlList'>Hazard Controls</label><select class='select long-field' name='controlControlList' id='controlControlList'>";
			if (controls.length !== 0) {
				html += "<option value=''>-Link to all Controls in selected Hazard Report-</option>";
				for (var i = 0; i < controls.length; i++) {
					var optionText;
					if (controls[i].transfer === true) {
						optionText = controls[i].controlNumber + "-T - " + controls[i].text;
					} else {
						optionText = controls[i].controlNumber + " - " + controls[i].text;
					}
					html += "<option value=" + controls[i].controlID + ">" + manipulateTextLength(optionText, 85) + "</option>";
				}
				html += "</select>";
				AJS.$(controlContainer).append(html);
			} else {
				AJS.$(controlContainer).append("<label class='popupLabels' for='controlControlList'>Hazard Controls</label><div>-Link to all Controls in selected Cause- (Selected Cause currently has no Controls)</div>");
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

function initControlPageDateModification() {
	AJS.$(".HTSDate").each(function() {
		var dateStrUnformatted = AJS.$(this).text();
		var dateStrFormatted = formatDate(dateStrUnformatted);
		AJS.$(this).text(dateStrFormatted);
	});
}

function initControlPageMultiSelectes() {
	AJS.$(".controlCauses").multiselect2side({
		selectedPosition: 'right',
		moveOptions: false,
		labelsx: '',
		labeldx: '',
		'search': 'Search: ',
		autoSort: true,
		autoSortAvailable: true
	});

	// Adjust the CSS
	var multiSelectDivs = AJS.$(".ms2side__div");
	AJS.$(multiSelectDivs).each(function() {
		AJS.$(this).children(":nth-child(1)").children(":nth-child(1)").css("padding-bottom", "3px");
		AJS.$(this).children(":nth-child(2)").css("padding-top", "12px");
		AJS.$(this).children(":nth-child(3)").css("padding-top", "28px");
	});
}

function serializeExistingControls() {
	if (AJS.$("#ControlPageTable").is(":visible")) {
		var serializedObj = {};
		AJS.$(".ControlPageFormExisting").each(function () {
			var controlID = AJS.$(this).find("[name='controlID']").val();
			var serialized = AJS.$(this).serialize();
			serializedObj[controlID] = serialized;
		});
		return serializedObj;
	} else {
		return null;
	}
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
		id: "deleteDialog",
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
				postDeleteToControlServlet(controlIDsToDelete[i]);
			}
			dialog.hide();
			dialog.remove();
			displayAppropriateMessage(result, "Control");
		}
	});

	dialog.show();
}

function postDeleteToControlServlet(controlIDToDelete) {
	AJS.$.ajax({
		type: "DELETE",
		async: false,
		url: "controls?id=" + controlIDToDelete + "&reason=" + "FOOBAR",
		success: function(data) {
			console.log("SUCCESS");
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