
function onJavaJukeboxVolumeChanged() {
    var value = parseInt($("#javaJukeboxVolume").slider("option", "value"));
    onGain(value / 100);
}

function updateJavaJukeboxPlayerControlBar(playQueue){
    console.log("updateJavaJukeboxPlayerControlBar");
    console.dir(playQueue);
    $("#playingPositionDisplay").html(playQueue.startPlayerAtPosition);
    $("#playingDurationDisplay").html(playQueue.entries[0].durationAsString);
}

function initJavaJukeboxPlayerControlBar() {
    $("#javaJukeboxSongPositionSlider").slider({max: 100, value: 50, animate: "fast", range: "min"});

    $("#javaJukeboxVolumeSlider").slider({max: 100, value: 50, animate: "fast", range: "min"});
    $("#javaJukeboxVolumeSlider").on("slidestop", onJavaJukeboxVolumeChanged);

    $("#playingPositionDisplay").html("0:00");
    $("#playingDurationDisplay").html("-:--");

    $("#javaJukeboxSongPositionSlider").slider("value",0);
}


