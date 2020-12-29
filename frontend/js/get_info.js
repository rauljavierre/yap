$(document).ready(get_info);

function get_info() {
    $.ajax({
        type: "GET",
        url: "http://localhost/actuator/info",
        success: function (json) {
            let urls = json.URLs == null ? 0 : json.URLs
            let qrs = json.QRs == null ? 0 : json.QRs
            let csvs = json.CSVs == null ? 0 : json.CSVs

            $('#host_information_urls_qrs_csvs').html(
                '<td>' + urls +
                '<td>' + qrs +
                '<td>' + csvs
            );
            setTimeout(get_info, 1000);
        },
        error: function () {
            alert("Error polling host information")
        }
    });
}