AJS.$(document).ready(function() {
	var $ = AJS.$;
	dateLayout();

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
					type: "DELETE",
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
			}

			return false;
		});
	});

	$(".getReports").click(function() {
		var self = $(this);
		$.ajax({
			type: "GET",
			url: "hazardlist?key=" + self.data("key"),
			success: function(html) {
				var hazardTableHTML = $(html).find("#hazardTable");
				$("#hazardTableHolder").html(hazardTableHTML);
				dateLayout();
			}
		});
	});


	function dateLayout() {
    	//Fixing the date layout on the landing page. To change the define the new layout in the toString method.
		var lastEditColumn = $('table#hazardTable tbody td:nth-child(4)');
		if(lastEditColumn.length > 0) {
    		lastEditColumn.each(function () { $(this)[0].innerText = Date.parse($(this)[0].innerText.substring(0,19)).toString("MMMM dd, yyyy, HH:mm") });
    	}
    }

});