function navigateTo(trigger, contentId){
    AJS.$("#main-nav li").removeClass("aui-nav-selected");
    AJS.$("#" + trigger).addClass("aui-nav-selected");
    AJS.$(".nav-content").hide();
    AJS.$("#" + contentId).show();
}

function manipulateTextLength(theText, numChars) {
    if (theText.length >= numChars){
        return theText.substring(0, numChars - 3) + "...";
    }
    else {
        return theText;
    }
}

function setNavigationErrorMessage(theMessage) {
    AJS.$("#navigationError").text(theMessage);
    AJS.$("#navigationError").show();
}

function initializePayloadDropDownSelection(currentHazardPayloadID) {
    var missionList;
    AJS.$.ajax({
        type: "GET",
        async: false,
        url: AJS.params.baseURL + "/rest/htsrest/1.0/report/allpayloads/",
        success: function(data) {
            missionList = data;
        }
    });

    if(missionList.length > 0) {
        var temp;
        if (currentHazardPayloadID === null) {
            temp = "<option value='' selected>-Select Mission/Payload-</option>";
        }
        else {
            temp = "<option value=''>-Select Mission/Payload-</option>";
        }
        AJS.$(missionList).each(function() {
            if (this.payloadID === currentHazardPayloadID ) {
                temp += "<option value=" + this.payloadID + " selected>" + manipulateTextLength(this.title, 85) + "</option>";
            }
            else {
                temp += "<option value=" + this.payloadID + ">" + manipulateTextLength(this.title, 85) + "</option>";
            }
        });
        AJS.$("#payloadNavigationList").append(temp);
        AJS.$("#payloadNavigationList option").tsort();
    }
}

function initializeHazardDropDownSelection(currentHazardPayloadID, currentHazardID) {
    var hazardList;
    AJS.$.ajax({
        type:"GET",
        async: false,
        url: AJS.params.baseURL + "/rest/htsrest/1.0/report/allpayloads/" + currentHazardPayloadID,
        success: function(data) {
            hazardList = data;
        }
    });

    AJS.$("#hazardNavigationList").children().remove();
    if(hazardList.length > 0) {
        var temp;
        if (currentHazardID === null) {
            temp = "<option value='' selected>-Select Hazard Report-</option>";
        }
        else {
            temp = "<option value=''>-Select Hazard Report-</option>";
        }
        AJS.$(hazardList).each(function() {
            if (this.hazardID === currentHazardID) {
                temp += "<option value=" + this.hazardID + " selected>" + manipulateTextLength(this.hazardNumber, 25) + " - " + manipulateTextLength(this.title, 57) + "</option>";
            }
            else {
                temp += "<option value=" + this.hazardID + ">" + manipulateTextLength(this.hazardNumber, 25) + " - " + manipulateTextLength(this.title, 57) + "</option>";
            }
        });
        AJS.$("#hazardNavigationList").append(temp);
        AJS.$("#hazardNavigationList option").tsort();
    }
    else {
        AJS.$("#hazardNavigationList").append("<option value=''>-No HRs have been created-</option>");
    }
    return hazardList.length;
}

function initializeHazardDropDownSelectionToEmpty(noHazardReports) {
    if (noHazardReports) {
        AJS.$("#hazardNavigationList").children().remove();
        AJS.$("#hazardNavigationList").append("<option value=''>-No HRs have been created-</option>");
    }
    else {
        AJS.$("#hazardNavigationList").children().remove();
        AJS.$("#hazardNavigationList").append("<option value=''>-Select Hazard Report-</option>");
    }
}

