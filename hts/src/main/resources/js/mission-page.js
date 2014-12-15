console.log("=== mission-page.js ===");

function initializeMissionPage() {
	if (INIT.MISSIONS === true) {
		INIT.MISSIONS = false;
		initMissionClickEvents();
	}
	initMissionPageHeight();
	initHazardPageDateModification();
}

function initMissionPageHeight() {
	var theWindow = AJS.$(window).height();
	var elementOne = AJS.$(".aui-header").outerHeight();
	var elementTwo = AJS.$(".aui-navgroup-inner").outerHeight();
	var elementThree = AJS.$(".aui-page-header").outerHeight();
	var elementFour = AJS.$("#footer").outerHeight();
	var adjuster = 10;
	var fakeTableHeight = theWindow - elementOne - elementTwo - elementThree - elementFour - adjuster;
	var missionTableHeight = AJS.$("#MissionPageTable").outerHeight();

	if (fakeTableHeight > missionTableHeight) {
		// This is to keep a nice big white background, until there are enough Hazards in the system
		AJS.$(".aui-page-panel-inner").css({"height": fakeTableHeight + "px"});
	}
}

function initHazardPageDateModification() {
	AJS.$(".HTSDate").each(function() {
		var dateStrUnformatted = AJS.$(this).text();
		var dateStrFormatted = formatDate(dateStrUnformatted);
		AJS.$(this).text(dateStrFormatted);
	});
}

function initMissionClickEvents() {
	// Mission (left panel) click event
	AJS.$(".LandingPageMissionLink").live("click", function() {
		var missionID = AJS.$(this).data("mission");
		var hazards;
		if (missionID === "all") {
			// Call REST Service method in shared-rest.js file
			hazards = getAllHazards();
		} else {
			// Call REST Service method in shared-rest.js file
			hazards = getAllHazardsByMissionID(missionID);
		}
		if (hazards.length !== 0) {
			// Insert hazards data into the Mission Table
			updateHazardTable(hazards);
		} else {
			// TODO: ...
		}
	});

	AJS.$(".PrintLink").live("click", function() {
		console.log("print stuff");
		var hazardID = AJS.$(this).data("id");
		var form = AJS.$("#report-generation" + hazardID);
		AJS.$(form).submit();
	});
}

function updateHazardTable(hazards) {
	console.log(hazards);
	var html;
	for (var i = 0; i < hazards.length; i++) {
		var hazardNumber = hazards[i].hazardNumber === undefined ? "&lt;To Be Determined&gt;" : hazards[i].hazardNumber;
		var hazardTitle = hazards[i].hazardTitle === undefined ? "&lt;To Be Determined&gt;" : hazards[i].hazardTitle;

		html += "<tr>" +
					"<td>" + hazardNumber + "</td>" +
					"<td>" + hazardTitle + "</td>" +
					"<td><a href='" + hazards[i].jiraProjectURL + "'>" + hazards[i].missionTitle + "</a></td>" +
					"<td>" + formatDate(hazards[i].revisionDate) + "</td>" +
					"<td>" +
						"<ul class='menu'>" +
							"<li><a href='/jira/plugins/servlet/hazards?id=" + hazards[i].hazardID + "' class='RouteLink'>Edit</a></li>" +
							"<li><a href='#' class='PrintLink' data-id='" + hazards[i].hazardID + "'>Print</a></li>" +
							"<form method='post' action='report-generation' id='report-generation" + hazards[i].hazardID + "'>" +
								"<input type='hidden' name='hazardToDownload' id='hazardToDownload' value='" + hazards[i].hazardID + "' />" +
							"</form>" +
						"</ul>" +
					"</td>" +
					"<td><a href='" + hazards[i].jiraSubtaskURL + "'>" + hazards[i].jiraSubtaskSummary + "</a></td>" +
				"</tr>";
	}
	AJS.$("#MissionPageTableBody").empty();
	AJS.$("#MissionPageTableBody").append(html);
}