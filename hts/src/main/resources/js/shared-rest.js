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
		url: AJS.params.baseURL + "/rest/hts/1.0/hazard/cause/" + hazardID,
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

function getTransferOrigins(hazardElement, elementType) {
	var result = [];
 	AJS.$.ajax({
		type: "GET",
		url: AJS.params.baseURL + "/rest/hts/1.0/transfer/findOrigins?type=" + elementType + "&elementId=" + hazardElement,
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

function getTransferTargetDeleteWarning(transferOrigins) {
	
	
}


/*
"<p class='ConfirmDialogErrorText'>Warning: This cause is the target of a transfer:</p>" +

									"<p class='ConfirmDialogErrorText ConfirmDialogHazardAssociationText'>" +
										"<a href='hazardlist?edit=y&key=" + associatedCause.hazardID + "'>Hazard " + associatedCause.hazardNumber + "</a>" +
										" (owned by " + associatedCause.hazardOwner + "): ";
										
										
										
										
										"<a href='causeform?edit=y&key=" + associatedCause.hazardID + "' class='openAssociatedCause' data-causeid='" + associatedCause.originCauses[k].originCauseID + "'>" +
											"Cause " + associatedCause.originCauses[k].originCauseNumber +
										
										"<a href='controlform?edit=y&key=" + associatedCause.hazardID + "' class='openAssociatedControl' data-controlid='" + associatedCause.originControls[l].originControlID + "'>" +
										"Control " + associatedCause.originControls[l].originControlNumber +

										*/