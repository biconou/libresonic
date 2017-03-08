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
package org.libresonic.player.controller;

import org.apache.commons.lang3.StringUtils;
import org.libresonic.player.Logger;
import org.libresonic.player.domain.MediaFile;
import org.libresonic.player.domain.MusicFolder;
import org.libresonic.player.domain.Player;
import org.libresonic.player.domain.Share;
import org.libresonic.player.service.MediaFileService;
import org.libresonic.player.service.PlayerService;
import org.libresonic.player.service.SettingsService;
import org.libresonic.player.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

import static org.libresonic.player.controller.ExternalPlayerController.SHARE_PATH;

/**
 * Controller for the page used to play shared music (Twitter, Facebook etc).
 *
 * @author Sindre Mehus
 */
@Controller
@RequestMapping(SHARE_PATH + "**")
public class ExternalPlayerController {

    private static final Logger LOG = Logger.getLogger(ExternalPlayerController.class);

    public static final String SHARE_PATH = "/share/";

    @Autowired
    private SettingsService settingsService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private ShareService shareService;
    @Autowired
    private MediaFileService mediaFileService;

    @RequestMapping(method = RequestMethod.GET)
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, Object> map = new HashMap<>();

        String pathRelativeToBase = new UrlPathHelper().getPathWithinApplication(request);

        String shareName = StringUtils.removeStart(pathRelativeToBase, SHARE_PATH);

        if(StringUtils.isBlank(shareName)) {
            LOG.warn("Could not find share with shareName " + shareName);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        Share share = shareService.getShareByName(shareName);

        if (share != null && share.getExpires() != null && share.getExpires().before(new Date())) {
            LOG.warn("Share " + shareName + " is expired");
            share = null;
        }

        if (share != null) {
            share.setLastVisited(new Date());
            share.setVisitCount(share.getVisitCount() + 1);
            shareService.updateShare(share);
        }

        Player player = playerService.getGuestPlayer(request);

        map.put("share", share);
        map.put("songs", getSongs(share, player.getUsername()));
        map.put("player", player.getId());

        return new ModelAndView("externalPlayer", "model", map);
    }

    private List<MediaFile> getSongs(Share share, String username) throws IOException {
        List<MediaFile> result = new ArrayList<MediaFile>();

        List<MusicFolder> musicFolders = settingsService.getMusicFoldersForUser(username);

        if (share != null) {
            for (MediaFile file : shareService.getSharedFiles(share.getId(), musicFolders)) {
                if (file.getFile().exists()) {
                    if (file.isDirectory()) {
                        result.addAll(mediaFileService.getChildrenOf(file, true, false, true));
                    } else {
                        result.add(file);
                    }
                }
            }
        }
        return result;
    }

}