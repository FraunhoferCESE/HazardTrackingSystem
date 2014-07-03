function manipulateDates(dates) {
	if (dates.length > 0) {
		dates.each(function () {
			if (AJS.$(this)[0].innerText != "N/A") {
				AJS.$(this)[0].innerText = Date.parse(AJS.$(this)[0].innerText.substring(0,19)).toString("MM/dd/yyyy, HH:mm");
			}
		});
	}
}

function manipulateDescriptions(descriptions) {
	if (descriptions.length > 0) {
		descriptions.each(function () {
			// Shortend description text for each control for cleaner UI
			if (AJS.$(this)[0].innerText.length > 40) {
				AJS.$(this)[0].innerText = AJS.$(this)[0].innerText.substr(0,39) + '...';
			}
		});
	}
}

AJS.$(document).ready(function(){

	// CSS fixes (on predefined JIRA element)>
	AJS.$('form.aui').css({'margin':'0'});

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
	var dates = AJS.$(".ControlDate");
	manipulateDates(dates);
	var descriptions = AJS.$(".ControlDescriptionText");
	manipulateDescriptions(descriptions);
	/* Text manipulation code ends */
});