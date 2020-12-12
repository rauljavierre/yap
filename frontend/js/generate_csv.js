$(document).ready(
    function () {
        // Show last file selected in the input field
        document.getElementById('inputFile').onchange = function () {
            document.getElementById("labelFile").innerHTML = this.value.substring(this.value.lastIndexOf("\\") + 1, this.value.length);
        };

        // Short file
        $("#csv-file").submit(
            function (event) {
                event.preventDefault();
                let formData = new FormData(document.getElementById("csv-file"));
                let file = formData.get('file');
                let reader = new FileReader();
                // Event fired when file reading finished
                reader.addEventListener('load', function(e) {
                    let fileData = e.target.result;
                    let urlList = fileData.split(",");
                    let lastURL = urlList[urlList.length-1];
                    urlList[urlList.length-1] = lastURL.substring(0, lastURL.length-1);
                    shortFile(urlList);
                });
                reader.readAsText(file);
            }
        );
    }
);

// Generate file to download
function generateFile (data) {
    const hrefContent = "data:text/plain;charset=utf-8," + encodeURIComponent(data);
    const filename = "shortener.csv";
    $("#csvResult").html(
        "<div class='alert alert-danger lead' style='display: table; width: 30%; margin: 0 auto;'><a href='"
        + hrefContent
        + "' title='Download CSV file' download='"
        + filename
        + "'>Download CSV file</a></div>");
}

// Short a list of URLs with websockets
function shortFile(urlList) {
    let responseList = ""
    // Send each URL to the server
    let counter = 0;
    let socket = new WebSocket("ws://localhost:3001/csv");
    socket.onopen = function(e) {
        urlList.forEach(function (item) {
            socket.send(item);
        });
    };
    socket.onmessage = function(event) {
        responseList = responseList + event.data + "\n";
        counter = counter + 1;
        if (counter === urlList.length) {
            socket.close(1000, "File shortening done");
            generateFile(responseList);
        }
    };
    socket.onerror = function(error) {
        alert("Se ha producido un error.");
    };
}