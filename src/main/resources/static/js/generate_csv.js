$(document).ready(
    function () {
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
                    alert(response);
                }).fail(function(jqxhr,textStatus,errorThrown) {
                    alert("Fallo en la peticion")
                    console.log(jqxhr);
                    console.log(textStatus);
                    console.log(errorThrown);
                });
            }
        );
    }
);