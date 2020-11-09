$(document).ready(
    function () {
        var urlShort;
        $("#shortener").submit(
            function (event) {
                event.preventDefault();
                $.ajax({
                    type: "POST",
                    url: "/link",
                    data: $(this).serialize(),
                    success: function (msg) {
                        $("#result").html(
                            "<div id=\"shortUrl\" class='alert alert-danger lead' style=\"font-family: 'Open Sans'\"><a target='_blank' href='"
                            + "/r/"
                            + msg
                            + "'>"
                            + window.location.href.replace('http://','')
                            + "r/"
                            + msg
                            + "</a>" +
                            " </div>");
                        $("#copy-to-clipboard").html(
                            "<button type=\"button\" class=\"btn btn-danger\" onclick=\"copyToClipboard()\">Copy to clipboard &#128221;</button>");
                        // Mostramos el boton
                        var qrButton = document.getElementById("qrCode");
                        if (qrButton.style.display === "none") {
                            qrButton.style.display = "block";
                        }
                        urlShort = $('#shortUrl').text();
                        urlShort = encodeURIComponent(urlShort);
                    },
                    error: function () {
                        $("#result").html(
                            "<div class='alert alert-danger lead' style=\"font-family: 'Open Sans'\">Try with another URL... &#128532;</div>");
                        $("#copy-to-clipboard").html("");
                    }
                });
            }
        );

        $("#qrGenerate").click(
            function () {
                $.ajax({
                    type: "POST",
                    url: "/qr",
                    data: { url: urlShort },
                    success: function(response) {
                        $('#qrImage').html('<img src="data:image/png;base64,'+response+'"/>');
                        $("#text1").html("<p>Prueba Codigo QR</p>");
                    },
                    error: function () {
                      $("#qrImage").html(
                          "<div class='alert alert-danger lead' style=\"font-family: 'Open Sans'\">Imposible generar QR +;</div>");
                      $("#text1").html("<p>" + urlShort + "</p>");
                    }
                });
            }
        );
    }
);