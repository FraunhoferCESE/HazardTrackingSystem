console.log("=== hazard-page.js ===");

function initializeHazardPage() {
	if (INIT.HAZARDS === true) {
		INIT.HAZARDS = false;
		initHazardPageClickEvents();
	}
	initHazardPageMultiSelectes();
}

function initHazardPageClickEvents() {
	//when doing a cause transfer, automatically expand the targetcause on the cause page
	AJS.$(".causeLink").click(function(event) {
	    // Get the link that fired the click event
		var targetID = AJS.$(this).attr("targetID");
	
	    modifyHTSCookieOpenCauses("open", targetID, null);
	    // CAll the shared-cookes.js code that will set the user's cookie to expand Cause Number on HazardNumber 
	});
	
	AJS.$("#HazardPageSave").live("click", function() {
		AJS.$("#HazardPageForm").ajaxSubmit({
			async: false,
			success: function(data) {
				console.log("SUCCESS");
				JIRA.Messages.showSuccessMsg(
					"The Hazard was successfully updated.",
					{closeable: true}
				);
			},
			error: function(error) {
				// TODO:
				// return an object here which contain error message
				// display similar message as in success, but of the error kind
				console.log("ERROR");
			}
		});
	});
}

function initHazardPageMultiSelectes() {
	AJS.$("#hazardSubsystem").multiselect2side({
		selectedPosition: 'right',
		moveOptions: false,
		labelsx: '',
		labeldx: '',
		'search': 'Search: ',
		autoSort: true,
		autoSortAvailable: true
	});

	AJS.$("#hazardPhase").multiselect2side({
		selectedPosition: 'right',
		moveOptions: false,
		labelsx: '',
		labeldx: '',
		'search': 'Search: ',
		autoSort: true,
		autoSortAvailable: true
	});

	AJS.$("#hazardGroup").multiselect2side({
		selectedPosition: 'right',
		moveOptions: false,
		labelsx: '',
		labeldx: '',
		'search': 'Search: ',
		autoSort: true,
		autoSortAvailable: true
	});

	// Adjust the CSS
	var multiSelectDivs = AJS.$(".ms2side__div");
	AJS.$(multiSelectDivs).each(function() {
		AJS.$(this).children(":nth-child(1)").children(":nth-child(1)").css("padding-bottom", "3px");
		AJS.$(this).children(":nth-child(3)").css("padding-top", "28px");
	});
}