/**
 * 
 */

function initializeHazardNavigation() {
	projectSelect = AJS.$("#missionNavigation");
	projectSelect.children().remove();

	hazardSelect = AJS.$("#hazardNavigation");
	hazardSelect.children().remove();

	var projects = getProjectsForUser();
	if (projects.length == 0) {
		projectSelect.append("<option></option>").text(
				"<No Projects Available>").attr("value", -1);
	} else {
		AJS.$.each(projects, function(index, value) {
			projectSelect.append(AJS.$("<option></option>").attr("value",
					value.id).text(value.name));
		});

		projectSelect.val(AJS.$("hazard").attr("projectId"))
		projectSelect.change(function() {
			hazardSelect.children().remove();
			selectProject(projectSelect.val());
		});
		
		selectProject(AJS.$("hazard").attr("projectId"));
		hazardSelect.val(AJS.$("hazard").attr("currentId"));
		
		AJS.$("#gotoSelectedHazardReport").click(function() {
			AJS.$("form#hazardNavForm input#selectedHazard").val(hazardSelect.val());
		});
		
	}
}

function selectProject(projectId) {
	if (typeof projectId !== "undefined" && projectId != -1) {
		var hazards = getHazardsForProject(projectId);
		if (hazards.length == 0) {
			hazardSelect.append("<option></option>")
				.text("<No Hazards for Project>");
		} else {
			AJS.$.each(hazards, function(index, value) {
				hazardSelect.append(AJS.$("<option></option>")
						.attr("value",value.hazardID)
						.text(value.hazardNumber == null ? "<Hazard id=" + value.hazardID + ">" : value.hazardNumber));
			});
		}
	}
}

function getProjectsForUser() {
	var projects = [];
	AJS.$.ajax({
		type : "GET",
		url : AJS.params.baseURL + "/rest/hts/1.0/mission/user",
		async : false,
		success : function(data) {
			projects = data;
		},
		error : function() {
			console.log("ERROR");
		}
	});

	return projects;
}

function getHazardsForProject(projectId) {
	var hazards = [];
	AJS.$
			.ajax({
				type : "GET",
				url : AJS.params.baseURL + "/rest/hts/1.0/mission/hazards/"
						+ projectId,
				async : false,
				success : function(data) {
					hazards = data;
				},
				error : function() {
					console.log("ERROR");
				}
			});

	return hazards;
}
