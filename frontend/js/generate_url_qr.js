$(document).ready(
    function () {

        $("#shortener").submit(
            function (event) {
                event.preventDefault();
                $("#qrImage").html("");
                $("#result").html("");

                const generateQRisChecked = document.getElementById("generateQR").checked
                const requestData = {
                    url: document.getElementById("url").value,
                    generateQR: generateQRisChecked
                }

                $.ajax({
                    type: "POST",
                    url: "http://yapsh.tk/link",
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
                        $("#qrImage").html("");
                    }
                });
            }
        );

        function generateQR (urlShort, attempts) {
            if (attempts > 0) {
                const url ="http://yapsh.tk/qr/" + urlShort.split("http://yapsh.tk/")[1]
                if (ImageExist(url)) {
                    $("#qrImage").html("<img src=\"" + url + "\" width=\"200em\">");
                }
                else {
                    setTimeout(generateQR, 2000/attempts, urlShort, attempts - 1);
                }
            }
            else {
                $("#result").html(
                    "<div class='alert alert-danger lead' style=\"font-family: 'Open Sans',serif\">Try with another URL... &#128532;</div>");
                $("#qrImage").html("");
            }
        }

        // https://stackoverflow.com/questions/31936444/how-to-check-image-url-is-404-using-javascript
        function ImageExist(url) {
            const http = new XMLHttpRequest();
            http.open('GET', url, false);
            http.send();
            return http.status!==404;
        }
    }
);
