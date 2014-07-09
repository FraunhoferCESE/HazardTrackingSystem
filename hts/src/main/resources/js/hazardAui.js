AJS.$(document).ready(function(){
    AJS.$(".error").hide();   
    if(window.location.href.indexOf("hazardform") > -1 || window.location.href.indexOf("hazardlist") > -1) {
        navigateTo("hazard-nav-item", "content-1");
    }
    else if(window.location.href.indexOf("causeform") > -1) {
        navigateTo("cause-nav-item", "content-2");
    }
    /* COMMENT IN WHEN THESE PATHS HAVE BEEN ADDED AND REMOVE THE CLICK EVENTS BELOW
    else if(window.location.href.indexOf("controlform") > -1) {
        navigateTo("control-nav-item", "content-3");
    }
    else if(window.location.href.indexOf("verificationform") > -1) {
        navigateTo("verification-nav-item", "content-4");
    }*/

    AJS.$("#control-nav-item").click(function(e){
        navigateTo("control-nav-item", "content-3");
    });
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

    AJS.$("#payloadNavigationList").live("mousedown", function() {
        console.log("PAYLOAD OPEN");
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
            console.log("payload list length " + AJS.$("#payloadNavigationList option").length);
            if(AJS.$("#payloadNavigationList option").length <= 1) {
                AJS.$(missionList).each(function() {
                    temp += "<option value=" + this.payloadID + ">" + this.title + "</option>";
                });
                AJS.$("#payloadNavigationList").append(temp);
            }
        }
    });

    AJS.$("#payloadNavigationList").live("change", function() {
        AJS.$("span#hazardReportsNavigation").children().remove();
        var value = AJS.$(this).val();
        var hazardList;
        if(value.length){
            AJS.$.ajax({
                type:"GET",
                async: false,
                url: AJS.params.baseURL + "/rest/htsrest/1.0/report/allpayloads/" + value,
                success: function(data) {
                    hazardList = data;
                }
            });
            console.log(hazardList.length);
            if(hazardList.length > 0) {
                var temp = "<select size='1' class='select' name='hazardNavigationList' id='hazardNavigationList'><option value=''>-Select Hazard Report-</option>";
                AJS.$(hazardList).each(function() {
                    temp += "<option value=" + this.hazardID + ">" + this.hazardNumber + " - " + this.title + "</option>";
                });
                temp += "</select><a href='#' style='margin-left:4px' class='aui-button' id='navigateToHazard'>GO</a>";
            }
            else {
                var temp = "<select size='1' class='select long-field' style='width:290px' name='hazardNavigationList' id='hazardNavigationList'><option value=''>-No Hazard Reports have been created-</option></select>";
            }

            AJS.$("span#hazardReportsNavigation").append(temp);
        }
    });

    AJS.$("#hazardNavigationList").live("change", function() {
        var value = AJS.$(this).val();
        if(value.length) {
            AJS.$("#navigateToHazard").attr("href", AJS.params.baseURL + "/plugins/servlet/hazardlist?edit=y&key=" + value);
        }
    });
    
    function navigateTo(trigger, contentId){
        AJS.$("#main-nav li").removeClass("aui-nav-selected");
        AJS.$("#" + trigger).addClass("aui-nav-selected");
        AJS.$(".nav-content").hide();
        AJS.$("#" + contentId).show();
    }
});