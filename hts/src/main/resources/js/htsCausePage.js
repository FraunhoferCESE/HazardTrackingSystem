AJS.$(document).ready(function(){
	AJS.$('.causeFormList ul').hide();

	AJS.$('.causeFormHeading').click(function() {
	    AJS.$(this).find('ul').slideToggle();
	});
});