$(document).ready(
    function () {
        document.getElementById('inputFile').onchange = function () {
            var fileName = this.value.substring(this.value.lastIndexOf("\\")+1,this.value.length);
            document.getElementById("labelFile").innerHTML = fileName;
        };
        $("#csv-file").submit(
            function (event) {
                event.preventDefault();
                var formData = new FormData(document.getElementById("csv-file"));
                $.ajax({
                    data: formData,
                    dataType: "text",
                    type: "POST",
                    url: "/csv-file",
                    contentType: false,
                    processData: false
                }).done(function(response) {
                    var hrefContent = "data:text/plain;charset=utf-8," + encodeURIComponent(response);
                    var filename = "shortener.csv";
                    $("#csvResult").html(
                        "<div class='alert alert-danger lead'><a href='"
                        + hrefContent
                        + "' title='Download CSV file' download='"
                        + filename
                        + "'>Download CSV file</a></div>");
                }).fail(function(jqxhr,textStatus,errorThrown) {
                    $("#csvResult").html(
                            "<div class='alert alert-danger lead' style=\"font-family: 'Open Sans'\">The file can't be empty &#128532;</div>");
                });
            }
        );
    }
);
