AJS.$(document).ready(function() {
	var $ = AJS.$;

	function confirmation() {
		var defer = $.Deferred();
		var dialog = new AJS.Dialog({
			width: 500,
			height: 190,
			id: "deleteDialog",
		});
		
		dialog.show();
		dialog.addHeader("Confirm");
		dialog.addPanel("Panel 1", "<p class='dialog-panel-body'>Deleting will premanently remove this hazard report from JIRA. Do you want to continue?</p>", "panel-body");
		dialog.get("panel:0").setPadding(0);
		
		dialog.addButton("Continue", function(dialog) {
			dialog.hide();
			defer.resolve("true");
		});

		dialog.addLink("Cancel", function(dialog) {
			dialog.hide();
		}, "#");

		return defer.promise();
	}

	$(".deleteHazardReport").click(function() {
		var self = $(this);
		confirmation().then(function(ans) {
			if(ans){
				$.ajax({
					type: "delete",
					url: "hazardlist?key=" + self.data("key"),
					success: function(data) {
						self.parent().parent().parent().parent().remove();
						if($("#hazardTable tr").length === 1) {
							location.reload();
						}
					},
					error: function() {
						console.log('error', arguments);
					}
				});
			}.0

			return false;
		});
	});


});