console.log("=== shared-rest.js ===");

// REST Service call to get all hazards
function getAllHazards() {
	var result = [];
	AJS.$.ajax({
		type: "GET",
		url: AJS.params.baseURL + "/rest/hts/1.0/hazard/all",
		async: false,
		success: function(data) {
			console.log("SUCCESS");
			result = data;
		},
		error: function() {
			console.log("ERROR");
		}
	});
	return result;
}

// REST Service call to get all hazards
function getAllHazardsByMissionID(missionID) {
	var result = [];
	AJS.$.ajax({
		type: "GET",
		url: AJS.params.baseURL + "/rest/hts/1.0/mission/hazards/" + missionID,
		async: false,
		success: function(data) {
			console.log("SUCCESS");
			result = data;
		},
		error: function() {
			console.log("ERROR");
		}
	});
	return result;
}

// REST Service call to get all Causes within a Hazard
function getAllCausesWithinHazard(hazardID) {
	var result = [];
	AJS.$.ajax({
		type: "GET",
		url: AJS.params.baseURL + "/rest/hts/1.0/hazard/causes/" + hazardID,
		async: false,
		success: function(data) {
			console.log("SUCCESS");
			result = data;
		},
		error: function() {
			console.log("ERROR");
		}
	});
	return result;
}

// REST Service call to get all Controls within a Cause
function getAllControlsWithinCause(causeID) {
	var result = [];
	AJS.$.ajax({
		type: "GET",
		url: AJS.params.baseURL + "/rest/hts/1.0/cause/control/" + causeID,
		async: false,
		success: function(data) {
			console.log("SUCCESS");
			result = data;
		},
		error: function() {
			console.log("ERROR");
		}
	});
	return result;
}