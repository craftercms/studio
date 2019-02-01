/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.api.v2.dal;

import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlueprintDescriptor {

    public static void main(String[] args) throws FileNotFoundException {

        Yaml yaml = new Yaml();

        BlueprintDescriptor bpd = new BlueprintDescriptor();
        Blueprint bp = new Blueprint();

        Blueprint.Version version = new Blueprint.Version();
        version.setMajor(1);
        version.setMinor(0);
        version.setPatch(0);
        bp.setVersion(version);
        Blueprint.Website website = new Blueprint.Website();
        bp.setWebsite(website);
        Blueprint.Media media = new Blueprint.Media();
        List<Blueprint.ScreenshotWrapper> screenshots = new ArrayList<Blueprint.ScreenshotWrapper>();
        Blueprint.ScreenshotWrapper sw1 = new Blueprint.ScreenshotWrapper();
        Blueprint.Screenshot ss1 = new Blueprint.Screenshot();
        sw1.setScreenshot(ss1);
        screenshots.add(sw1);
        Blueprint.Screenshot ss2 = new Blueprint.Screenshot();
        Blueprint.ScreenshotWrapper sw2 = new Blueprint.ScreenshotWrapper();
        sw2.setScreenshot(ss2);
        screenshots.add(sw2);
        media.setScreenshots(screenshots);
        List<Blueprint.VideoWrapper> videos = new ArrayList<Blueprint.VideoWrapper>();
        Blueprint.VideoWrapper vw1 = new Blueprint.VideoWrapper();
        Blueprint.Video v1 = new Blueprint.Video();
        vw1.setVideo(v1);
        videos.add(vw1);
        Blueprint.VideoWrapper vw2 = new Blueprint.VideoWrapper();
        Blueprint.Video v2 = new Blueprint.Video();
        vw2.setVideo(v2);
        videos.add(vw2);
        media.setVideos(videos);
        Blueprint.Developer developer = new Blueprint.Developer();
        List<Blueprint.PersonWrapper> people = new ArrayList<Blueprint.PersonWrapper>();
        Blueprint.PersonWrapper pw1 = new Blueprint.PersonWrapper();
        Blueprint.Person p1 = new Blueprint.Person();
        pw1.setPerson(p1);
        people.add(pw1);
        Blueprint.PersonWrapper pw2 = new Blueprint.PersonWrapper();
        Blueprint.Person p2 = new Blueprint.Person();
        pw2.setPerson(p2);
        people.add(pw2);
        developer.setPeople(people);
        Blueprint.Company company = new Blueprint.Company();
        developer.setCompany(company);
        media.setDeveloper(developer);
        bp.setMedia(media);
        Blueprint.Build build = new Blueprint.Build();
        build.setDate(ZonedDateTime.now().format(DateTimeFormatter.ofPattern(StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ)));
        build.setId(UUID.randomUUID().toString());
        bp.setBuild(build);
        Blueprint.License license = new Blueprint.License();
        bp.setLicense(license);
        List<Blueprint.CraftercmsVersionSupported> cvs = new ArrayList<Blueprint.CraftercmsVersionSupported>();
        Blueprint.CraftercmsVersionSupported cvs1 = new Blueprint.CraftercmsVersionSupported();
        cvs.add(cvs1);
        Blueprint.CraftercmsVersionSupported cvs2 = new Blueprint.CraftercmsVersionSupported();
        cvs.add(cvs2);
        bp.setCraftercmsVersionsSupported(cvs);
        bpd.setBlueprint(bp);

        String output = yaml.dumpAsMap(bpd);
        System.out.println(output);

        FileReader fr = new FileReader("descriptor.yaml");
        BlueprintDescriptor bdp2 = yaml.loadAs(fr, BlueprintDescriptor.class);
        String output2 = yaml.dumpAsMap(bdp2);
        System.out.println(output2);
    }

    private String descriptorVersion;
    private Blueprint blueprint;

    public String getDescriptorVersion() {
        return descriptorVersion;
    }

    public void setDescriptorVersion(String descriptorVersion) {
        this.descriptorVersion = descriptorVersion;
    }

    public Blueprint getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(Blueprint blueprint) {
        this.blueprint = blueprint;
    }
}
