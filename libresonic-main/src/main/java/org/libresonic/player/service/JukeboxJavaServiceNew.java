/*
 This file is part of Libresonic.

 Libresonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Libresonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Libresonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2016 (C) Libresonic Authors
 Based upon Subsonic, Copyright 2009 (C) Sindre Mehus
 */
package org.libresonic.player.service;

import com.github.biconou.AudioPlayer.JavaPlayer;
import com.github.biconou.AudioPlayer.api.PlayerListener;
import org.apache.commons.io.IOUtils;
import org.libresonic.player.Logger;
import org.libresonic.player.domain.*;
import org.libresonic.player.service.jukebox.AudioPlayer;
import org.libresonic.player.util.FileUtil;

import java.io.File;
import java.io.InputStream;

import static org.libresonic.player.service.jukebox.AudioPlayer.State.EOM;

/**
 * Plays music on the local audio device.
 *
 * @author Sindre Mehus
 */
public class JukeboxJavaServiceNew implements AudioPlayer.Listener {

    private static final Logger LOG = Logger.getLogger(JukeboxService.class);

    com.github.biconou.AudioPlayer.api.Player audioPlayer = null;

    private TranscodingService transcodingService;
    private AudioScrobblerService audioScrobblerService;
    private StatusService statusService;
    private SettingsService settingsService;
    private SecurityService securityService;

    private Player player;
    private TransferStatus status;
    private MediaFile currentPlayingFile;
    private float gain = 0.5f;
    private int offset;
    private MediaFileService mediaFileService;

    /**
     * Updates the jukebox by starting or pausing playback on the local audio device.
     *
     * @param libresonicPlayer The player in question.
     * @param offset Start playing after this many seconds into the track.
     */
    public synchronized void updateJukebox(Player libresonicPlayer, int offset) throws Exception {
        User user = securityService.getUserByName(libresonicPlayer.getUsername());
        if (!user.isJukeboxRole()) {
            LOG.warn(user.getUsername() + " is not authorized for jukebox playback.");
            return;
        }

        if (audioPlayer == null) {
            initAudioPlayer(libresonicPlayer);
        }

        if (libresonicPlayer.getPlayQueue().getStatus() == PlayQueue.Status.PLAYING) {
            this.player = libresonicPlayer;
            MediaFile result;
            synchronized (libresonicPlayer.getPlayQueue()) {
                result = libresonicPlayer.getPlayQueue().getCurrentFile();
            }
            play(result, offset);
        } else {
            if (audioPlayer != null) {
                audioPlayer.pause();
            }
        }
    }

    private void initAudioPlayer(final Player libresonicPlayer) {
        audioPlayer = new JavaPlayer();
        audioPlayer.registerListener(new PlayerListener() {
            @Override
            public void onBegin(int index, File currentFile) {
                currentPlayingFile = libresonicPlayer.getPlayQueue().getCurrentFile();
                onSongStart(currentPlayingFile);
            }

            @Override
            public void onEnd(int index, File file) {
                onSongEnd(currentPlayingFile);
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

    private synchronized void play(MediaFile file, int offset) {
        InputStream in = null;

        try {

            // Resume if possible.
            boolean sameFile = file != null && file.equals(currentPlayingFile);
            boolean paused = audioPlayer != null && audioPlayer.isPaused();
            if (sameFile && paused && offset == 0) {
                audioPlayer.play();
            } else {
                this.offset = offset;
                if (audioPlayer != null) {
                    audioPlayer.close();
                    if (currentPlayingFile != null) {
                        onSongEnd(currentPlayingFile);
                    }
                }

                if (file != null) {
                    int duration = file.getDurationSeconds() == null ? 0 : file.getDurationSeconds() - offset;
                    TranscodingService.Parameters parameters = new TranscodingService.Parameters(file, new VideoTranscodingSettings(0, 0, offset, duration, false));
                    String command = settingsService.getJukeboxCommand();
                    parameters.setTranscoding(new Transcoding(null, null, null, null, command, null, null, false));
                    in = transcodingService.getTranscodedInputStream(parameters);
                    /*audioPlayer = new AudioPlayer(in, this);
                    audioPlayer.setGain(gain);
                    audioPlayer.play();*/
                    onSongStart(file);
                }
            }

            currentPlayingFile = file;

        } catch (Exception x) {
            LOG.error("Error in jukebox: " + x, x);
            IOUtils.closeQuietly(in);
        }
    }

    public synchronized void stateChanged(AudioPlayer audioPlayer, AudioPlayer.State state) {
        if (state == EOM) {
            player.getPlayQueue().next();
            MediaFile result;
            synchronized (player.getPlayQueue()) {
                result = player.getPlayQueue().getCurrentFile();
            }
            play(result, 0);
        }
    }

    public synchronized float getGain() {
        return gain;
    }

    public synchronized int getPosition() {
        //return audioPlayer == null ? 0 : offset + audioPlayer.getPosition();
        return 0;
    }

    /**
     * Returns the player which currently uses the jukebox.
     *
     * @return The player, may be {@code null}.
     */
    public Player getPlayer() {
        return player;
    }

    private void onSongStart(MediaFile file) {
        LOG.info(player.getUsername() + " starting jukebox for \"" + FileUtil.getShortPath(file.getFile()) + "\"");
        status = statusService.createStreamStatus(player);
        status.setFile(file.getFile());
        status.addBytesTransfered(file.getFileSize());
        mediaFileService.incrementPlayCount(file);
        scrobble(file, false);
    }

    private void onSongEnd(MediaFile file) {
        LOG.info(player.getUsername() + " stopping jukebox for \"" + FileUtil.getShortPath(file.getFile()) + "\"");
        if (status != null) {
            statusService.removeStreamStatus(status);
        }
        scrobble(file, true);
    }

    private void scrobble(MediaFile file, boolean submission) {
        if (player.getClientId() == null) {  // Don't scrobble REST players.
            audioScrobblerService.register(file, player.getUsername(), submission, null);
        }
    }

    public synchronized void setGain(float gain) {
        this.gain = gain;
        if (audioPlayer != null) {
            audioPlayer.setGain(gain);
        }
    }

    public void setTranscodingService(TranscodingService transcodingService) {
        this.transcodingService = transcodingService;
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
