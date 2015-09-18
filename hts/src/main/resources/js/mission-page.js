console.log("=== mission-page.js ===");

function initializeMissionPage() {
	if (INIT.MISSIONS === true) {
		INIT.MISSIONS = false;
		initMissionClickEvents();
	}
	initMissionPageHeight();
}

function initMissionPageHeight() {
	var theWindow = AJS.$(window).height();
	var elementOne = AJS.$(".aui-header").outerHeight();
	var elementTwo = AJS.$(".aui-navgroup-inner").outerHeight();
	var elementThree = AJS.$(".aui-page-header").outerHeight();
	var elementFour = AJS.$("#footer").outerHeight();
	var adjuster = 10;
	var fakeTableHeight = theWindow - elementOne - elementTwo - elementThree
			- elementFour - adjuster;
	var missionTableHeight = AJS.$("#MissionPageTable").outerHeight();

	if (fakeTableHeight > missionTableHeight) {
		// This is to keep a nice big white background, until there are enough
		// Hazards in the system
		AJS.$(".aui-page-panel-inner").css({
			"height" : fakeTableHeight + "px"
		});
	}
}

function initMissionClickEvents() {
	AJS.$(".PrintLink").live("click", function() {
		var hazardID = AJS.$(this).data("id");
		var form = AJS.$("#report-generation" + hazardID);
		AJS.$(form).submit();
	});

	AJS.$("input#selectAll").change(function() {
		AJS.$(".selectHazard").prop("checked", AJS.$(this).is(":checked"));
	});

	AJS.$("input#PrintHazards").on(
			"click",
			function() {
				var selectedHazards = [];
				AJS.$(".selectHazard:checked").each(
						function() {
							selectedHazards.push("hazardToDownload=" + AJS.$(this).val());
						});

				if (selectedHazards.length > 0) {
					window.location.href = "report-generation?"	+ selectedHazards.join("&");
				}
			});
}