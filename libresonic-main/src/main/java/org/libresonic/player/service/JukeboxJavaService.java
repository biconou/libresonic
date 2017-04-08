package org.libresonic.player.service;

import com.github.biconou.AudioPlayer.JavaPlayer;
import com.github.biconou.AudioPlayer.api.PlayerListener;
import org.libresonic.player.domain.*;
import org.libresonic.player.util.FileUtil;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 *
 *
 * @author Rémi Cocula
 */
public class JukeboxJavaService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(JukeboxJavaService.class);

    private AudioScrobblerService audioScrobblerService;
    private StatusService statusService;
    private SettingsService settingsService;
    private SecurityService securityService;

    com.github.biconou.AudioPlayer.api.Player audioPlayer = null;
    private MediaFile currentPlayingFile;
    private TransferStatus status;


    private MediaFileService mediaFileService;


    public synchronized void updateJukebox(Player libresonicPlayer, int offset) throws Exception {

        log.debug("begin updateJukebox");

        if (audioPlayer == null) {
            initAudioPlayer(libresonicPlayer);
        }

        // Control user authorizations
        User user = securityService.getUserByName(libresonicPlayer.getUsername());
        if (!user.isJukeboxRole()) {
            log.warn("{} is not authorized for jukebox playback.",user.getUsername());
            return;
        }

        log.debug("PlayQueue.Status is {}",libresonicPlayer.getPlayQueue().getStatus());
        if (libresonicPlayer.getPlayQueue().getStatus() == PlayQueue.Status.PLAYING) {
            MediaFile currentFileInPlayQueue;
            synchronized (libresonicPlayer.getPlayQueue()) {
                currentFileInPlayQueue = libresonicPlayer.getPlayQueue().getCurrentFile();
            }
            log.debug("Current file in play queue is {}",currentFileInPlayQueue.getName());

            boolean sameFile = currentFileInPlayQueue != null && currentFileInPlayQueue.equals(currentPlayingFile);
            boolean paused = audioPlayer.isPaused();

            if (sameFile && paused) {
                log.debug("Same file and paused -> try to resume playing");
                audioPlayer.play();
            } else {
                if (sameFile) {
                    log.debug("Same file and offset={} -> try to move to this position",offset);
                    audioPlayer.setPos(offset);
                } else {
                    log.debug("Different file to play -> start a new play list");
                    if (currentFileInPlayQueue != null) {
                        audioPlayer.setPlayList(libresonicPlayer.getPlayQueue());
                        audioPlayer.play();
                    }
                }
            }
        } else {
            try {
                log.debug("try to pause player");
                audioPlayer.pause();
            } catch (Exception e) {
                log.error("Error trying to pause",e);
                throw e;
            }
        }
    }

    private void initAudioPlayer(final Player libresonicPlayer) {
        audioPlayer = new JavaPlayer();
        audioPlayer.registerListener(new PlayerListener() {
            @Override
            public void onBegin(int index, File currentFile) {
                currentPlayingFile = libresonicPlayer.getPlayQueue().getCurrentFile();
                onSongStart(libresonicPlayer, currentPlayingFile);
            }

            @Override
            public void onEnd(int index, File file) {
                onSongEnd(libresonicPlayer, currentPlayingFile);
            }

            @Override
            public void onFinished() {
                // Nothing to do here
            }

            @Override
            public void onStop() {
                // Nothing to do here
            }

            @Override
            public void onPause() {
                // Nothing to do here
            }

        });
        log.debug("New audio player {} has been initialized.",audioPlayer.toString());
    }


    public synchronized int getPosition() {
        if (audioPlayer == null) {
            return 0;
        } else {
            return audioPlayer.getPlayingInfos().currentAudioPositionInSeconds();
        }
    }

    public void setPosition(int positionInSeconds) {
        if (audioPlayer != null) {
            audioPlayer.setPos(positionInSeconds);
        }
    }


    private void onSongStart(Player player,MediaFile file) {
        log.info("[onSongStart] {} starting jukebox for \"{}\"",player.getUsername(),FileUtil.getShortPath(file.getFile()));
        if (status != null) {
            statusService.removeStreamStatus(status);
            status = null;
        }
        status = statusService.createStreamStatus(player);
        status.setFile(file.getFile());
        status.addBytesTransfered(file.getFileSize());
        mediaFileService.incrementPlayCount(file);
        scrobble(player,file, false);
    }

    private void onSongEnd(Player player,MediaFile file) {
        log.info("[onSongEnd] {} stopping jukebox for \"{}\"",player.getUsername(),FileUtil.getShortPath(file.getFile()));
        if (status != null) {
            statusService.removeStreamStatus(status);
            status = null;
        }
        scrobble(player,file, true);
    }

    private void scrobble(Player player,MediaFile file, boolean submission) {
        if (player.getClientId() == null) {  // Don't scrobble REST players.
            audioScrobblerService.register(file, player.getUsername(), submission, null);
        }
    }

    public float getGain() {
        if (audioPlayer != null) {
            return audioPlayer.getGain();
        }
        return 0.5f;
    }

    public synchronized void setGain(float gain) {
        log.debug("setGain : gain={}",gain);
        if (audioPlayer != null) {
            audioPlayer.setGain(gain);
        }
    }


    public void setAudioScrobblerService(AudioScrobblerService audioScrobblerService) {
        this.audioScrobblerService = audioScrobblerService;
    }

    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

}
