console.log("=== verification-page.js ===");

var EXISTING_VERIFICATIONS_SERIALIZED = null;

function initializeVerificationPage() {
	if (INIT.VERIFICATIONS === true) {
		INIT.VERIFICATIONS = false;
		initVerificationPageClickEvents();
	}
	initVerificationPageMultiSelectes();
	initVerificationPageDateModification();
	EXISTING_VERIFICATIONS_SERIALIZED = serializeExistingVerifications();

	var existingVerificationsCount = AJS.$(".VerificationTableToggle").length;
	openHTSCookieOpenVerifications(existingVerificationsCount);
	renameVerificationPageExpandButton(existingVerificationsCount);
}

function initVerificationPageClickEvents() {
	// Clear new Verification form
	AJS.$("#VerificationPageClearNew").live("click", function() {
		var formElement = AJS.$("#VerificationPageFormAddNew");
		AJS.$(formElement).find("#verificationDescription").val("");
		AJS.$(formElement).find("#verificationStatus").val("").trigger('chosen:updated');
		AJS.$(formElement).find("#verificationType").val("").trigger('chosen:updated');
		AJS.$(formElement).find("#verificationRespParty").val("");
		AJS.$(formElement).find("#verificationEstComplDate").val("");

		var multiSelectElement = AJS.$(formElement).find("#VerificationPageFormAddNewMultiSelect");
		AJS.$(multiSelectElement).find(".RemoveAll").trigger("click");
	});

	// Add new Verification click event
	AJS.$("#VerificationPageAddNewVerification").live("click", function() {
		// Calling function in shared-cookies.js file
		toggleOpenCloseIcon(AJS.$(this), AJS.$("#VerificationPageNewContainer"));
	});

	// Open/close on existing Verification
	AJS.$(".VerificationTableToggle").live("click", function() {
		var elementID = AJS.$(this).parent().parent().attr("id");
		var verificationID = elementID.split("VerificationTableEntryID")[1];
		var displayElement = AJS.$("#VerificationTableEntryContentID" + verificationID);
		var operation = toggleOpenCloseIcon(AJS.$(this), displayElement);
		var existingVerificationCount = AJS.$(".VerificationTableToggle").length;
		// Calling function in shared-cookies.js file
		modifyHTSCookieOpenVerifications(operation, verificationID, existingVerificationCount);
	});

	// Expand All/Close All verifications
	AJS.$("#VerificationPageExpandAllButton").live("click", function() {
		var operation = AJS.$(this).val();
		var buttonElements = AJS.$(".VerificationTableToggle");
		var existingVerificationsCount = AJS.$(".VerificationTableToggle").length;

		if (operation === "Expand All") {
			buttonElements.each(function () {
				if (AJS.$(this).hasClass("aui-iconfont-add")) {
					AJS.$(this).removeClass("aui-iconfont-add");
					AJS.$(this).addClass("aui-iconfont-devtools-task-disabled");
					var elementID = AJS.$(this).parent().parent().attr("id");
					var verificationID = elementID.split("VerificationTableEntryID")[1];
					AJS.$("#VerificationTableEntryContentID" + verificationID).show();
					// Calling function in shared-cookies.js file
					modifyHTSCookieOpenVerifications("open", verificationID, null);
				}
			});
		} else {
			buttonElements.each(function () {
				if (AJS.$(this).hasClass("aui-iconfont-devtools-task-disabled")) {
					AJS.$(this).removeClass("aui-iconfont-devtools-task-disabled");
					AJS.$(this).addClass("aui-iconfont-add");
					var elementID = AJS.$(this).parent().parent().attr("id");
					var verificationID = elementID.split("VerificationTableEntryID")[1];
					AJS.$("#VerificationTableEntryContentID" + verificationID).hide();
					// Calling function in shared-cookies.js file
					modifyHTSCookieOpenVerifications("close", verificationID, null);
				}
			});
		}
		// Calling function in shared-cookies.js file
		renameVerificationPageExpandButton(existingVerificationsCount);
	});

	// Save new verification
	AJS.$(".VerificationPageSaveAllChanges").live("click", function() {
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
			var addNewResult = addNewVerificationFormValidation();
			if (addNewResult.dirty === true && addNewResult.validated === true) {
				result.addNewPost = true;
				postFormToVerificationServlet(AJS.$("#VerificationPageFormAddNew"));
			} else if (addNewResult.dirty === true && addNewResult.validated === false) {
				result.addNewErrors = true;
			} else if (addNewResult.dirty === false && addNewResult.validated === false) {
				result.addNewNoChanges = true;
			}
		}

		var existingResult = existingVerificationFormValidation();
		if (existingResult.validated === true) {
			result.existingErrors = false;
		} else {
			result.existingErrors = true;
		}
		if (existingResult.validated === true &&
			existingResult.modifiedExistingVerificationsIDs.length === 0 &&
			existingResult.deleteExistingVerificationsIDs.length === 0 &&
			existingResult.modifiedDeleteExistingVerificationsIDs.length === 0) {
			result.existingNoChanges = true;
		}
		if (existingResult.modifiedExistingVerificationsIDs.length !== 0) {
			result.existingPost = true;
			for (var i = 0; i < existingResult.modifiedExistingVerificationsIDs.length; i++) {
				var formElement = AJS.$("input[name='verificationID'][value='" + existingResult.modifiedExistingVerificationsIDs[i] +"']").closest("form");
				postFormToVerificationServlet(formElement);
			}
		}
		if (existingResult.deleteExistingVerificationsIDs.length !== 0) {
			result.existingDelete = true;
			/* TODO:
				Create the "openDeleteVerificationDialog" function.
				This function will be 95% the same as the Cause/Controls version. So its a good starting point to just c/p it and then modify columm headers, data and such.
			*/
			openDeleteVerificationDialog(existingResult.deleteExistingVerificationsIDs, result);
		} else {
			// Display appropriate message and load the template again to see the changes
			displayAppropriateMessage(result, "Verification");
		}

	});
	
		// Duplicate delete reason in delete dialog
	AJS.$("#ConfirmDialogDuplBtnVerifications").live("click", function() {
		var reasonTextFields = AJS.$(".ConfirmDialogReasonTextInput");
		var reasonToDuplicate;
		reasonTextFields.each(function (index) {
			if (index === 0) {
				reasonToDuplicate = AJS.$(this).val();
				if (reasonToDuplicate === "") {
					var verificationID = AJS.$(this).attr("id").replace( /^\D+/g, '');
					var errorElement = AJS.$("#ConfirmDialogErrorTextForVerifificationID" + verificationID);
					AJS.$(errorElement).text("For the Verification above, please provide a short delete reason.");
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

function initVerificationPageDateModification() {
	var estimatedCompletionDates = AJS.$(".VerificationDate");
	estimatedCompletionDates.each(function () {
		var defaultDateArr = (AJS.$(this).data("date")).split(" ");
		var defaultDateStr = defaultDateArr[0];
		AJS.$(this).val(defaultDateStr);
	});
}

function initVerificationPageMultiSelectes() {
	AJS.$(".verificationControls").multiselect2side({
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

function serializeExistingVerifications() {
	if (AJS.$("#VerificationPageTable").is(":visible")) {
		var serializedObj = {};
		AJS.$(".VerificationPageFormExisting").each(function () {
			var verificationID = AJS.$(this).find("[name='verificationID']").val();
			var serialized = AJS.$(this).serialize();
			serializedObj[verificationID] = serialized;
		});
		return serializedObj;
	} else {
		return null;
	}
}

function existingVerificationFormValidation() {
	var modifiedExistingVerificationsIDs = [];
	var deleteExistingVerificationsIDs = [];
	var modifiedDeleteExistingVerificationsIDs = [];
	var validated = true;

	AJS.$(".VerificationPageFormExisting").each(function () {
		var verificationID = AJS.$(this).find("[name='verificationID']").val();
		var deleteSelected = AJS.$(".VerificationPageDeleteBox[value='" + verificationID + "']").is(':checked');
		var oldSerialized = EXISTING_VERIFICATIONS_SERIALIZED[verificationID];
		var newSerialized = AJS.$(this).serialize();
		if (newSerialized !== oldSerialized) {
			if (newSerialized.indexOf("verificationDescription=&") > -1) {
				if (deleteSelected === false) {
					AJS.$(this).find("[data-error='verificationDescription']").show();
					validated = false;
				}
			} else {
				if (deleteSelected === false) {
					modifiedExistingVerificationsIDs.push(verificationID);
				} else {
					modifiedDeleteExistingVerificationsIDs.push(verificationID);
				}
			}
		}
		if (deleteSelected === true) {
			deleteExistingVerificationsIDs.push(verificationID);
		}
	});

	return {"validated" : validated,
			"modifiedExistingVerificationsIDs" : modifiedExistingVerificationsIDs,
			"deleteExistingVerificationsIDs" : deleteExistingVerificationsIDs,
			"modifiedDeleteExistingVerificationsIDs" : modifiedDeleteExistingVerificationsIDs};
}

function addNewVerificationFormValidation() {
	var formElement = AJS.$("#VerificationPageFormAddNew");
	var dirty = false;
	var validated = false;
	if (AJS.$(formElement).find("#verificationDescription").val() !== "" ||
		AJS.$(formElement).find("#verificationStatus").val() !== "" ||
		AJS.$(formElement).find("#verificationType").val() !== "" ||
		AJS.$(formElement).find("#verificationRespParty").val() !== "" ||
		AJS.$(formElement).find("#verificationEstComplDate").val() !== "" ||
		AJS.$(formElement).find("#verificationControlsms2side__dx").children().length !== 0) {
		dirty = true;
	}
	if (dirty === true) {
		if (AJS.$(formElement).find("#verificationDescription").val() === "") {
			AJS.$(formElement).find("[data-error='verificationDescription']").show();
		} else {
			validated = true;
		}
	}
	return {"dirty" : dirty, "validated" : validated};
}

function postFormToVerificationServlet(formElement) {
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

// The following functions have to do with deleting Verifications
function openDeleteVerificationDialog(verificationIDsToDelete, result) {
	var html1 = "<span class='ConfirmDialogHeadingOne'>Hazard Title: <span class='ConfirmDialogHeadingOneContent'>" + AJS.$("#MissionHazardNavHazardTitle").text() + "</span></span>" +
				"<span class='ConfirmDialogHeadingOne'>Hazard #: <span class='ConfirmDialogHeadingOneContent'>" + AJS.$("#MissionHazardNavHazardNumber").text() + "</span></span>";
	var html2;
	if (verificationIDsToDelete.length === 1) {
		html2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>" +
					"The following Verification will be deleted from the above Hazard Report. In order to complete the deletion, you will need to provide a short delete reason." +
				"</span></div>";
	} else {
		html2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>" +
					"The following Verifications will be deleted from the above Hazard Report. In order to complete the deletion, you will need to provide a short delete reason for each of the Verifications." +
				"</span></div>";
	}
	var html3 = "<table>" +
					"<thead>" +
						"<tr>" +
							"<th class='ConfirmDialogTableHeader ConfirmDialogTableCellOne'>#</th>" +
							"<th class='ConfirmDialogTableHeader ConfirmDialogTableCellTwo'>Description</th>" +
							"<th class='ConfirmDialogTableHeader ConfirmDialogTableCellThree'>Status</th>" +
						"</tr>" +
					"</thead>" +
					"<tbody>";
	for (var i = 0; i < verificationIDsToDelete.length; i++) {
		var verificationFirstRow = AJS.$("#VerificationTableEntryID" + verificationIDsToDelete[i]);
		html3 += "<tr><td colspan='100%' class='ConfirmDialogTableNoBorder'><div class='ConformDialogEmptyRow'></div></td></tr>";
		html3 += "<tr><td class='ConfirmDialogTableNoBorder'>" + verificationFirstRow.children(":nth-child(2)").text().replace("Verification ", "") + "</td>";
		html3 += "<td class='ConfirmDialogTableNoBorder'><div class='ConfirmDialogDescriptionText'>" + verificationFirstRow.children(":nth-child(3)").text() + "</div></td>";
		html3 += "<td class='ConfirmDialogTableNoBorder'>" + verificationFirstRow.children(":nth-child(4)").text() + "</td>";
		html3 += "</tr>";

		if (i === 0 && verificationIDsToDelete.length > 1) {
			html3 += "<tr>" +
						"<td colspan='100%' class='ConfirmDialogTableNoBorder'>" +
							"<div class='ConfirmDialogLabelContainer'>" +
								"<label for='ReasonTextForControl'><span class='HTSRequired'>* </span>Reason</label>" +
							"</div>" +
							"<div class='ConfirmDialogReasonTextContainer'>" +
								"<input type='text' class='ConfirmDialogReasonTextInput' name='ReasonTextForVerification' id='ReasonTextForVerificationID" + verificationIDsToDelete[i] + "'>" +
							"</div>" +
							"<div class='ConfirmDialogDuplButtonContainer'>" +
								"<button class='aui-button ConfirmDialogDuplButton' id='ConfirmDialogDuplBtnVerifications'>Apply to all</button>" +
							"</div>" +
						"</td>" +
					"</tr>";
		} else {
			html3 += "<tr>" +
						"<td colspan='100%' class='ConfirmDialogTableNoBorder'>" +
							"<div class='ConfirmDialogLabelContainer'>" +
								"<label for='ReasonTextForVerification'><span class='HTSRequired'>* </span>Reason</label>" +
							"</div>" +
							"<div class='ConfirmDialogReasonTextContainerNoButton'>" +
								"<input type='text' class='ConfirmDialogReasonTextInput' name='ReasonTextForVerification' id='ReasonTextForVerificationID" + verificationIDsToDelete[i] + "'>" +
							"</div>" +
						"</td>" +
					"</tr>";
		}
		html3 += "<tr>" +
					"<td colspan='100%' class='ConfirmDialogTableNoBorder'>" +
						"<p class='ConfirmDialogErrorText ConfirmDialogErrorTextHidden' id='ConfirmDialogErrorTextForVerificationID" + verificationIDsToDelete[i] +"'></p>" +
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
		deselectAllVerifications();
		result.existingDelete = false;
		displayAppropriateMessage(result, "Verification");
	});

	dialog.addButton("Continue", function(dialog) {
		var validated = deleteVerificationFormValidation(verificationIDsToDelete);
		if (validated === true) {
			for (var i = 0; i < verificationIDsToDelete.length; i++) {
				postDeleteToVerificationServlet(verificationIDsToDelete[i], AJS.$("#ReasonTextForVerificationID" + verificationIDsToDelete[i]).val());
			}
			dialog.hide();
			dialog.remove();
			displayAppropriateMessage(result, "Verification");
		}
	});

	dialog.show();
}

function postDeleteToVerificationServlet(verificationIDToDelete, reason) {
	AJS.$.ajax({
		type: "DELETE",
		async: false,
		url: "verifications?id=" + verificationIDToDelete + "&reason=" + reason,
		success: function(data) {
			console.log("SUCCESS");
			modifyHTSCookieOpenVerifications("close", verificationIDToDelete, null);
		},
		error: function(data) {
			console.log("ERROR");
		}
	});
}

function deleteVerificationFormValidation(verificationIDsToDelete) {
	var validated = true;
	for (var i = 0; i < verificationIDsToDelete.length; i++) {
		var deleteReason = AJS.$("#ReasonTextForVerificationID" + verificationIDsToDelete[i]).val();
		var errorElement = AJS.$("#ConfirmDialogErrorTextForVerificationID" + verificationIDsToDelete[i]);
		if (deleteReason === "") {
			AJS.$(errorElement).text("For the Verification above, please provide a short delete reason.");
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

function deselectAllVerifications() {
	var checkboxes = AJS.$(".VerificationPageDeleteBox:checked");
	checkboxes.each(function () {
		AJS.$(this).attr("checked", false);
	});
}