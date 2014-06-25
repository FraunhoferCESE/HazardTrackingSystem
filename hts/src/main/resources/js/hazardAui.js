AJS.$(document).ready(function(){
    AJS.$(".error").hide();
    if(window.location.href.indexOf("hazardform") > -1 || window.location.href.indexOf("hazardlist") > -1) {
        AJS.$("#content-2").hide();
        AJS.$("#content-3").hide();
        AJS.$("#content-4").hide();
    }
    else if(window.location.href.indexOf("causeform") > -1) {
        AJS.$("#content-1").hide();
        AJS.$("#content-3").hide();
        AJS.$("#content-4").hide();
        AJS.$("#hazard-nav-item").removeClass("aui-nav-selected");
        AJS.$("#cause-nav-item").addClass("aui-nav-selected");
    }
    else if(window.location.href.indexOf("controlform") > -1) {
        AJS.$("#content-1").hide();
        AJS.$("#content-2").hide();
        AJS.$("#content-4").hide();
        AJS.$("#hazard-nav-item").removeClass("aui-nav-selected");
        AJS.$("#control-nav-item").addClass("aui-nav-selected");
    }

    AJS.$("#hazard-nav-item").click(function(e){
        navigateTo(e.target, "content-1");
    });
    AJS.$("#cause-nav-item").click(function(e){
        navigateTo(e.target, "content-2");
    });
    AJS.$("#control-nav-item").click(function(e){
        navigateTo(e.target, "content-3");
    });
    AJS.$("#verification-nav-item").click(function(e){
        navigateTo(e.target, "content-4");
    });

    if(window.location.href.indexOf("hazardform") > -1) {
        AJS.$("#CreateHazardNav").addClass("aui-nav-selected");
        AJS.$("#ViewAllNav").removeClass("aui-nav-selected");
    }
    else if(window.location.href.indexOf("?edit=y") > -1) {
        AJS.$("#ViewAllNav").removeClass("aui-nav-selected");
        AJS.$("#CreateHazardNav").removeClass("aui-nav-selected");
    }


    function navigateTo(trigger, contentId){
        AJS.$("#main-nav li").removeClass("aui-nav-selected");
        AJS.$(trigger).parent().addClass("aui-nav-selected");
        AJS.$(".nav-content").hide();
        AJS.$("#" + contentId).show();
    }
});