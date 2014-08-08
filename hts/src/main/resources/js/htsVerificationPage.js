function manipulateDates(dates) {
	if (dates.length > 0) {
		dates.each(function () {
			if (AJS.$(this)[0].innerText != "N/A") {
				AJS.$(this)[0].innerText = Date.parse(AJS.$(this)[0].innerText.substring(0,19)).toString("MM/dd/yyyy, HH:mm");
			}
		});
	}
}

function addNewVerificationFormIsDirty() {
	if (AJS.$("#verificationDescriptionNew").val() === "" &&
		AJS.$("#verificationTypeNew").val() === "" &&
		AJS.$("#verificationRespPartyNew").val() === "" &&
		AJS.$("#verificationComplDateNew").val() === "")  {
		return false;
	}
	else {
		return true;
	}
}

function editVerificationFormIsDirty(formElement) {
	if (AJS.$(formElement).find("#verificationDescriptionEdit").isDirty() ||
		AJS.$(formElement).find("#verificationTypeEdit").isDirty() ||
		AJS.$(formElement).find("#verificationRespPartyEdit").isDirty() ||
		AJS.$(formElement).find("#verificationComplDateEdit").isDirty() ||
		AJS.$(formElement).find("#verificationStatusEdit").isDirty()) {
		return true;
	}
	else {
		return false;
	}
}

AJS.$(document).ready(function(){
	/* Text manipulation code begins */
	var dates = AJS.$(".VerificationDate");
	manipulateDates(dates);

	var estCompletionDates = AJS.$(".VerificationEstComplDate");
	estCompletionDates.each(function () {
		var defaultDateArr = (AJS.$(this).data("date")).split(" ");
		var defaultDateStr = defaultDateArr[0];
		AJS.$(this)[0].value = defaultDateStr;
	});
	/* Text manipulation code ends */

	AJS.$(".SaveAllVerificationChanges").live("click", function() {
		var createdVerificationForms = AJS.$(".editVerificationForm");
		var newVerification = false;
		var updatedVerification = false;

		// Check if the user wants to add a new Verification
		if (addNewVerificationFormIsDirty()) {
			AJS.$("#addNewVerificationForm").trigger("submit");
			newVerification = true;
		}

		// Check if the user has edited an existing Verification
		createdVerificationForms.each(function () {
			if (editVerificationFormIsDirty(AJS.$(this))) {
				AJS.$(this).trigger("submit");
				//console.log("dirty");
				updatedVerification = true;
			}
		});

		if (editVerificationFormIsDirty()) {
			updatedVerification = true;
		}

		if (newVerification || updatedVerification) {
			if (AJS.$(".validationError").is(":visible")) {
				JIRA.Messages.showWarningMsg("Not all changes have been saved. See invalid forms below.", {closeable: true});
			}
			else {
				console.log("reload");
				//location.reload();
			}
		}
	});

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
		}
		else {
			entryEdit.addClass("VerificationsTableEditEntryHidden");
			entry.removeClass("aui-iconfont-devtools-task-disabled");
			entry.addClass("aui-iconfont-add");
		}
	});
	/* Expand functionality code ends */



});