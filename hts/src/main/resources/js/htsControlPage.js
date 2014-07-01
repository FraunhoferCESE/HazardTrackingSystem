function checkElementExpansion(element) {
	return element.is(":visible");
}

function manipulateDate(dateToManipulate) {
	if (dateToManipulate.length > 0) {
		dateToManipulate.each(function () {
			AJS.$(this)[0].innerText = Date.parse(AJS.$(this)[0].innerText.substring(0,19)).toString("MMMM dd, yyyy, HH:mm");
		});
	}
}

AJS.$(document).ready(function(){

	/* Expand functionality code begins */
	AJS.$('.ControlsTableCellToggle').click(function() {
		var entry = AJS.$(this);
		var entryFullID = entry.attr("id");
		var entryID = entryFullID.slice(-1);
		var entryEdit = AJS.$("#ControlsTableEditEntry" + entryID);
		if (entryEdit.hasClass("ControlsTableEditEntryHidden")) {
			entryEdit.removeClass("ControlsTableEditEntryHidden");
			entry.removeClass("aui-icon aui-icon-small aui-iconfont-add");
			entry.addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
		}
		else {
			entryEdit.addClass("ControlsTableEditEntryHidden");
			entry.removeClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
			entry.addClass("aui-icon aui-icon-small aui-iconfont-add");
		}
	});
	/* Expand functionality code ends */

	/* Text manipulation code begins */
	var createdDate = AJS.$(".createdDate");
	//var lastUpdatedDate = AJS.$(".lastUpdatedDate");
	manipulateDate(createdDate);
	//manipulateDate(lastUpdatedDate);

	for (var i = 1; i <= 3; i++) {
		// Shortend description text for each control for cleaner UI
		var descText = AJS.$(".ControlDescText" + i).text();
		if (descText.length > 30) {
			shortendDescText = descText.substr(0,40) + '...';
			AJS.$(".ControlDescText" + i).text(shortendDescText);
		}
	}
	/* Text manipulation code ends */
});