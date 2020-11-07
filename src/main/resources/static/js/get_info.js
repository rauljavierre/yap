$(document).ready(get_info);

function get_info() {
    $.ajax({
        type: "GET",
        url: "/get_info",
        success: function (json) {
            $('#host_information_urls_qrs_csvs').html(
                '<td>' + json.NumberOfGeneratedURLs +
                '<td>' + json.NumberOfGeneratedQRs +
                '<td>' + json.NumberOfGeneratedCSVs
            );
            $('#host_information_memory').html(
                '<td>' + json.UsedMemory +
                '<td>' + json.AvailableMemory +
                '<td>' + json.TotalMemory
            );
            $('#host_information_cpu').html(
                '<td>' + json.UsageOfCPU +
                '<td>' + json.NumberOfCores +
                '<td>' + json.CPUFrequency
            );
            $('#host_information_platform_boottime').html(
                '<td>' + json.Platform +
                '<td>' + json.BootTime
            );
            setTimeout(get_info, 1000);
        },
        error: function () {
            alert("Error polling host information")
        }
    });
}
