$(document).ready(
    function () {
        let urlShort;

        $("#shortener").submit(
            function (event) {
                event.preventDefault();
                $.ajax({
                    type: "POST",
                    url: "http://localhost:3001/link",
                    data: $(this).serialize(),
                    success: function (msg) {
                        $("#result").html(
                            "<div id=\"shortUrl\" class='alert alert-danger lead' style=\"font-family: 'Open Sans',serif\"><a target='_blank' href='"
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

                        const qrButton = document.getElementById("qrCode");
                        if (qrButton.style.display === "none") {
                            qrButton.style.display = "block";
                        }
                        urlShort = $('#shortUrl').text();
                        urlShort = encodeURIComponent(urlShort);
                    },
                    error: function () {
                        console.log("hey buenas")

                        $("#result").html(
                            "<div class='alert alert-danger lead' style=\"font-family: 'Open Sans',serif\">Try with another URL... &#128532;</div>");
                        $("#copy-to-clipboard").html("");
                        $("#qrImage").html("");
                        $("#qrCode").html("");

                        const qrButton = document.getElementById("qrCode");
                        qrButton.style.display = "none";
                    }
                });
            }
        );

        $("#qrGenerate").click(
            function () {
                $.ajax({
                    type: "GET",
                    url: "/qr",
                    data: { url: urlShort },
                    success: function(response) {
                        $('#qrImage').html('<img src="data:image/png;base64,'+response+'"/>');
                    },
                    error: function () {
                      $("#qrImage").html(
                          "<div class='alert alert-danger lead' style=\"font-family: 'Open Sans',serif\">We've found an error generating the QR... &#128532;</div>");
                      $("#text1").html("<p>" + urlShort + "</p>");
                    }
                });
            }
        );
    }
);
