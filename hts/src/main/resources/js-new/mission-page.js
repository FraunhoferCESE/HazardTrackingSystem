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
}

function updateHazardTable(hazards) {
	var html;
	for (var i = 0; i < hazards.length; i++) {
		var hazardNumber = hazards[i].hazardNumber === undefined ? "&lt;To Be Determined&gt;" : hazards[i].hazardNumber;
		var hazardTitle = hazards[i].hazardTitle === undefined ? "&lt;To Be Determined&gt;" : hazards[i].hazardTitle;

		html += "<tr>" +
					"<td>" + hazardNumber + "</td>" +
					"<td>" + hazardTitle + "</td>" +
					"<td>" + hazards[i].missionTitle + "</td>" +
					"<td>" + formatDate(hazards[i].revisionDate) + "</td>" +
					"<td>" +
						"<ul class='menu'>" +
							"<li><a href='/jira/plugins/servlet/hazards?id=" + hazards[i].hazardID + "' class='RouteLink'>Edit</a></li>" +
							"<li><a href='#'>Print</a></li>" +
						"</ul>" +
					"</td>" +
					"<td><a href='" + hazards[i].jiraSubtaskURL + "'>Open</a></td>" +
				"</tr>";
	}
	AJS.$("#MissionPageTableBody").empty();
	AJS.$("#MissionPageTableBody").append(html);
}
