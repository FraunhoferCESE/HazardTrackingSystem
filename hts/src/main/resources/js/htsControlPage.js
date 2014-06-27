function checkElementExpansion(element) {
	return element.is(":visible");
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