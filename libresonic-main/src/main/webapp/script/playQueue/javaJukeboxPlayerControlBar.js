
javaJukeboxPlayerCurrentStreamUrl = null;

function onJavaJukeboxVolumeChanged() {
    var value = $("#javaJukeboxVolumeSlider").slider("value");
    var gain = value / 100;
    playQueueService.setGain(gain);
}

function onJavaJukeboxPositionChanged() {
    var pos = $("#javaJukeboxSongPositionSlider").slider("value");
    playQueueService.setJavaJukeboxPosition(pos);
}

function updateJavaJukeboxPlayerControlBar(song){
console.log("updateJavaJukeboxPlayerControlBar");
    if (song != null) {
        var playingStream = song.streamUrl;
        if (playingStream != javaJukeboxPlayerCurrentStreamUrl) {
            javaJukeboxPlayerCurrentStreamUrl = playingStream;
            newSongPlaying(song);
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

function newSongPlaying(song) {
console.log("newSongPlaying");
    var songDuration = song.duration;
    $("#playingDurationDisplay").html(songTimeAsString(songDuration));
    $("#playingPositionDisplay").html("0:00");

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


