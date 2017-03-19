package org.libresonic.player.service;

import com.github.biconou.AudioPlayer.JavaPlayer;
import com.github.biconou.AudioPlayer.api.PlayerListener;
import org.libresonic.player.Logger;
import org.libresonic.player.domain.*;
import org.libresonic.player.domain.Player;
import org.libresonic.player.util.FileUtil;

import java.io.File;


/**
 *
 *
 * @author Rémi Cocula
 */
public class JukeboxJavaService  {

    private static final Logger LOG = Logger.getLogger(JukeboxService.class);

    private AudioScrobblerService audioScrobblerService;
    private StatusService statusService;
    private SettingsService settingsService;
    private SecurityService securityService;

    com.github.biconou.AudioPlayer.api.Player audioPlayer = null;
    private MediaFile currentPlayingFile;
    private TransferStatus status;


    private float gain = 0.5f;
    private MediaFileService mediaFileService;


    public synchronized void updateJukebox(Player libresonicPlayer, int offset) throws Exception {

        if (audioPlayer == null) {
            initAudioPlayer(libresonicPlayer);
        }

        // Control user authorizations
        User user = securityService.getUserByName(libresonicPlayer.getUsername());
        if (!user.isJukeboxRole()) {
            LOG.warn(user.getUsername() + " is not authorized for jukebox playback.");
            return;
        }

        if (libresonicPlayer.getPlayQueue().getStatus() == PlayQueue.Status.PLAYING) {
            MediaFile currentFileInPlayQueue;
            synchronized (libresonicPlayer.getPlayQueue()) {
                currentFileInPlayQueue = libresonicPlayer.getPlayQueue().getCurrentFile();
            }

            boolean sameFile = currentFileInPlayQueue != null && currentFileInPlayQueue.equals(currentPlayingFile);
            boolean paused = audioPlayer.isPaused();

            if (sameFile && paused) {
                audioPlayer.play();
            } else {
                if (sameFile) {
                    audioPlayer.setPos(offset);
                } else {
                    if (currentFileInPlayQueue != null) {
                        audioPlayer.stop();
                        audioPlayer.setPlayList(libresonicPlayer.getPlayQueue());
                        if (!audioPlayer.isPlaying()) {
                            audioPlayer.play();
                        }
                    }
                }
            }
        } else {
            try {
                audioPlayer.pause();
            } catch (Exception e) {
                LOG.error("Error trying to pause",e);
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
    }


    public synchronized int getPosition() {
        // TODO do something ?
        return 0;
        // return audioPlayer == null ? 0 : offset + audioPlayer.getPosition();
    }

    private void onSongStart(Player player,MediaFile file) {
        LOG.info("[onSongStart] " + player.getUsername() + " starting jukebox for \"" + FileUtil.getShortPath(file.getFile()) + "\"");
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
        LOG.info("[onSongEnd] " + player.getUsername() + " stopping jukebox for \"" + FileUtil.getShortPath(file.getFile()) + "\"");
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

    public synchronized void setGain(float gain) {
        this.gain = gain;
        audioPlayer.setGain(this.gain);
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
