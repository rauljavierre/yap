$(document).ready(get_info);

function get_info() {
    $.ajax({
        type: "GET",
        url: "http://yapsh.tk/actuator/info",
        success: function (json) {
            $('#host_information_urls_qrs_csvs').html(
                '<td>' + json.URLs +
                '<td>' + json.QRs +
                '<td>' + json.CSVs
            );
            setTimeout(get_info, 1000);
        },
        error: function () {
            alert("Error polling host information")
        }
    });
}