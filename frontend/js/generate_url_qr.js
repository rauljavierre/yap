$(document).ready(
    function () {

        $("#shortener").submit(
            function (event) {
                event.preventDefault();

                const generateQRisChecked = document.getElementById("generateQR").checked
                const requestData = {
                    url: document.getElementById("url").value,
                    generateQR: generateQRisChecked
                }

                $.ajax({
                    type: "POST",
                    url: "http://localhost:3001/link",
                    dataType : "json",
                    contentType: "application/json; charset=utf-8",
                    data: JSON.stringify(requestData),
                    success: function (msg) {
                        $("#result").html(
                            "<div id=\"shortUrl\" class='alert alert-danger lead' style=\"font-family: 'Open Sans',serif\"><a target='_blank' href='"
                            + msg.url
                            + "'>"
                            + msg.url
                            + "</a>" +
                            " </div>");
                        $("#copy-to-clipboard").html(
                            "<button type=\"button\" class=\"btn btn-danger\" onclick=\"copyToClipboard()\">Copy to clipboard &#128221;</button>");

                        if (generateQRisChecked) {
                            generateQR($('#shortUrl').text(), 15);  // polling: generateAndStoreQR may cost
                        }
                        else {
                            $("#qrImage").html("");
                        }
                    },
                    error: function () {
                        $("#result").html(
                            "<div class='alert alert-danger lead' style=\"font-family: 'Open Sans',serif\">Try with another URL... &#128532;</div>");
                        $("#copy-to-clipboard").html("");
                        $("#qrImage").html("");
                    }
                });
            }
        );

        function generateQR (urlShort, attempts) {
            $.ajax({
                type: "GET",
                url: "http://localhost:3001/qr/" + urlShort.split("http://localhost/")[1],
                success: function(response) {
                    $('#qrImage').html('<img src="data:image/png;base64,'+response.qr +'" width="250em"/>');
                },
                error: function () {
                    if (attempts === 0) {
                        $("#qrImage").html(
                            "<div class='alert alert-danger lead' style=\"font-family: 'Open Sans',serif\">We've found an error generating the QR... &#128532;</div>");
                    }
                    else {
                        setTimeout(generateQR, 3000/attempts, urlShort, attempts - 1);
                    }
                }
            });
        }
    }
);
