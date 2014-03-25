AJS.$(document).ready(function(){
    AJS.$("#content-2").hide();
    AJS.$("#content-3").hide();
    AJS.$("#content-4").hide();
    AJS.$(".error").hide();

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
    
    var lastEditColumn = AJS.$('table#hazard-table tbody td:nth-child(3)');
    lastEditColumn.each(function () { AJS.$(this)[0].innerText = Date.parse(AJS.$(this)[0].innerText.substring(0,19)).toString("MMMM dd, yyyy, HH:mm") });
    
    function navigateTo(trigger, contentId){
        AJS.$("#main-nav li").removeClass("aui-nav-selected");
        AJS.$(trigger).parent().addClass("aui-nav-selected");
        AJS.$(".nav-content").hide();
        AJS.$("#" + contentId).show();
    }
});