AJS.$(document).ready(function(){
    AJS.$("#hazardPayload option").tsort();

    AJS.$(".aui-page-header").css({"padding-top":"5px", "padding-bottom":"5px"});
    AJS.$(".error").hide();
    if(window.location.href.indexOf("hazardform") > -1 || window.location.href.indexOf("hazardlist") > -1) {
        navigateTo("hazard-nav-item", "content-1");
    }
    else if(window.location.href.indexOf("causeform") > -1) {
        navigateTo("cause-nav-item", "content-2");
    }
    else if(window.location.href.indexOf("controlform") > -1) {
        navigateTo("control-nav-item", "content-3");
    }
    else if(window.location.href.indexOf("verificationform") > -1) {
        navigateTo("verification-nav-item", "content-4");
    }

    if(window.location.href.indexOf("?edit=y") > -1) {
        AJS.$("#ViewAllNav").removeClass("aui-nav-selected");
        AJS.$("#CreateHazardNav").removeClass("aui-nav-selected");
    }
    else if(window.location.href.indexOf("hazardlist") > -1 ) {
        //Left blank intentionally.
    }
    else {
        AJS.$("#CreateHazardNav").addClass("aui-nav-selected");
        AJS.$("#ViewAllNav").removeClass("aui-nav-selected");
    }

    var whichPage = AJS.$.url();
    var parameters = AJS.$.url().param();
    var selectedPayload = null;
    if (AJS.$.url().data.seg.path.length === 4) {
        whichPage = AJS.$.url().data.seg.path[3];
    }
    else {
        whichPage = AJS.$.url().data.seg.path[2];
    }
    if (!AJS.$.isEmptyObject(parameters)) {
        selectedPayload = parseInt(parameters.selpay, 10);
    }

    var currentHazardPayloadID;
    var currentHazardID;
    var numberOfHazardsFetched;
    if (whichPage === "hazardlist" && AJS.$.url().param("edit") === "y" ||
        whichPage === "causeform" && AJS.$.url().param("edit") === "y" ||
        whichPage === "controlform" && AJS.$.url().param("edit") === "y" ||
        whichPage === "verificationform" && AJS.$.url().param("edit") === "y") {
        var currentHazardPayloadElement = AJS.$("#hazardPayloadID")[0];
        if (currentHazardPayloadElement !== undefined) {
            var currentHazardPayloadIDStr = currentHazardPayloadElement.value;
            currentHazardPayloadID = parseInt(currentHazardPayloadIDStr, 10);
            var currentHazardIDStr = AJS.$("#hazardID")[0].value;
            currentHazardID = parseInt(currentHazardIDStr, 10);
            initializePayloadDropDownSelection(currentHazardPayloadID);
            numberOfHazardsFetched = initializeHazardDropDownSelection(currentHazardPayloadID, currentHazardID);
        }
    }
    if (whichPage === "hazardform") {
        if (selectedPayload === null) {
            initializePayloadDropDownSelection(null);
            initializeHazardDropDownSelectionToEmpty(false);
        }
        else {
            initializePayloadDropDownSelection(selectedPayload);
            numberOfHazardsFetched = initializeHazardDropDownSelection(selectedPayload, null);
        }
    }

    AJS.$("#payloadNavigationList").change(function(){
        var currentHazardPayloadIDStr = AJS.$(this).val();
        currentHazardPayloadID = parseInt(currentHazardPayloadIDStr, 10);
        if (currentHazardPayloadIDStr !== "") {
            numberOfHazardsFetched = initializeHazardDropDownSelection(currentHazardPayloadID, null);
            if (numberOfHazardsFetched === 0) {
                initializeHazardDropDownSelectionToEmpty(true);
            }
        }
        else {
            initializeHazardDropDownSelectionToEmpty(false);
        }
    });

    AJS.$("#gotoSelectedHazardReport").live("click", function() {
        var gotoPayloadID = AJS.$("#payloadNavigationList").val();
        var gotoHazardID = AJS.$("#hazardNavigationList").val();

        if (gotoPayloadID === "" && gotoHazardID === "") {
            setNavigationErrorMessage("Please select a Mission/Payload and a Hazard Report.");
        }
        else if (numberOfHazardsFetched === 0) {
            setNavigationErrorMessage("No HRs have been created for this Mission/Payload.");
        }
        else if (gotoHazardID === "") {
            setNavigationErrorMessage("Please select a Hazard Report.");
        }
        else {
            if (gotoHazardID !== currentHazardIDStr) {
                window.location = AJS.params.baseURL + "/plugins/servlet/hazardlist?edit=y&key=" + gotoHazardID;
            }
            else {
                setNavigationErrorMessage("You are currently viewing this Hazard Report.");
            }
        }
    });

    AJS.$("#payloadNavigationList").live("click", function() {
        if (AJS.$("#navigationError").is(":visible")) {
            AJS.$("#navigationError").hide();
        }
    });

    AJS.$("#hazardNavigationList").live("click", function() {
        if (AJS.$("#navigationError").is(":visible")) {
            AJS.$("#navigationError").hide();
        }
    });

    AJS.$("#ErrorPageLink").live("click", function() {
        window.location = AJS.params.baseURL + "/plugins/servlet/hazardlist";
    });
});