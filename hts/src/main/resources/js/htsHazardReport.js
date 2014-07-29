function openDeleteHazardReportDialog(hazardIDToBeDeleted) {
	var dialog = new AJS.Dialog({
		width: 500,
		height: 190,
		id: "deleteDialog",
	});

	dialog.show();
	dialog.addHeader("Confirm");
	dialog.addPanel("Panel 1", "<p class='dialog-panel-body'>Deleting this Hazard Report will premanently remove it from the HTS.<br>Press Continue to confirm this action.</p>", "panel-body");
	dialog.get("panel:0").setPadding(0);

	dialog.addButton("Continue", function(dialog) {
		deleteHazardReport(hazardIDToBeDeleted);
		dialog.hide();
	});

	dialog.addButton("Cancel", function(dialog) {
		dialog.hide();
	});
}

function deleteHazardReport(hazardIDToBeDeleted) {
	AJS.$.ajax({
		type: "DELETE",
		url: "hazardlist?key=" + hazardIDToBeDeleted,
		success: function(data) {
			AJS.$("#hazardEntryID" + hazardIDToBeDeleted).remove();
			if(AJS.$("#hazardTable tbody tr").length === 0) {
				AJS.$("#hazardTable").hide();
				AJS.$("#HazardTableMessage").text("This Mission/Payload contains no Hazard Reports.");
				AJS.$("#HazardTableMessage").show();
			}
		},
		error: function() {
			console.log("ERROR");
		}
	});
}

function layout() {
	//Fixing the date layout on the landing page. To change the define the new layout in the toString method.
	var lastEditColumn = AJS.$('table#hazardTable tbody td:nth-child(4)');
	if(lastEditColumn.length > 0) {
		lastEditColumn.each(function () {
			AJS.$(this)[0].innerText = Date.parse(AJS.$(this)[0].innerText.substring(0,19)).toString("MMMM dd, yyyy, HH:mm");
		});
	}
}

function navigationFilter() {
	AJS.$(".getReports").live('click', function() {
		var self = AJS.$(this);
		var hazardID = self.data("key");
		if (hazardID === "all") {
			AJS.$.ajax({
				type: "GET",
				url: "hazardlist",
				success: function(html) {
					var hazardTableHTML = AJS.$(html).find("#hazardTable");
					if(hazardTableHTML.length > 0) {
						AJS.$("#HazardTableMessage").hide();
						AJS.$("#hazardTableHolder").html(hazardTableHTML);
						layout();
					}
					else {
						AJS.$("#hazardTable").hide();
						AJS.$("#HazardTableNoHazardsMessage").hide();
						AJS.$("#HazardTableMessage").text("No Hazard Reports have been created.");
						AJS.$("#HazardTableMessage").show();
					}
				}
			});
		}
		else {
			AJS.$.ajax({
				type: "GET",
				url: "hazardlist?key=" + hazardID,
				success: function(html) {
					var hazardTableHTML = AJS.$(html).find("#hazardTable");
					if(hazardTableHTML.length > 0) {
						AJS.$("#HazardTableMessage").hide();
						AJS.$("#hazardTableHolder").html(hazardTableHTML);
						layout();
					}
					else {
						AJS.$("#hazardTable").hide();
						AJS.$("#HazardTableNoHazardsMessage").hide();
						AJS.$("#HazardTableMessage").text("This Mission/Payload contains no Hazard Reports.");
						AJS.$("#HazardTableMessage").show();
					}
				}
			});
		}
	});
}

AJS.$(document).ready(function() {
	layout();
	navigationFilter();

	AJS.$(".deleteHazardReport").live('click', function() {
		var hazardIDToBeDeleted = AJS.$(this).data("key");
		openDeleteHazardReportDialog(hazardIDToBeDeleted);
	});
});