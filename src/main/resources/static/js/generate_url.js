$(document).ready(
    function () {
        $("#shortener").submit(
            function (event) {
                event.preventDefault();
                $.ajax({
                    type: "POST",
                    url: "/link",
                    data: $(this).serialize(),
                    success: function (msg) {
                        $("#result").html(
                            "<div class='alert alert-danger lead' style=\"font-family: 'Open Sans'\"><a target='_blank' href='"
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
                    },
                    error: function () {
                        $("#result").html(
                            "<div class='alert alert-danger lead' style=\"font-family: 'Open Sans'\">Try with another URL... &#128532;</div>");
                        $("#copy-to-clipboard").html("");
                    }
                });
            }
        );
    }
);