


function onJavaJukeboxVolumeChanged() {
    var value = $("#javaJukeboxVolumeSlider").slider("value");
    var gain = value / 100;
    playQueueService.setGain(gain);
}

function onJavaJukeboxPositionChanged() {
    var pos = $("#javaJukeboxSongPositionSlider").slider("value");
    playQueueService.setJavaJukeboxPosition(pos);
}

function updateJavaJukeboxPlayerControlBar(playQueue){
console.log("updateJavaJukeboxPlayerControlBar");
//console.dir(playQueue);
    if (playQueue != null) {
        var currentPlayQueueEntry = playQueue.entries[playQueue.index];
        if (currentPlayQueueEntry != null) {
            newSongPlaying(currentPlayQueueEntry);
        }
    }
}

function songTimeAsString(timeInSeconds) {
    var m = moment.duration(timeInSeconds, 'seconds');
    var seconds = m.seconds();
    var secondsAsString = seconds;
    if (seconds < 10) {
        secondsAsString = "0" + seconds;
    }
    return m.minutes() + ":" + secondsAsString;
}



songPlayingTimerId = null;

function newSongPlaying(playQueueEntry) {
console.log("newSongPlaying");
    $("#playingDurationDisplay").html(songTimeAsString(playQueueEntry.duration));
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
    $("#playingPositionDisplay").html(songTimeAsString(pos + 1));
}



function initJavaJukeboxPlayerControlBar() {
    $("#javaJukeboxSongPositionSlider").slider({max: 100, value: 0, animate: "fast", range: "min"});
    $("#javaJukeboxSongPositionSlider").slider("value",0);
    $("#javaJukeboxSongPositionSlider").on("slidestop", onJavaJukeboxPositionChanged);

    $("#javaJukeboxVolumeSlider").slider({max: 100, value: 50, animate: "fast", range: "min"});
    $("#javaJukeboxVolumeSlider").on("slidestop", onJavaJukeboxVolumeChanged);

    $("#playingPositionDisplay").html("0:00");
    $("#playingDurationDisplay").html("-:--");
}


