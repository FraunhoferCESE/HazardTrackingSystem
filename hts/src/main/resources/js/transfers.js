console.log("=== transfers.js ===");

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