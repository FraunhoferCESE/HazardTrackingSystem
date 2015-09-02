console.log("=== shared-rest.js ===");

// REST Service call to get all hazards
function getAllHazardsByMissionID(missionID) {
	var result = [];
	AJS.$.ajax({
		type: "GET",
		url: AJS.params.baseURL + "/rest/hts/1.0/mission/hazards/" + missionID,
		async: false,
		success: function(data) {
			result = data;
		},
		error: function() {
			console.log("ERROR");
		}
	});
	return result;
}

// REST Service call to get all Causes within a Hazard
function getAllCausesWithinHazard(hazardID, includeTransfers) {
	var result = [];
	AJS.$.ajax({
		type: "GET",
		url: AJS.params.baseURL + "/rest/hts/1.0/hazard/cause/" + hazardID + "?includeTransfers="+includeTransfers,
		async: false,
		success: function(data) {
			result = data;
		},
		error: function() {
			console.log("ERROR");
		}
	});
	return result;
}

// REST Service call to get all Controls within a Cause
function getAllControlsWithinCause(causeID, includeTransfers) {
	var result = [];
	AJS.$.ajax({
		type: "GET",
		url: AJS.params.baseURL + "/rest/hts/1.0/cause/control/" + causeID + "?includeTransfers=" + includeTransfers,
		async: false,
		success: function(data) {
			result = data;
		},
		error: function() {
			console.log("ERROR");
		}
	});
	return result;
}

function getTransferOrigins(hazardElement, elementType) {
	var transferOrigins = [];
 	AJS.$.ajax({
		type: "GET",
		url: AJS.params.baseURL + "/rest/hts/1.0/transfer/findOrigins?type=" + elementType + "&elementId=" + hazardElement,
		async: false,
		success: function(data) {
			transferOrigins = data;
		},
		error: function() {
			console.log("ERROR");
		}
	}); 

	return transferOrigins;
}
