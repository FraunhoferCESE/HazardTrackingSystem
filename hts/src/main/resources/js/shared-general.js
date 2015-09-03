console.log("=== shared-general.js ===");

function openForm(toggleElement, displayElement) {
	AJS.$(toggleElement).removeClass("aui-iconfont-add").addClass("aui-iconfont-devtools-task-disabled");
	AJS.$(displayElement).show();
}

function closeForm(toggleElement, displayElement) {
	AJS.$(toggleElement).removeClass("aui-iconfont-devtools-task-disabled").addClass("aui-iconfont-add");
	AJS.$(displayElement).hide();
}

function isOpen(toggleElement) {
	return AJS.$(toggleElement).hasClass("aui-iconfont-devtools-task-disabled");
}

function toggleOpenCloseIcon(clickedElement, displayElement) {
	var operation;
	if (AJS.$(clickedElement).hasClass("aui-iconfont-add")) {
		AJS.$(clickedElement).removeClass("aui-iconfont-add");
		AJS.$(clickedElement).addClass("aui-iconfont-devtools-task-disabled");
		AJS.$(displayElement).show();
		operation = "open";
	}
	else {
		AJS.$(clickedElement).removeClass("aui-iconfont-devtools-task-disabled");
		AJS.$(clickedElement).addClass("aui-iconfont-add");
		AJS.$(displayElement).hide();
		operation = "close";
	}
	return operation;
}

function manipulateTextLength(text, length) {
	if (text.length >= length){
		return text.substring(0, (length - 3)) + "...";
	} else {
		return text;
	}
}

function displayAppropriateMessage(result, property) {
	if (result.existingNoChanges === true && result.addNewNoChanges === true &&
		result.addTransferNoChanges === true) {
		JIRA.Messages.showWarningMsg("No changes were made.", {closeable: true});
	}
	if (result.existingPost === true || result.addNewPost === true ||
		result.addTransferPost === true || result.existingDelete) {
		var successMessage = "The following changes were made:<br>";
		if (result.addNewPost === true) {
			successMessage += "<b> &#149; Created a new " + property + "<b><br>";
		}
		if (result.addTransferPost === true) {
			successMessage += "<b> &#149; Created a new transferred " + property + "<b><br>";
		}
		if (result.existingPost === true) {
			successMessage += "<b> &#149; Updated existing " + property + "(s)<b><br>";
		}
		if (result.existingDelete === true) {
			successMessage += "<b> &#149; Deleted existing " + property + "(s)<b><br>";
		}
		JIRA.Messages.showSuccessMsg(successMessage, {closeable: true});
		var path = AJS.$.url().data.attr.relative;
		loadTemplate(path);

	}
	if (result.existingErrors === true || result.addNewErrors === true ||
		result.addTransferErrors === true) {
		var errorMessage = "There was a problem with the following:<br>";
		if (result.addNewErrors === true) {
			errorMessage += "<b> &#149; Creating a new " + property + "<b><br>";
		}
		if (result.addTransferErrors === true) {
			errorMessage += "<b> &#149; Creating a new transferred " + property + ".<b><br>";
		}
		if (result.existingErrors === true) {
			errorMessage += "<b> &#149; Updating existing " + property + "(s)<b><br>";
		}
		JIRA.Messages.showErrorMsg(errorMessage, {closeable: true});
	}
	
	
}

function assert(condition, message) {
    if (!condition) {
        message = message || "Assertion failed";
        if (typeof Error !== "undefined") {
            throw new Error(message);
        }
        throw message; // Fallback
    }
}

function getTransferTargetDeleteWarning(targetElement, elementType) {
	var html = "";
	
	if(elementType == "cause") {
		html += "<p class='ConfirmDialogErrorText ConfirmDialogHazardAssociationText'>" +
		"<a href='hazards?id=" + targetElement.hazardId + "'>Hazard " + targetElement.hazardNumber + "</a>" +
		" (owned by " + targetElement.hazardOwner + "): ";
		html += "<a href='causes?id=" + targetElement.hazardId + "' class='openAssociatedCause' data-causeid='" + targetElement.causeID + "'>" +
		"Cause " + targetElement.causeNumber + "</a>";
	}
	else if (elementType == "control") {
		html += "<p class='ConfirmDialogErrorText ConfirmDialogHazardAssociationText'>" +
		"<a href='hazards?id=" + targetElement.hazardId + "'>Hazard " + targetElement.hazardNumber + "</a>" +
		" (owned by " + targetElement.hazardOwner + "): ";
		html += "<a href='controls?id=" + targetElement.hazardId + "#controlID"+targetElement.controlID+"' class='openAssociatedControl' data-controlid='" + targetElement.controlID + "'>" +
		"Control " + targetElement.fullyQualifiedNumber + "</a>";
	}
	else if (elementType == "verification") {
		html += "<p class='ConfirmDialogErrorText ConfirmDialogHazardAssociationText'>";
		html += "<a href='verifications?id=" + targetElement.hazardId + "#verificationID"+targetElement.verificationId+"' class='openAssociatedVerification' data-verificationid='" + targetElement.verificationId + "'>" +
		"Verification " + targetElement.fullyQualifiedNumber + "</a>";
	}
	
	html += "</p>";
	return html;
}	