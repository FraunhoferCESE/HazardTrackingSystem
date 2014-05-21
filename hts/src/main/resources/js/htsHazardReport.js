function initiateDeleteHazardReports () {
	function confirmation() {
		var defer = AJS.$.Deferred();
		var dialog = new AJS.Dialog({
			width: 500,
			height: 190,
			id: "deleteDialog",
		});
		
		dialog.show();
		dialog.addHeader("Confirm");
		dialog.addPanel("Panel 1", "<p class='dialog-panel-body'>Deleting will premanently remove this hazard report from JIRA. Do you want to continue?</p>", "panel-body");
		dialog.get("panel:0").setPadding(0);
		
		dialog.addButton("Continue", function(dialog) {
			dialog.hide();
			defer.resolve("true");
		});

		dialog.addLink("Cancel", function(dialog) {
			dialog.hide();
		}, "#");

		return defer.promise();
	}

	AJS.$(".deleteHazardReport").live('click', function() {
		var self = AJS.$(this);
		confirmation().then(function(ans) {
			if(ans){
				AJS.$.ajax({
					type: "DELETE",
					url: "hazardlist?key=" + self.data("key"),
					success: function(data) {
						self.parent().parent().parent().parent().remove();
						if(AJS.$("#hazardTable tr").length === 1) {
							//TODO figure out a better way to handle these cases
							var noHazardReports = '<h2 class="noHazard">No Hazard reports have been created</h2>';
							AJS.$("#hazardTableHolder").html(noHazardReports);
						}
					},
					error: function() {
						console.log('error', arguments);
					}
				});
			}
			return false;
		});
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
	if(!AJS.$.trim(AJS.$(".noHazard").html())) {
		AJS.$(".noHazard").remove();
	}
}

function navigationFilter() {
	AJS.$(".getReports").live('click', function() {
		var self = AJS.$(this);
		AJS.$.ajax({
			type: "GET",
			url: "hazardlist?key=" + self.data("key"),
			success: function(html) {
				var hazardTableHTML = AJS.$(html).find("#hazardTable");
				if(hazardTableHTML.length > 0) {
					AJS.$("#hazardTableHolder").html(hazardTableHTML);
					layout();
					initiateDeleteHazardReports();
				}
				else {
					if(!(AJS.$(".noHazard").length > 0)) {
						var noHazardReportCreated = AJS.$(html).find(".noHazard");
						AJS.$("#hazardTableHolder").html(noHazardReportCreated);
					}
				}
			}
		});
	});
}

AJS.$(document).ready(function() {
	layout();
	initiateDeleteHazardReports();
	navigationFilter();
});