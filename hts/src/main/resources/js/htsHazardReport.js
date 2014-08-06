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

	var hazardNumberColumn = AJS.$('table#hazardTable tbody td:nth-child(1)');
	if(hazardNumberColumn.length > 0) {
		hazardNumberColumn.each(function () {
			if (AJS.$(this)[0].innerText.length >= 64) {
				var shortend = (AJS.$(this)[0].innerText).substring(0, 61) + "...";
				AJS.$(this)[0].innerText = shortend;
			}
		});
	}

	var hazardTitleColumn = AJS.$('table#hazardTable tbody td:nth-child(2)');
	if(hazardTitleColumn.length > 0) {
		hazardTitleColumn.each(function () {
			if (AJS.$(this)[0].innerText.length >= 128) {
				var shortend = (AJS.$(this)[0].innerText).substring(0, 125) + "...";
				AJS.$(this)[0].innerText = shortend;
			}
		});
	}
}

function getNumberOfCreatedPayloads() {
	var payloads;
	AJS.$.ajax({
		type: "GET",
		async: false,
		url: AJS.params.baseURL + "/rest/htsrest/1.0/report/allpayloads/",
		success: function(data) {
			payloads = data;
		}
	});
	return payloads.length;
}

function getNumberOfCreatedHazards() {
	var hazards;
	AJS.$.ajax({
		type: "GET",
		async: false,
		url: AJS.params.baseURL + "/rest/htsrest/1.0/report/allhazards/",
		success: function(data) {
			hazards = data;
		}
	});
	return hazards.length;
}

function sortListOfPayloads() {
	AJS.$("#listOfCreatedPayloads li").tsort();
}

AJS.$(document).ready(function() {
	layout();
	var currentlyViewingPayload = null;

	var whichPage = AJS.$.url();
	var parameters = AJS.$.url().param();
	if (AJS.$.url().data.seg.path.length === 4) {
		whichPage = AJS.$.url().data.seg.path[3];
	}
	else {
		whichPage = AJS.$.url().data.seg.path[2];
	}

	var createdPayloads = getNumberOfCreatedPayloads();
	if (whichPage === "hazardform" && createdPayloads === 0) {
		JIRA.Messages.showWarningMsg("No Mission/Payloads have been created.<br>Please create a Mission/Payload before proceeding.", {closeable: true});
		//window.location.href = AJS.params.baseURL + "/plugins/servlet/hazardlist";
	}

	if (whichPage === "hazardlist" && AJS.$.isEmptyObject(parameters)) {
		var createdHazards = getNumberOfCreatedHazards();

		if (createdPayloads !== 0) {
			AJS.$("#HazardTableNoPayloadsMessage").hide();
			if (createdHazards === 0) {
			}
			sortListOfPayloads();
		}

		if (createdHazards !== 0) {
			AJS.$("#HazardTableNoHazardsMessage").hide();
		}

		if (createdPayloads === 0 && createdHazards === 0) {
			AJS.$("#HazardTableViewingHeader").hide();
			AJS.$("#HazardTableCreateHazardLink").hide();
		}

		var listOfCreatedPayloads = AJS.$(".getReports");
		if (listOfCreatedPayloads.length > 0) {
			listOfCreatedPayloads.each(function () {
				if (AJS.$(this)[0].text.length >= 64) {
					AJS.$(this)[0].text = AJS.$(this)[0].text.substring(0, 61) + "...";
				}
			});
		}
	}

	if (whichPage === "hazardlist" && parameters.hasOwnProperty("edit")) {
		AJS.$("#downloadHazardReportButton").show();
	}

	if (whichPage === "hazardform" && !AJS.$.isEmptyObject(parameters)) {
		var selectedPayload = parameters.selpay;
		AJS.$("#hazardPayload option[value='" + selectedPayload + "']").prop({selected: true});
	}

	AJS.$(".getReports").live('click', function() {
		var self = AJS.$(this);
		var payloadID = self.data("key");
		if (payloadID === "all") {
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
					AJS.$("#HazardTableViewingHeaderContent")[0].innerHTML = "All Hazard Reports";
				}
			});
			currentlyViewingPayload = null;
		}
		else {
			AJS.$.ajax({
				type: "GET",
				url: "hazardlist?key=" + payloadID,
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
						AJS.$("#HazardTableMessage").text("No Hazard Reports have been created for this Mission/Payload.");
						AJS.$("#HazardTableMessage").show();
					}
					AJS.$("#HazardTableViewingHeaderContent")[0].innerHTML = AJS.$("#listOfCreatedPayloads").find("[data-key='" + payloadID + "']").text();
				}
			});
			currentlyViewingPayload = payloadID;
		}
	});

	AJS.$(".deleteHazardReport").live('click', function() {
		var hazardIDToBeDeleted = AJS.$(this).data("key");
		openDeleteHazardReportDialog(hazardIDToBeDeleted);
	});

	AJS.$(".createNewHazardReport").live("click", function() {
		if (getNumberOfCreatedPayloads() > 0) {
			var restOfURL;
			if (currentlyViewingPayload === null) {
				restOfURL = "/plugins/servlet/hazardform";
			}
			else {
				restOfURL = "/plugins/servlet/hazardform?selpay=" + currentlyViewingPayload;
			}

			if (AJS.$(this).is("button")) {
				window.location.href = AJS.params.baseURL + restOfURL;
			}
			else {
				AJS.$(this).attr("href", AJS.params.baseURL + restOfURL);
			}
		}
		else {
			JIRA.Messages.showWarningMsg("Please create a Mission/Payload before creating a Hazard Report.", {closeable: true});
		}
	});

	AJS.$(".downloadHazardReport").live("click", function() {
		AJS.$("#reportgeneration").submit();
	});

	// AJS.$("#hazardSave").live("click", function() {
	// 	AJS.$("#downloadHazardReportButton").show();
	// });

});