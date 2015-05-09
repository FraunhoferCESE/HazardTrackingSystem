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
		projectSelect.append("<option></option>").text("<No Projects Available>");
	} else {
		AJS.$.each(projects, function(index, value) {
			projectSelect.append(AJS.$("<option></option>").attr("value",
					value.id).text(value.name));
		});
		
		projectSelect.change(function() {
			alert("I changed");
		});
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

function getHazardsForProject() {
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

