console.log("=== shared-general.js ===");

function formatDate(dateStrUnformatted) {
	var dateObj = new Date(dateStrUnformatted);
	var month = (dateObj.getMonth()+1) > 9 ? (dateObj.getMonth()+1) : "0" + (dateObj.getMonth()+1);
	var day = (dateObj.getDate()) > 9 ? (dateObj.getDate()) : "0" + (dateObj.getDate());
	var hours = (dateObj.getHours()) > 9 ? (dateObj.getHours()) : "0" + (dateObj.getHours());
	var minutes = (dateObj.getMinutes()) > 9 ? (dateObj.getMinutes()) : "0" + (dateObj.getMinutes());
	var dateStrFormatted =
		month + "/" +
		day + "/" +
		dateObj.getFullYear() + " " +
		hours + ":" +
		minutes;
	return dateStrFormatted;
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