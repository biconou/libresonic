


function onJavaJukeboxVolumeChanged() {
    var value = parseInt($("#javaJukeboxVolume").slider("option", "value"));
    onGain(value / 100);
}

function updateJavaJukeboxPlayerControlBar(playQueue){
console.log("updateJavaJukeboxPlayerControlBar");
console.dir(playQueue);
    if (playQueue != null) {
        var currentPlayQueueEntry = playQueue.entries[playQueue.index];
        if (currentPlayQueueEntry != null) {
            newSongPlaying(currentPlayQueueEntry);
        }
    }
}

songPlayingTimerId = null;

function newSongPlaying(playQueueEntry) {
    var songDuration = playQueueEntry.duration;
    var m = moment.duration(songDuration, 'seconds');
    $("#playingPositionDisplay").html(0);
    //$("#playingDurationDisplay").html(m.minutes() + ":" + m.seconds());
    $("#playingDurationDisplay").html(songDuration);

    $("#javaJukeboxSongPositionSlider").slider({max: songDuration, value: 0, animate: "fast", range: "min"});
    if (songPlayingTimerId != null) {
        clearInterval(songPlayingTimerId);
    }
    songPlayingTimerId = setInterval(songPlayingTimer, 1000);
}

function songPlayingTimer() {
    var pos = $("#javaJukeboxSongPositionSlider").slider("value");
    $("#javaJukeboxSongPositionSlider").slider("value",pos + 1);
    $("#playingPositionDisplay").html(pos + 1);
}



function initJavaJukeboxPlayerControlBar() {
    $("#javaJukeboxSongPositionSlider").slider({max: 100, value: 0, animate: "fast", range: "min"});
    $("#javaJukeboxSongPositionSlider").slider("value",0);

    $("#javaJukeboxVolumeSlider").slider({max: 100, value: 50, animate: "fast", range: "min"});
    $("#javaJukeboxVolumeSlider").on("slidestop", onJavaJukeboxVolumeChanged);

    $("#playingPositionDisplay").html("0:00");
    $("#playingDurationDisplay").html("-:--");
}


