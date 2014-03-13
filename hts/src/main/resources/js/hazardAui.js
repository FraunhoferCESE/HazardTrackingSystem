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

    AJS.$("#hazard-save-button").click(function(e){
        var initationVal = AJS.$("#hazard-initation").val();
        var completionVal = AJS.$("#hazard-completion").val();
        var hazardTitle = AJS.$("#hazard-title").val();
        var hazardNumb = AJS.$("#hazard-number").val();
        if(hazardNumb === "") {
            AJS.$(".error").hide();
            AJS.$("#hazardNum").show();
            e.preventDefault();
        }
        else if(hazardTitle === "") {
            AJS.$(".error").hide();
            AJS.$("#hazardTitle").show();
            e.preventDefault();
        }
        else if(ValidateDate(initationVal, completionVal) == -1) {
            AJS.$(".error").hide();
            AJS.$("#incorrectInitYear").show();
            e.preventDefault();    
        }
        else if(ValidateDate(initationVal, completionVal) == -2) {
            AJS.$(".error").hide();
            AJS.$("#incorrectCompYear").show();
            e.preventDefault();    
        }
        else if(ValidateDate(initationVal, completionVal) || completionVal === "" ){
            AJS.$(".error").hide();
            AJS.$("#initError").hide();
        }
        else if(initationVal === "" && completionVal !== ""){
            AJS.$(".error").hide();
            AJS.$("#initError").show();
            e.preventDefault();     
        }
        else{
            AJS.$(".error").hide();
            AJS.$("#compError").show();
            e.preventDefault();
        }
    });
 
    
    function ValidateDate(initationVal, completionVal){
        var x = new Date(initationVal);
        var y = new Date(completionVal);
        if(x.getFullYear() < 1950) {
            return -1;
        }
        else if(y.getFullYear() < 1950){
            return -2;
        }
        return x < y;
    }

    function navigateTo(trigger, contentId){
        AJS.$("#main-nav li").removeClass("aui-nav-selected");
        AJS.$(trigger).parent().addClass("aui-nav-selected");
        AJS.$(".nav-content").hide();
        AJS.$("#" + contentId).show();
    }
});