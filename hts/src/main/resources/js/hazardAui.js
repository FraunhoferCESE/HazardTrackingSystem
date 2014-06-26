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
    
    function navigateTo(trigger, contentId){
        AJS.$("#main-nav li").removeClass("aui-nav-selected");
        AJS.$("#" + trigger).addClass("aui-nav-selected");
        AJS.$(".nav-content").hide();
        AJS.$("#" + contentId).show();
    }
});