<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License version 3 as published by
  ~ the Free Software Foundation.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  -->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!-- Identity template: copy everything by default -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <!-- Add <widget> to <siteUi> if it does not exist -->
    <xsl:template match="siteUi/widget[last()]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
        <!-- If there is no craftercms.components.ContentTypeManagement widget under siteUi, add it here -->
        <xsl:if test="not(../widget[@id='craftercms.components.ContentTypeManagement'])">
            <widget id="craftercms.components.ContentTypeManagement">
                <configuration>
                    <objectTypes id="page">
                        <descriptor>
                            <name>Page</name>
                            <type>page</type>
                            <fields>
                                <file-name>
                                    <defaultValue/>
                                    <description/>
                                    <helpText/>
                                    <id>file-name</id>
                                    <name>Page URL</name>
                                    <type>file-name</type>
                                </file-name>
                                <internal-name>
                                    <defaultValue/>
                                    <description/>
                                    <helpText/>
                                    <id>internal-name</id>
                                    <name>Internal Name</name>
                                    <type>input</type>
                                </internal-name>
                                <navLabel>
                                    <defaultValue/>
                                    <description/>
                                    <helpText/>
                                    <id>navLabel</id>
                                    <name>Nav Label</name>
                                    <type>input</type>
                                </navLabel>
                                <placeInNav>
                                    <defaultValue/>
                                    <description/>
                                    <helpText/>
                                    <id>placeInNav</id>
                                    <name>Place In Nav</name>
                                    <type>page-nav-order</type>
                                </placeInNav>
                            </fields>
                            <sections title="System Properties">
                                <id>defaultSection</id>
                                <color>rgba(255,0,0,.7)</color>
                                <title>System Properties</title>
                                <expandByDefault>true</expandByDefault>
                                <fields>file-name</fields>
                                <fields>internal-name</fields>
                                <fields>placeInNav</fields>
                                <fields>navLabel</fields>
                            </sections>
                        </descriptor>
                    </objectTypes>
                    <objectTypes id="component">
                        <descriptor>
                            <name>Component</name>
                            <type>component</type>
                            <fields>
                                <file-name>
                                    <defaultValue/>
                                    <description/>
                                    <helpText/>
                                    <id>file-name</id>
                                    <name>Component ID</name>
                                    <type>auto-filename</type>
                                </file-name>
                                <internal-name>
                                    <defaultValue/>
                                    <description/>
                                    <helpText/>
                                    <id>internal-name</id>
                                    <name>Internal Name</name>
                                    <type>input</type>
                                </internal-name>
                            </fields>
                            <sections title="System Properties">
                                <id>defaultSection</id>
                                <color>rgba(255,0,0,.7)</color>
                                <title>System Properties</title>
                                <expandByDefault>true</expandByDefault>
                                <fields>file-name</fields>
                                <fields>internal-name</fields>
                            </sections>
                        </descriptor>
                    </objectTypes>
                    <controls id="repeat">
                        <icon id="@mui/icons-material/ReplayOutlined"/>
                    </controls>
                    <controls id="input">
                        <icon id="@mui/icons-material/DriveFileRenameOutlineOutlined"/>
                    </controls>
                    <controls id="input-email">
                        <icon id="@mui/icons-material/AlternateEmailOutlined"/>
                    </controls>
                    <controls id="input-link">
                        <icon id="@mui/icons-material/LinkOutlined"/>
                    </controls>
                    <controls id="input-phone">
                        <icon id="@mui/icons-material/PhoneAndroidOutlined"/>
                    </controls>
                    <controls id="numeric-input">
                        <icon id="@mui/icons-material/PinOutlined"/>
                    </controls>
                    <controls id="textarea">
                        <icon id="@mui/icons-material/NotesOutlined"/>
                    </controls>
                    <controls id="rte">
                        <icon id="@mui/icons-material/CodeOutlined"/>
                    </controls>
                    <controls id="dropdown">
                        <icon id="@mui/icons-material/ArrowDropDownOutlined"/>
                    </controls>
                    <controls id="time">
                        <icon id="@mui/icons-material/AccessTimeOutlined"/>
                    </controls>
                    <controls id="date-time">
                        <icon id="@mui/icons-material/CalendarMonthOutlined"/>
                    </controls>
                    <controls id="checkbox">
                        <icon id="@mui/icons-material/CheckBoxOutlined"/>
                    </controls>
                    <controls id="checkbox-group">
                        <icon id="@mui/icons-material/ChecklistOutlined"/>
                    </controls>
                    <controls id="node-selector">
                        <icon id="@mui/icons-material/ZoomInMapOutlined"/>
                    </controls>
                    <controls id="image-picker">
                        <icon id="@mui/icons-material/InsertPhotoOutlined"/>
                    </controls>
                    <controls id="video-picker">
                        <icon id="@mui/icons-material/VideocamOutlined"/>
                    </controls>
                    <controls id="transcoded-video-picker">
                        <icon id="@mui/icons-material/VideocamOutlined"/>
                    </controls>
                    <controls id="label">
                        <icon id="@mui/icons-material/SellOutlined"/>
                    </controls>
                    <controls id="page-nav-order">
                        <icon id="@mui/icons-material/ImportExportOutlined"/>
                    </controls>
                    <controls id="file-name">
                        <icon id="@mui/icons-material/InsertDriveFileOutlined"/>
                    </controls>
                    <controls id="auto-filename">
                        <icon id="@mui/icons-material/DescriptionOutlined"/>
                    </controls>
                    <controls id="internal-name">
                        <icon id="@mui/icons-material/FontDownloadOutlined"/>
                    </controls>
                    <controls id="locale-selector">
                        <icon id="@mui/icons-material/PublicOutlined"/>
                    </controls>
                    <controls id="disabled">
                        <icon id="@mui/icons-material/NotInterestedOutlined"/>
                    </controls>
                    <controls id="forcehttps">
                        <icon id="@mui/icons-material/HttpsOutlined"/>
                    </controls>
                    <controls id="uuid">
                        <icon id="@mui/icons-material/BadgeOutlined"/>
                    </controls>
                    <controls id="expired-date">
                        <icon id="@mui/icons-material/EventBusyOutlined"/>
                    </controls>
                    <datasources id="components">
                        <icon id="@mui/icons-material/ExtensionOutlined"/>
                    </datasources>
                    <datasources id="shared-content">
                        <icon id="@mui/icons-material/ShareOutlined"/>
                    </datasources>
                    <datasources id="embedded-content">
                        <icon id="@mui/icons-material/AdjustOutlined"/>
                    </datasources>
                    <datasources id="img-desktop-upload">
                        <icon id="@mui/icons-material/InsertPhotoOutlined"/>
                    </datasources>
                    <datasources id="img-repository-upload">
                        <icon id="@mui/icons-material/InsertPhotoOutlined"/>
                    </datasources>
                    <datasources id="file-desktop-upload">
                        <icon id="@mui/icons-material/UploadFileOutlined"/>
                    </datasources>
                    <datasources id="file-browse-repo">
                        <icon id="@mui/icons-material/PanToolAltOutlined"/>
                    </datasources>
                    <datasources id="WebDAV-repo">
                        <icon id="@mui/icons-material/DnsOutlined"/>
                    </datasources>
                    <datasources id="img-WebDAV-repo">
                        <icon id="@mui/icons-material/DnsOutlined"/>
                    </datasources>
                    <datasources id="video-WebDAV-repo">
                        <icon id="@mui/icons-material/DnsOutlined"/>
                    </datasources>
                    <datasources id="WebDAV-upload">
                        <icon id="@mui/icons-material/DnsOutlined"/>
                    </datasources>
                    <datasources id="img-WebDAV-upload">
                        <icon id="@mui/icons-material/DnsOutlined"/>
                    </datasources>
                    <datasources id="video-WebDAV-upload">
                        <icon id="@mui/icons-material/DnsOutlined"/>
                    </datasources>
                    <datasources id="S3-repo">
                        <icon id="@mui/icons-material/InsertDriveFileOutlined"/>
                    </datasources>
                    <datasources id="img-S3-repo">
                        <icon id="@mui/icons-material/InsertPhotoOutlined"/>
                    </datasources>
                    <datasources id="video-S3-repo">
                        <icon id="@mui/icons-material/VideocamOutlined"/>
                    </datasources>
                    <datasources id="S3-upload">
                        <icon id="@mui/icons-material/InsertDriveFileOutlined"/>
                    </datasources>
                    <datasources id="img-S3-upload">
                        <icon id="@mui/icons-material/InsertPhotoOutlined"/>
                    </datasources>
                    <datasources id="video-S3-upload">
                        <icon id="@mui/icons-material/VideocamOutlined"/>
                    </datasources>
                    <datasources id="video-S3-transcoding">
                        <icon id="@mui/icons-material/VideocamOutlined"/>
                    </datasources>
                    <datasources id="video-desktop-upload">
                        <icon id="@mui/icons-material/VideocamOutlined"/>
                    </datasources>
                    <datasources id="video-browse-repo">
                        <icon id="@mui/icons-material/VideocamOutlined"/>
                    </datasources>
                    <datasources id="key-value-list">
                        <icon id="@mui/icons-material/KeyOutlined"/>
                    </datasources>
                    <datasources id="simpleTaxonomy">
                        <icon id="@mui/icons-material/StyleOutlined"/>
                    </datasources>
                    <datasources id="audio-desktop-upload">
                        <icon id="@mui/icons-material/AudioFileOutlined"/>
                    </datasources>
                    <datasources id="audio-browse-repo">
                        <icon id="@mui/icons-material/AudioFileOutlined"/>
                    </datasources>
                    <datasources id="configured-list">
                        <icon id="@mui/icons-material/AudioFileOutlined"/>
                    </datasources>
                </configuration>
            </widget>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
