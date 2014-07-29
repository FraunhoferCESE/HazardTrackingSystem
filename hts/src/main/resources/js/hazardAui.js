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
        if(AJS.$("#payloadNavigationList option").length <= 1) {
            AJS.$(missionList).each(function() {
                if (this.payloadID === currentHazardPayloadID) {
                    temp += "<option value=" + this.payloadID + " selected>" + this.title + "</option>";
                }
                else {
                    temp += "<option value=" + this.payloadID + ">" + this.title + "</option>";
                }
            });
            AJS.$("#payloadNavigationList").append(temp);
        }
    }
    return missionList.length;
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
                temp += "<option value=" + this.hazardID + " selected>" + this.hazardNumber + " - " + this.title + "</option>";
            }
            else {
                temp += "<option value=" + this.hazardID + ">" + this.hazardNumber + " - " + this.title + "</option>";
            }
        });
        AJS.$("#hazardNavigationList").append(temp);
    }
    else {
        AJS.$("#hazardNavigationList").append("<option value=''>-Select Hazard Report-</option>");
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

function getPayloadCookie() {
    var numberOfCreatedPayloadsStr = AJS.Cookie.read("NUMBER_OF_PAYLOADS");
    var numberOfCreatedPayloads = parseInt(numberOfCreatedPayloadsStr, 10);
    if (numberOfCreatedPayloads === -1) {
        console.log("parse error");
        return null;
    }
    return numberOfCreatedPayloads;
}

AJS.$(document).ready(function(){
    AJS.$(".aui-page-header").css({"padding-top":"5px", "padding-bottom":"5px"});
    //AJS.$(".getReports").css({"padding-top":"3px", "padding-bottom":"3px"});
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
    /* COMMENT IN WHEN THESE PATHS HAVE BEEN ADDED AND REMOVE THE CLICK EVENTS BELOW
    else if(window.location.href.indexOf("verificationform") > -1) {
        navigateTo("verification-nav-item", "content-4");
    }*/

    AJS.$("#verification-nav-item").click(function(e){
        navigateTo("verification-nav-item", "content-4");
    });

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
    if (AJS.$.url().data.seg.path.length === 4) {
        whichPage = AJS.$.url().data.seg.path[3];
    }
    else {
        whichPage = AJS.$.url().data.seg.path[2];
    }

    var currentHazardPayloadID;
    var numberOfPayloadsFetched;
    var currentHazardID;
    var numberOfHazardsFetched;
    if (whichPage === "hazardlist" && AJS.$.url().param("edit") === "y") {
        var currentHazardPayloadIDStr = AJS.$("#hazardPayloadID")[0].value;
        currentHazardPayloadID = parseInt(currentHazardPayloadIDStr, 10);
        var currentHazardIDStr = AJS.$("#hazardID")[0].value;
        currentHazardID = parseInt(currentHazardIDStr, 10);
        numberOfPayloadsFetched = initializePayloadDropDownSelection(currentHazardPayloadID);
        numberOfHazardsFetched = initializeHazardDropDownSelection(currentHazardPayloadID, currentHazardID);
    }
    if (whichPage === "hazardform") {
        numberOfPayloadsFetched = initializePayloadDropDownSelection(null);
        initializeHazardDropDownSelectionToEmpty(false);
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

    AJS.$("#createNewHazardReport").live("click", function() {
        // if (whichPage === "hazardlist") {
        //     var parameters = AJS.$.url().param();
        //     if (AJS.$.isEmptyObject(parameters)) {
        //         setPayloadCookie(AJS.$(".getReports").length);
        //     }
        // }

        if (getPayloadCookie() > 0) {
            AJS.$(this).attr("href", AJS.params.baseURL + "/plugins/servlet/hazardform");
        }
        else {
            JIRA.Messages.showWarningMsg("Please create a Mission/Payload before creating a Hazard Report.", {closeable: true});
        }
    });

    function navigateTo(trigger, contentId){
        AJS.$("#main-nav li").removeClass("aui-nav-selected");
        AJS.$("#" + trigger).addClass("aui-nav-selected");
        AJS.$(".nav-content").hide();
        AJS.$("#" + contentId).show();
    }
});