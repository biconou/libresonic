/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */
package org.libresonic.player.service;

import com.github.biconou.AudioPlayer.JavaPlayer;
import com.github.biconou.AudioPlayer.PlayerListener;
import org.libresonic.player.Logger;
import org.libresonic.player.domain.*;
import org.libresonic.player.util.FileUtil;

import java.io.File;
import java.util.List;

/**
 *
 *
 * @author Rémi Cocula
 */
public class JukeboxService  {

    private static final Logger LOG = Logger.getLogger(JukeboxService.class);

    // TODO what to do with that ?
    private AudioScrobblerService audioScrobblerService;
    private StatusService statusService;
    private SettingsService settingsService;
    private SecurityService securityService;

    com.github.biconou.AudioPlayer.Player audioPlayer = null;
    private MediaFile currentPlayingFile;
    private TransferStatus status;


    private float gain = 0.5f;
    //private int offset;
    private MediaFileService mediaFileService;

    /* (non-Javadoc)
     * @see net.sourceforge.subsonic.service.IJukeboxService#updateJukebox(net.sourceforge.subsonic.domain.Player, int)
     */
    public synchronized void updateJukebox(Player libresonicPlayer, int offset) throws Exception {

        if (audioPlayer == null) {
            audioPlayer = new JavaPlayer();
            audioPlayer.registerListener(new PlayerListener() {
                @Override
                public void onBegin(int index, File currentFile) {
                    currentPlayingFile = libresonicPlayer.getPlayQueue().getCurrentFile();
                    onSongStart(libresonicPlayer,currentPlayingFile);
                }

                @Override
                public void onEnd(int index, File file) {
                    onSongEnd(libresonicPlayer,currentPlayingFile);
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
                    //TODO set position
                    //cmusDriver.setPosition(offset);
                    //CMusStatus status = cmusDriver.status();
                    //String pos = status.getPosition();
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



    /* (non-Javadoc)
     * @see net.sourceforge.subsonic.service.IJukeboxService#getGain()
     */
    public synchronized float getGain() {
        return gain;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.subsonic.service.IJukeboxService#getPosition()
     */
    public synchronized int getPosition() {
        // TODO do something ?
        return 0;
        // return audioPlayer == null ? 0 : offset + audioPlayer.getPosition();
    }

    private void onSongStart(Player player,MediaFile file) {
        LOG.info("[onSongStart] " + player.getUsername() + " starting jukebox for \"" + FileUtil.getShortPath(file.getFile()) + "\"");
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
        }
        scrobble(player,file, true);
    }

    private void scrobble(Player player,MediaFile file, boolean submission) {
        if (player.getClientId() == null) {  // Don't scrobble REST players.
            audioScrobblerService.register(file, player.getUsername(), submission, null);
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.subsonic.service.IJukeboxService#setGain(float)
     */
    public synchronized void setGain(float gain) {

        this.gain = gain;
    }


    /* (non-Javadoc)
     * @see net.sourceforge.subsonic.service.IJukeboxService#setAudioScrobblerService(net.sourceforge.subsonic.service.AudioScrobblerService)
     */
    public void setAudioScrobblerService(AudioScrobblerService audioScrobblerService) {
        this.audioScrobblerService = audioScrobblerService;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.subsonic.service.IJukeboxService#setStatusService(net.sourceforge.subsonic.service.StatusService)
     */
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.subsonic.service.IJukeboxService#setSettingsService(net.sourceforge.subsonic.service.SettingsService)
     */
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.subsonic.service.IJukeboxService#setSecurityService(net.sourceforge.subsonic.service.SecurityService)
     */
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.subsonic.service.IJukeboxService#setMediaFileService(net.sourceforge.subsonic.service.MediaFileService)
     */
    public void setMediaFileService(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    /**
     * Returns the player which currently uses the jukebox.
     *
     * @return The player, may be {@code null}.
     */
    public Player getPlayer() {
        throw new UnsupportedOperationException();
    }

}
