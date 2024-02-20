function fetchPlaylist() {
    var playlistId = document.getElementById('playlistId').value;
    var xhr = new XMLHttpRequest();
    xhr.open('GET', '/scraper/list/' + playlistId, true);
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                // Success - Display response
                document.getElementById('responseContainer').innerHTML = xhr.responseText;
            } else {
                // Error - Display error message
                document.getElementById('responseContainer').innerHTML = "Failed to fetch playlist. Status code: " + xhr.status;
            }
        }
    };
    xhr.send();
}