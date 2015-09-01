console.log("=== verification-page.js ===");

var EXISTING_VERIFICATIONS_SERIALIZED = null;

function initializeVerificationPage() {
	initVerificationPageClickEvents();

	EXISTING_VERIFICATIONS_SERIALIZED = [];
	AJS.$(".VerificationPageFormExisting").each(function() {
		var verificationID = AJS.$(this).find("[name='verificationID']").val();
		var serialized = AJS.$(this).serialize();
		EXISTING_VERIFICATIONS_SERIALIZED[verificationID] = serialized;
	});
	// TODO: This needs updated for Verification page
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

function initVerificationPageClickEvents() {
	
	// Open/close on a cause header
	AJS.$(".VerificationCauseTableToggle").on("click", function() {
		var displayElement = AJS.$(this).parent().siblings('ul');
		if(!isOpen(AJS.$(this))) {			
			openForm(AJS.$(this), displayElement);
		}
		else {
			// TODO: This should close controls and verifications. Close verifications in the cookie
//			AJS.$(this).parent().parent().find(".VerificationControlTableToggle").each(function () {
//				var controlID = AJS.$(this).parent().attr("id").split("ControlTableEntryID")[1];
//				var controlDisplayElement = AJS.$("#ControlTableEntryContentID" + controlID);
//				closeForm(AJS.$(this), controlDisplayElement);
//				modifyHTSCookieOpenControls("close", controlID);
//			});
//			
//			closeForm(AJS.$(this), displayElement);
		}
	});
	
	// Open/close on a control
	AJS.$(".VerificationControlTableToggle").on("click", function() {
		var controlID = AJS.$(this).parent().attr("id").split("ControlTableEntryID")[1];
		var displayElement = AJS.$("#ControlTableEntryContentID" + controlID);
		
		//TODO: This needs to open the control header.
//		if(!isOpen(AJS.$(this))) {
//			openForm(AJS.$(this),displayElement);
//		}
//		else {			
//			closeForm(AJS.$(this),displayElement);
//		}
	});
	
	// Open/close on existing Verification
	AJS.$(".VerificationTableToggle").on("click",	function() {
//		var elementID = AJS.$(this).parent().parent().attr("id");
//		var verificationID = elementID.split("VerificationTableEntryID")[1];
//		var displayElement = AJS.$("#VerificationTableEntryContentID" + verificationID);
//		var operation = toggleOpenCloseIcon(AJS.$(this), displayElement);
//		var existingVerificationCount = AJS.$(".VerificationTableToggle").length;
//
//		// Calling function in shared-cookies.js file
//		modifyHTSCookieOpenVerifications(operation,	verificationID, existingVerificationCount);
	});

	// Expand All button click
	AJS.$("#ControlPageExpandAllButton").on("click", function() {
		// TODO: This needs to open all causes, controls, and verifications. Verification cookie open.
//		AJS.$(".VerificationCauseTableToggle").each(function() {
//			if(!isOpen(AJS.$(this))) {
//				openForm(AJS.$(this), AJS.$(this).parent().siblings('ul'));
//			}
//		});	
//		
//		AJS.$(".VerificationControlTableToggle").each(function() {
//			var controlID = AJS.$(this).parent().attr("id").split("ControlTableEntryID")[1];
//			if(!isOpen(AJS.$(this))) {
//				openForm(AJS.$(this), AJS.$("#ControlTableEntryContentID" + controlID));
//			}
//		});
	});
	
	AJS.$("#ControlPageCloseAllButton").on("click", function() {
		// TODO: This needs to cascadingly close verifications - controls - causes. Verification cookie close.
//		AJS.$(".ControlTableToggle").each(function (index) {
//			var controlID = AJS.$(this).parent().attr("id").split("ControlTableEntryID")[1];
//			var controlDisplayElement = AJS.$("#ControlTableEntryContentID" + controlID);
//			closeForm(AJS.$(this), controlDisplayElement);
//			modifyHTSCookieOpenControls("close", controlID);
//		});
//		
//		AJS.$(".ControlCauseTableToggle").each(function() {
//			if(isOpen(AJS.$(this))) {
//				closeForm(AJS.$(this), AJS.$(this).parent().siblings('ul'));
//			}
//		});	
	});
	
	// Clear new Verification form
	AJS.$("#VerificationPageClearNew").on("click", function() {
		var formElement = AJS.$("#VerificationPageFormAddNew");
		AJS.$(formElement).find("#verificationDescription").val("");
		AJS.$(formElement).find("#verificationStatus").val("").trigger('chosen:updated');
		AJS.$(formElement).find("#verificationType").val("").trigger('chosen:updated');
		AJS.$(formElement).find("#verificationRespParty").val("");
		AJS.$(formElement).find("#verificationEstComplDate").val("");
		AJS.$(formElement).find("#verificationControlAssociation").val("");
	});

	// Add new Verification click event
	AJS.$("#AddNewVerification").on("click", function() {
		if(!isOpen(AJS.$(this))) {
			openForm(AJS.$(this), AJS.$("#VerificationPageNewContainer"));
		}
		else {
			closeForm(AJS.$(this), AJS.$("#VerificationPageNewContainer"));
		}
	});
	
	// Add new transfer click event
	AJS.$("#VerificationPageAddTransfer").on("click", function() {
		if(!isOpen(AJS.$(this))) {
			openForm(AJS.$(this), AJS.$("#VerificationPageTransferContainer"));
		}
		else {
			closeForm(AJS.$(this), AJS.$("#VerificationPageTransferContainer"));
		}
	});
	
	// TODO: Need to add event handler to clear the new Verification transfer form
	// Save new verification
	AJS.$(".VerificationPageSaveAllChanges").on("click", function() {
		var result = {
			existingPost : false,
			existingDelete : false,
			existingErrors : false,
			existingNoChanges : false,
			addNewPost : false,
			addNewErrors : false,
			addNewNoChanges : false,
			addTransferPost : false,
			addTransferErrors : false,
			addTransferNoChanges : false
		};

		var operation = AJS.$(this).data("ops");
		if (operation === "new" || operation === "all") {
			var addNewResult = addNewVerificationFormValidation();
			if (addNewResult.dirty === true	&& addNewResult.validated === true) {
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
		
		if (existingResult.validated === true
				&& existingResult.modifiedExistingVerificationsIDs.length === 0
				&& existingResult.deleteExistingVerificationsIDs.length === 0
				&& existingResult.modifiedDeleteExistingVerificationsIDs.length === 0) {
			result.existingNoChanges = true;
		}
		
		if (existingResult.modifiedExistingVerificationsIDs.length !== 0) {
			result.existingPost = true;
			for (var i = 0; i < existingResult.modifiedExistingVerificationsIDs.length; i++) {
				var formElement = AJS.$("input[name='verificationID'][value='"
										+ existingResult.modifiedExistingVerificationsIDs[i]
										+ "']").closest("form");
				postFormToVerificationServlet(formElement);
			}
		}
		
		if (existingResult.deleteExistingVerificationsIDs.length !== 0) {
			result.existingDelete = true;
			openDeleteVerificationDialog(existingResult.deleteExistingVerificationsIDs,	result);
		} else {
			// Display appropriate message and load the template
			// again to see the changes
			displayAppropriateMessage(result, "Verification");
		}

	});

	// Duplicate delete reason in delete dialog
	AJS	.$("#ConfirmDialogDuplBtnVerifications").on("click", function() {
		var reasonTextFields = AJS.$(".ConfirmDialogReasonTextInput");
		var reasonToDuplicate;
		reasonTextFields.each(function(index) {
			if (index === 0) {
				reasonToDuplicate = AJS.$(this).val();
				if (reasonToDuplicate === "") {
					var verificationID = AJS.$(this).attr("id").replace(/^\D+/g, '');
					var errorElement = AJS.$("#ConfirmDialogErrorTextForVerifificationID"+ verificationID);
					AJS.$(errorElement).text("For the Verification above, please provide a short delete reason.");
					AJS.$(errorElement).show();
					return false;
				}
			} else {
				AJS.$(this)[0].value = reasonToDuplicate;
			}
		});
	});
}

function existingVerificationFormValidation() {
	var modifiedExistingVerificationsIDs = [];
	var deleteExistingVerificationsIDs = [];
	var modifiedDeleteExistingVerificationsIDs = [];
	var validated = true;

	AJS.$(".VerificationPageFormExisting").each(function() {
		var verificationID = AJS.$(this).find("[name='verificationID']").val();
		var deleteSelected = AJS.$(".VerificationPageDeleteBox[value='"	+ verificationID + "']").is(':checked');
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

	return {
		"validated" : validated,
		"modifiedExistingVerificationsIDs" : modifiedExistingVerificationsIDs,
		"deleteExistingVerificationsIDs" : deleteExistingVerificationsIDs,
		"modifiedDeleteExistingVerificationsIDs" : modifiedDeleteExistingVerificationsIDs
	};
}

function addNewVerificationFormValidation() {
	var formElement = AJS.$("#VerificationPageFormAddNew");
	var dirty = false;
	var validated = false;
	if (AJS.$(formElement).find("#verificationDescription").val() !== ""
			|| AJS.$(formElement).find("#verificationStatus").val() !== ""
			|| AJS.$(formElement).find("#verificationType").val() !== ""
			|| AJS.$(formElement).find("#verificationRespParty").val() !== ""
			|| AJS.$(formElement).find("#verificationEstComplDate").val() !== "") {
		dirty = true;
	}
	if (dirty === true) {
		if (AJS.$(formElement).find("#verificationDescription").val() === "") {
			AJS.$(formElement).find("[data-error='verificationDescription']").show();
		} else {
			validated = true;
		}
	}
	return {
		"dirty" : dirty,
		"validated" : validated
	};
}

function postFormToVerificationServlet(formElement) {
	AJS.$(formElement).ajaxSubmit({
		async : false,
		success : function(data) {
			console.log("SUCCESS");
		},
		error : function(error) {
			console.log("ERROR");
		}
	});
}

// The following functions have to do with deleting Verifications
function openDeleteVerificationDialog(verificationIDsToDelete, result) {
	var html1 = "<span class='ConfirmDialogHeadingOne'>Hazard Title: <span class='ConfirmDialogHeadingOneContent'>"
			+ AJS.$("#MissionHazardNavHazardTitle").text()
			+ "</span></span>"
			+ "<span class='ConfirmDialogHeadingOne'>Hazard #: <span class='ConfirmDialogHeadingOneContent'>"
			+ AJS.$("#MissionHazardNavHazardNumber").text() + "</span></span>";
	var html2;
	if (verificationIDsToDelete.length === 1) {
		html2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>"
				+ "The following Verification will be deleted from the above Hazard Report. In order to complete the deletion, you will need to provide a short delete reason."
				+ "</span></div>";
	} else {
		html2 = "<div class='ConfirmDialogContentTwo'><span class='ConfirmDialogHeadingTwo'>"
				+ "The following Verifications will be deleted from the above Hazard Report. In order to complete the deletion, you will need to provide a short delete reason for each of the Verifications."
				+ "</span></div>";
	}
	var html3 = "<table>"
			+ "<thead>"
			+ "<tr>"
			+ "<th class='ConfirmDialogTableHeader ConfirmDialogTableCellOne'>#</th>"
			+ "<th class='ConfirmDialogTableHeader ConfirmDialogTableCellTwo'>Description</th>"
			+ "<th class='ConfirmDialogTableHeader ConfirmDialogTableCellThree'>Status</th>"
			+ "</tr>" + "</thead>" + "<tbody>";
	for (var i = 0; i < verificationIDsToDelete.length; i++) {
		var verificationFirstRow = AJS.$("#VerificationTableEntryID"
				+ verificationIDsToDelete[i]);
		html3 += "<tr><td colspan='100%' class='ConfirmDialogTableNoBorder'><div class='ConformDialogEmptyRow'></div></td></tr>";
		html3 += "<tr><td class='ConfirmDialogTableNoBorder'>"
				+ verificationFirstRow.children(":nth-child(2)").text()
						.replace("Verification ", "") + "</td>";
		html3 += "<td class='ConfirmDialogTableNoBorder'><div class='ConfirmDialogDescriptionText'>"
				+ verificationFirstRow.children(":nth-child(3)").text()
				+ "</div></td>";
		html3 += "<td class='ConfirmDialogTableNoBorder'>"
				+ verificationFirstRow.children(":nth-child(4)").text()
				+ "</td>";
		html3 += "</tr>";

		if (i === 0 && verificationIDsToDelete.length > 1) {
			html3 += "<tr>"
					+ "<td colspan='100%' class='ConfirmDialogTableNoBorder'>"
					+ "<div class='ConfirmDialogLabelContainer'>"
					+ "<label for='ReasonTextForControl'><span class='HTSRequired'>* </span>Reason</label>"
					+ "</div>"
					+ "<div class='ConfirmDialogReasonTextContainer'>"
					+ "<input type='text' class='ConfirmDialogReasonTextInput' name='ReasonTextForVerification' id='ReasonTextForVerificationID"
					+ verificationIDsToDelete[i]
					+ "'>"
					+ "</div>"
					+ "<div class='ConfirmDialogDuplButtonContainer'>"
					+ "<button class='aui-button ConfirmDialogDuplButton' id='ConfirmDialogDuplBtnVerifications'>Apply to all</button>"
					+ "</div>" + "</td>" + "</tr>";
		} else {
			html3 += "<tr>"
					+ "<td colspan='100%' class='ConfirmDialogTableNoBorder'>"
					+ "<div class='ConfirmDialogLabelContainer'>"
					+ "<label for='ReasonTextForVerification'><span class='HTSRequired'>* </span>Reason</label>"
					+ "</div>"
					+ "<div class='ConfirmDialogReasonTextContainerNoButton'>"
					+ "<input type='text' class='ConfirmDialogReasonTextInput' name='ReasonTextForVerification' id='ReasonTextForVerificationID"
					+ verificationIDsToDelete[i] + "'>" + "</div>" + "</td>"
					+ "</tr>";
		}
		html3 += "<tr>"
				+ "<td colspan='100%' class='ConfirmDialogTableNoBorder'>"
				+ "<p class='ConfirmDialogErrorText ConfirmDialogErrorTextHidden' id='ConfirmDialogErrorTextForVerificationID"
				+ verificationIDsToDelete[i] + "'></p>" + "</td>" + "</tr>";
	}
	html3 += "<tr><td colspan='100%' class='ConfirmDialogTableNoBorder'><div class='ConformDialogEmptyRow'></div></td></tr></tbody></table>";

	var dialog = new AJS.Dialog({
		width : 600,
		height : 475,
		id : "deleteDialog",
	});

	dialog.addHeader("Confirm");
	dialog.addPanel("Panel 1", "<div class='panelBody'>" + html1 + html2
			+ html3 + "</div>", "panel-body");
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
				postDeleteToVerificationServlet(verificationIDsToDelete[i],
						AJS.$("#ReasonTextForVerificationID"+ verificationIDsToDelete[i]).val());
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
		type : "DELETE",
		async : false,
		url : "verifications?id=" + verificationIDToDelete + "&reason="	+ reason,
		success : function(data) {
			console.log("SUCCESS");
			modifyHTSCookieOpenVerifications("close", verificationIDToDelete, null);
		},
		error : function(data) {
			console.log("ERROR");
		}
	});
}

function deleteVerificationFormValidation(verificationIDsToDelete) {
	var validated = true;
	for (var i = 0; i < verificationIDsToDelete.length; i++) {
		var deleteReason = AJS.$("#ReasonTextForVerificationID" + verificationIDsToDelete[i]).val();
		var errorElement = AJS.$("#ConfirmDialogErrorTextForVerificationID"	+ verificationIDsToDelete[i]);
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
	checkboxes.each(function() {
		AJS.$(this).attr("checked", false);
	});
}