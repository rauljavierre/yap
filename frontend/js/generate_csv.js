$(document).ready(
    function () {
        document.getElementById('inputFile').onchange = function () {
            document.getElementById("labelFile").innerHTML = this.value.substring(this.value.lastIndexOf("\\") + 1, this.value.length);
        };
        $("#csv-file").submit(
            function (event) {
                event.preventDefault();
                const formData = new FormData(document.getElementById("csv-file"));
                $.ajax({
                    data: formData,
                    dataType: "text",
                    type: "POST",
                    url: "/csv-file",
                    contentType: false,
                    processData: false
                }).done(function(response) {
                    const hrefContent = "data:text/plain;charset=utf-8," + encodeURIComponent(response);
                    const filename = "shortener.csv";
                    $("#csvResult").html(
                        "<div class='alert alert-danger lead'><a href='"
                        + hrefContent
                        + "' title='Download CSV file' download='"
                        + filename
                        + "'>Download CSV file</a></div>");
                }).fail(function() {
                    $("#csvResult").html(
                            "<div class='alert alert-danger lead' style=\"font-family: 'Open Sans',serif\">The file can't be empty &#128532;</div>");
                });
            }
        );
    }
);
