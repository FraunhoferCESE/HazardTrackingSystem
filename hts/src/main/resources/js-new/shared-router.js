console.log("=== shared-router.js ===");

var BASE_ROUTE = "/jira/plugins/servlet/";
var ROUTES = {
	MISSIONS: BASE_ROUTE + "missions",
	HAZARDS: BASE_ROUTE + "hazards",
	CAUSES: BASE_ROUTE + "causes",
	CONTROLS: BASE_ROUTE + "controls",
	VERIFICATIONS: BASE_ROUTE + "verifications"
};
var INIT = {
	MISSIONS: true,
	HAZARDS: true,
	CAUSES: true,
	CONTROLS: true,
	VERIFICATIONS: true
};

function loadTemplate(path) {
	AJS.$("#MainContent").load(path + " #ContentHolder", function() {
		var route = path.substr(0, path.indexOf("?"));
		updateSelected(route);
	});
}

function updateSelected(route) {
	if (route === ROUTES.MISSIONS || route === "") {
		AJS.$("#MissionNavItem").addClass("aui-nav-selected");
		initializeMissionPage();
	} else if (route === ROUTES.HAZARDS) {
		AJS.$("#MissionNavItem").removeClass("aui-nav-selected");
		AJS.$("#HazardPagePropertiesNavigation [id='HazardNavItem']").addClass("aui-nav-selected");
		initializeHazardPage();
	} else if (route === ROUTES.CAUSES) {
		AJS.$("#MissionNavItem").removeClass("aui-nav-selected");
		AJS.$("#HazardPagePropertiesNavigation [id='CausesNavItem']").addClass("aui-nav-selected");
		initializeCausePage();
	} else if (route === ROUTES.CONTROLS) {
		AJS.$("#MissionNavItem").removeClass("aui-nav-selected");
		AJS.$("#HazardPagePropertiesNavigation [id='ControlsNavItem']").addClass("aui-nav-selected");
	} else if (route === ROUTES.VERIFICATIONS) {
		AJS.$("#MissionNavItem").removeClass("aui-nav-selected");
		AJS.$("#HazardPagePropertiesNavigation [id='VerificationsNavItem']").addClass("aui-nav-selected");
	}
}

AJS.$(document).ready(function() {
	var currentRoute = AJS.$.url().data.attr.path;
	updateSelected(currentRoute);
	// Calling function in shared-cookies.js file
	initHTSCookie();

	var History = window.History;
	if (History.enabled) {
		var State = History.getState();
		var path = State.hash;
		History.pushState(null, null, path);
	} else {
		return false;
	}

	History.Adapter.bind(window, "statechange", function(){
		var State = History.getState();
		var path = State.hash;
		var route = path.substr(0, path.indexOf("?"));
		// If there is no parameter in the path, then route will be empty
		if (route === "") {
			route = path;
		}

		// Check if legal route, then load the template
		if (route === ROUTES.MISSIONS) {
			loadTemplate(path);
		} else if (route === ROUTES.HAZARDS) {
			loadTemplate(path);
		} else if (route === ROUTES.CAUSES) {
			loadTemplate(path);
		} else if (route === ROUTES.CONTROLS) {
			loadTemplate(path);
		} else if (route === ROUTES.VERIFICATIONS) {
			loadTemplate(path);
		} else {
			console.log("Illegal route!");
		}
	});

	AJS.$(".RouteLink").live("click", function(event) {
		event.preventDefault();
		var path = AJS.$(this).attr("href");
		History.pushState(null, null, path);
	});

});