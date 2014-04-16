var URL = "http://54.193.40.29:8001/library";
$(":button").click(function() {
    var isbn = this.id;
    alert('About to report lost on ISBN ' + isbn);
    var tr = $(this).closest('tr');
    var id = tr.find('#status');
    alert(id.text());
	$.ajax({
	    type: "PUT",
	    url: URL + '/v1/books/'+ isbn + '?status=lost',
	    contentType: "application/json",
	    success: function(data){
		    	alert(JSON.stringify(data)+' '+data.book.status);
			$(id).text(data.book.status);
	    }
	});
});

