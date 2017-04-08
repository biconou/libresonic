


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

function songDurationAsString(playQueueEntry) {
    var songDuration = playQueueEntry.duration;
    var m = moment.duration(songDuration, 'seconds');
    return m.minutes() + ":" + m.seconds();
}

function songPositionAsString() {
    var pos = $("#javaJukeboxSongPositionSlider").slider("value");
    var m = moment.duration(pos, 'seconds');
    return m.minutes() + ":" + m.seconds();
}


songPlayingTimerId = null;

function newSongPlaying(playQueueEntry) {
    $("#playingDurationDisplay").html(songDurationAsString(playQueueEntry));
    $("#playingPositionDisplay").html("0:00");

    var songDuration = playQueueEntry.duration;
    $("#javaJukeboxSongPositionSlider").slider({max: songDuration, value: 0, animate: "fast", range: "min"});
    if (songPlayingTimerId != null) {
        clearInterval(songPlayingTimerId);
    }
    songPlayingTimerId = setInterval(songPlayingTimer, 1000);
}

function songPlayingTimer() {
    var pos = $("#javaJukeboxSongPositionSlider").slider("value");
    $("#javaJukeboxSongPositionSlider").slider("value",pos + 1);
    $("#playingPositionDisplay").html(songPositionAsString());
}



function initJavaJukeboxPlayerControlBar() {
    $("#javaJukeboxSongPositionSlider").slider({max: 100, value: 0, animate: "fast", range: "min"});
    $("#javaJukeboxSongPositionSlider").slider("value",0);

    $("#javaJukeboxVolumeSlider").slider({max: 100, value: 50, animate: "fast", range: "min"});
    $("#javaJukeboxVolumeSlider").on("slidestop", onJavaJukeboxVolumeChanged);

    $("#playingPositionDisplay").html("0:00");
    $("#playingDurationDisplay").html("-:--");
}


