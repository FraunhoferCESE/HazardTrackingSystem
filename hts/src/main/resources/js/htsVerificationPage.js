function manipulateDates(dates) {
	if (dates.length > 0) {
		dates.each(function () {
			if (AJS.$(this)[0].innerText != "N/A") {
				AJS.$(this)[0].innerText = Date.parse(AJS.$(this)[0].innerText.substring(0,19)).toString("MM/dd/yyyy, HH:mm");
			}
		});
	}
}

// function addNewVerificationFormIsDirty() {
// 	var rtn = false;
// 	if (AJS.$("#verificationDescriptionNew").val() !== "") {
// 		rtn =
// 	}
// 	if (AJS.$("#verificationTypeNew").val() !== "") {
// 		return false;
// 	}
// 	if (AJS.$("#verificationRespPartyNew").val() !== "") {
// 		return false;
// 	}
// 	if (AJS.$("#verificationComplDateNew").val() !== "") {
// 		return false;
// 	}

// 	return true;
// }

AJS.$(document).ready(function(){
	/* Text manipulation code begins */
	var dates = AJS.$(".VerificationDate");
	manipulateDates(dates);
	/* Text manipulation code ends */

	AJS.$(".SaveAllVerificationChanges").live("click", function() {
		AJS.$("#addNewVerificationForm").trigger("submit");
		location.reload();

		// if (AJS.$("#addNewVerificationForm").isDirty()) {
		// 	console.log("dirty");
		// }
		// else {
		// 	console.log("clean");
		// }
	});

});