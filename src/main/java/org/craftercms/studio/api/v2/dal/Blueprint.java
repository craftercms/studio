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

import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.List;

public class Blueprint {

    public static void main(String[] args) {

        Yaml yaml = new Yaml();
        String output = yaml.dumpAsMap(new Blueprint());
        System.out.println(output);
    }

    private String id;
    private String name;
    private String tags;
    private Version version;
    private String description;
    private Website website;
    private Media media;
    private Build build;
    private License license;
    private List<CraftercmsVersionSupported> craftercmsVersionsSupported;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public List<CraftercmsVersionSupported> getCraftercmsVersionsSupported() {
        return craftercmsVersionsSupported;
    }

    public void setCraftercmsVersionsSupported(List<CraftercmsVersionSupported> craftercmsVersionsSupported) {
        this.craftercmsVersionsSupported = craftercmsVersionsSupported;
    }

    public static class Version {

        private int major;
        private int minor;
        private int patch;

        public int getMajor() {
            return major;
        }

        public void setMajor(int major) {
            this.major = major;
        }

        public int getMinor() {
            return minor;
        }

        public void setMinor(int minor) {
            this.minor = minor;
        }

        public int getPatch() {
            return patch;
        }

        public void setPatch(int patch) {
            this.patch = patch;
        }
    }

    public static class Website {

        String name;
        String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Media {

        List<ScreenshotWrapper> screenshots;
        List<VideoWrapper> videos;
        Developer developer;

        public List<ScreenshotWrapper> getScreenshots() {
            return screenshots;
        }

        public void setScreenshots(List<ScreenshotWrapper> screenshots) {
            this.screenshots = screenshots;
        }

        public List<VideoWrapper> getVideos() {
            return videos;
        }

        public void setVideos(List<VideoWrapper> videos) {
            this.videos = videos;
        }

        public Developer getDeveloper() {
            return developer;
        }

        public void setDeveloper(Developer developer) {
            this.developer = developer;
        }
    }

    public static class ScreenshotWrapper {
        private Screenshot screenshot;

        public Screenshot getScreenshot() {
            return screenshot;
        }

        public void setScreenshot(Screenshot screenshot) {
            this.screenshot = screenshot;
        }
    }

    public static class Screenshot {

        String title;
        String description;
        String url;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class VideoWrapper {

        private Video video;

        public Video getVideo() {
            return video;
        }

        public void setVideo(Video video) {
            this.video = video;
        }
    }

    public static class Video {

        String title;
        String description;
        String url;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Developer {

        List<PersonWrapper> people;
        Company company;

        public List<PersonWrapper> getPeople() {
            return people;
        }

        public void setPeople(List<PersonWrapper> people) {
            this.people = people;
        }

        public Company getCompany() {
            return company;
        }

        public void setCompany(Company company) {
            this.company = company;
        }
    }

    public static class PersonWrapper {
        private Person person;

        public Person getPerson() {
            return person;
        }

        public void setPerson(Person person) {
            this.person = person;
        }
    }

    public static class Person {

        String name;
        String email;
        String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Company {

        String name;
        String email;
        String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Build {

        String id;
        String date;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    public static class License {
        String url;
        String name;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class CraftercmsVersionSupported {
        String version;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}

