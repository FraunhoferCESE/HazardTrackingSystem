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

	/* TODO:
		Need to check the Verifications cookie here: open Verifications that are suppose to be open, rename Expand button if needed.
		Check out the Causes/Controls to see how to do this.
	*/
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
		//var existingVerificationCount = AJS.$(".VerificationTableToggle").length;
		// Calling function in shared-cookies.js file
		//modifyHTSCookieOpenCauses(operation, verificationID, existingCausesCount);
	});

	// Expand All/Close All Verifcations
	/* TODO:
		Add the click event here. It will be pretty much exactly the same as the Causes/Controls version.
		Need to keep in mind that cookies come into play here. When the user opens a particular Verifications, that gets put into an array in a cookie
		Take a look at the shared-cookie.js file - need to add functions there.
	*/

	// Save new cause
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
		console.log(existingResult);
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
			//openDeleteVerificationDialog(existingResult.deleteExistingCausesIDs, result);
		} else {
			// Display appropriate message and load the template again to see the changes
			displayAppropriateMessage(result, "Verification");
		}

	});
}

function initVerificationPageDateModification() {
	AJS.$(".HTSDate").each(function() {
		var dateStrUnformatted = AJS.$(this).text();
		var dateStrFormatted = formatDate(dateStrUnformatted);
		AJS.$(this).text(dateStrFormatted);
	});

	var estimatedCompletionDates = AJS.$(".VerificationDate");
	estimatedCompletionDates.each(function () {
		var defaultDateArr = (AJS.$(this).data("date")).split(" ");
		console.log(defaultDateArr);
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