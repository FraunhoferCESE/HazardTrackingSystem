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