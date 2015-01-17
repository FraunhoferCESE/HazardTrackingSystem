console.log("=== shared-cookies.js ===");

function initHTSCookie() {
	if (AJS.Cookie.read("HTS_COOKIE") === undefined) {
		var htsCookieJson = {
			OPEN_CAUSES: [],
			OPEN_CONTROLS: [],
			OPEN_VERIFICATIONS: []
		};
		AJS.Cookie.save("HTS_COOKIE", JSON.stringify(htsCookieJson));
	}
}

function openPropertyElement(buttonElement, contentElement) {
	AJS.$(buttonElement).removeClass("aui-iconfont-add");
	AJS.$(buttonElement).addClass("aui-iconfont-devtools-task-disabled");
	AJS.$(contentElement).show();
}

// Causes
function modifyHTSCookieOpenCauses(operation, causeID, existingCausesCount) {
	if (AJS.Cookie.read("HTS_COOKIE") !== undefined) {
		var htsCookieJson = JSON.parse(AJS.Cookie.read("HTS_COOKIE"));
		if (operation === "open") {
			htsCookieJson.OPEN_CAUSES.push(causeID);
		} else {
			var indexOfID = htsCookieJson.OPEN_CAUSES.indexOf(causeID);
			if (indexOfID > -1) {
				htsCookieJson.OPEN_CAUSES.splice(indexOfID, 1);
			}
		}
		AJS.Cookie.save("HTS_COOKIE", JSON.stringify(htsCookieJson));
		// Check if Expand All button needs renaming
		if (existingCausesCount !== null) {
			renameCausePageExpandButton(existingCausesCount);
		}
	}
}

function openHTSCookieOpenCauses() {
	if (AJS.Cookie.read("HTS_COOKIE") !== undefined) {
		var htsCookieJson = JSON.parse(AJS.Cookie.read("HTS_COOKIE"));
		for (var i = 0; i < htsCookieJson.OPEN_CAUSES.length; i++) {
			var causeID = htsCookieJson.OPEN_CAUSES[i];
			var buttonElement = AJS.$("#CauseTableEntryID" + causeID + " td:first-child").children("div");
			var contentElement = AJS.$("#CauseTableEntryContentID" + causeID);
			openPropertyElement(buttonElement, contentElement);
		}
	}
}

function renameCausePageExpandButton(existingCausesCount) {
	if (AJS.Cookie.read("HTS_COOKIE") !== undefined) {
		var htsCookieJson = JSON.parse(AJS.Cookie.read("HTS_COOKIE"));
		if (existingCausesCount === htsCookieJson.OPEN_CAUSES.length) {
			AJS.$("#CausePageExpandAllButton").val("Close All");
		} else {
			AJS.$("#CausePageExpandAllButton").val("Expand All");
		}
	}
}

// Controls
function modifyHTSCookieOpenControls(operation, controlID, existingControlsCount) {
	if (AJS.Cookie.read("HTS_COOKIE") !== undefined) {
		var htsCookieJson = JSON.parse(AJS.Cookie.read("HTS_COOKIE"));
		if (operation === "open") {
			htsCookieJson.OPEN_CONTROLS.push(controlID);
		} else {
			var indexOfID = htsCookieJson.OPEN_CONTROLS.indexOf(controlID);
			if (indexOfID > -1) {
				htsCookieJson.OPEN_CONTROLS.splice(indexOfID, 1);
			}
		}
		AJS.Cookie.save("HTS_COOKIE", JSON.stringify(htsCookieJson));
		// Check if Expand All button needs renaming
		if (existingControlsCount !== null) {
			renameControlPageExpandButton(existingControlsCount);
		}
	}
}

function openHTSCookieOpenControls() {
	if (AJS.Cookie.read("HTS_COOKIE") !== undefined) {
		var htsCookieJson = JSON.parse(AJS.Cookie.read("HTS_COOKIE"));
		for (var i = 0; i < htsCookieJson.OPEN_CONTROLS.length; i++) {
			var controlID = htsCookieJson.OPEN_CONTROLS[i];
			var buttonElement = AJS.$("#ControlTableEntryID" + controlID + " td:first-child").children("div");
			var contentElement = AJS.$("#ControlTableEntryContentID" + controlID);
			openPropertyElement(buttonElement, contentElement);
		}
	}
}

function renameControlPageExpandButton(existingControlsCount) {
	if (AJS.Cookie.read("HTS_COOKIE") !== undefined) {
		var htsCookieJson = JSON.parse(AJS.Cookie.read("HTS_COOKIE"));
		if (existingControlsCount === htsCookieJson.OPEN_CONTROLS.length) {
			AJS.$("#ControlPageExpandAllButton").val("Close All");
		} else {
			AJS.$("#ControlPageExpandAllButton").val("Expand All");
		}
	}
}

// Verifications
function modifyHTSCookieOpenVerifications(operation, verificationID, existingVerificationsCount) {
	if (AJS.Cookie.read("HTS_COOKIE") !== undefined) {
		var htsCookieJson = JSON.parse(AJS.Cookie.read("HTS_COOKIE"));
		if (operation === "open") {
			htsCookieJson.OPEN_VERIFICATIONS.push(verificationID);
		} else {
			var indexOfID = htsCookieJson.OPEN_VERIFICATIONS.indexOf(verificationID);
			if (indexOfID > -1) {
				htsCookieJson.OPEN_VERIFICATIONS.splice(indexOfID, 1);
			}
		}
		AJS.Cookie.save("HTS_COOKIE", JSON.stringify(htsCookieJson));
		// Check if Expand All button needs renaming
		if (existingVerificationsCount !== null) {
			renameVerificationPageExpandButton(existingVerificationsCount);
		}
	}
}

function openHTSCookieOpenVerifications() {
	if (AJS.Cookie.read("HTS_COOKIE") !== undefined) {
		var htsCookieJson = JSON.parse(AJS.Cookie.read("HTS_COOKIE"));
		for (var i = 0; i < htsCookieJson.OPEN_VERIFICATIONS.length; i++) {
			var verificationID = htsCookieJson.OPEN_VERIFICATIONS[i];
			var buttonElement = AJS.$("#VerificationTableEntryID" + verificationID + " td:first-child").children("div");
			var contentElement = AJS.$("#VerificationTableEntryContentID" + verificationID);
			openPropertyElement(buttonElement, contentElement);
		}
	}
}

function renameVerificationPageExpandButton(existingVerificationsCount) {
	if (AJS.Cookie.read("HTS_COOKIE") !== undefined) {
		var htsCookieJson = JSON.parse(AJS.Cookie.read("HTS_COOKIE"));
		if (existingVerificationsCount === htsCookieJson.OPEN_VERIFICATIONS.length) {
			AJS.$("#VerificationPageExpandAllButton").val("Close All");
		} else {
			AJS.$("#VerificationPageExpandAllButton").val("Expand All");
		}
	}
}