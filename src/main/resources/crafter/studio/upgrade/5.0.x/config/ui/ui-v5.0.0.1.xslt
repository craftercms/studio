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
                </configuration>
            </widget>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
