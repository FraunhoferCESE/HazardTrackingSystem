AJS.$(function(){
    AJS.$("#content-2").hide();
    AJS.$("#content-3").hide();
    AJS.$("#content-4").hide();
    AJS.$(".error").hide();
    AJS.$("#initError").hide();
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

    AJS.$("#hazard-save-button").click(function(e){
        var initationVal = AJS.$("#hazard-initation").val();
        var completionVal = AJS.$("#hazard-completion").val();
        if(ValidateDate(initationVal, completionVal) || completionVal === "" ){
            AJS.$(".error").hide();
            AJS.$("#initError").hide();
        }
        else if(initationVal === "" && completionVal !== ""){
            AJS.$("#initError").show();
            AJS.$(".error").hide();
            e.preventDefault();     
        }
        else{
            AJS.$("#initError").hide();
            AJS.$(".error").show();
            e.preventDefault();
        }
    });

    function ValidateDate(initationVal, completionVal){
        var x = new Date(initationVal);
        var y = new Date(completionVal);
        return x < y;
    }

    AJS.$('table tr td:nth-child('+3+')');

    function navigateTo(trigger, contentId){
        AJS.$("#main-nav li").removeClass("aui-nav-selected");
        AJS.$(trigger).parent().addClass("aui-nav-selected");
        AJS.$(".nav-content").hide();
        AJS.$("#" + contentId).show();
    }
});