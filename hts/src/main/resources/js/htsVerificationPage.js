function manipulateDates(dates) {
	if (dates.length > 0) {
		dates.each(function () {
			if (AJS.$(this)[0].innerText != "N/A") {
				AJS.$(this)[0].innerText = Date.parse(AJS.$(this)[0].innerText.substring(0,19)).toString("MM/dd/yyyy, HH:mm");
			}
		});
	}
}

function addNewVerificationFormIsDirty() {
	if (AJS.$("#verificationDescriptionNew").val() === "" &&
		AJS.$("#verificationTypeNew").val() === "" &&
		AJS.$("#verificationRespPartyNew").val() === "" &&
		AJS.$("#verificationComplDateNew").val() === "")  {
		return false;
	}
	else {
		return true;
	}
}

AJS.$(document).ready(function(){
	/* Text manipulation code begins */
	var dates = AJS.$(".VerificationDate");
	manipulateDates(dates);
	/* Text manipulation code ends */

	AJS.$(".SaveAllVerificationChanges").live("click", function() {

		if (addNewVerificationFormIsDirty()) {
			AJS.$("#addNewVerificationForm").trigger("submit");
			location.reload();
		}

	});

});