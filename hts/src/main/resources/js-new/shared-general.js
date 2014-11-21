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