<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <!-- to keep the right formatting -->
    <xsl:output method="xml" indent="yes" cdata-section-elements="${cdataElements}"/>
    <xsl:strip-space elements="*"/>

    <!-- copy all elements -->
    <xsl:template match="node() | @*">
        <!-- insert line breaks before comments -->
        <xsl:if test="self::comment()">
            <xsl:text>&#10;</xsl:text>
        </xsl:if>
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
        <!-- insert line breaks after comments -->
        <xsl:if test="self::comment()">
            <xsl:text>&#10;</xsl:text>
        </xsl:if>
    </xsl:template>

    <!-- Update title and icon for specific widget configurations -->
    <xsl:template match="/siteUi/widget[@id='craftercms.components.ICEToolsPanel']/configuration/widgets/widget[@id='craftercms.components.ToolsPanelPageButton']/configuration[title[@defaultMessage='Add Components']]">
        <configuration>
            <target id="icePanel"/>
            <title id="previewComponentsPanelTitle" defaultMessage="Create Content"/>
            <icon id="craftercms.icons.AddComponents"/>
            <widgets>
                <widget id="craftercms.components.PreviewComponentsPanel"/>
            </widgets>
        </configuration>
    </xsl:template>

    <xsl:template match="/siteUi/widget[@id='craftercms.components.ICEToolsPanel']/configuration/widgets/widget[@id='craftercms.components.ToolsPanelPageButton']/configuration[title[@defaultMessage='Browse Components']]">
        <configuration>
            <target id="icePanel"/>
            <title id="previewBrowseComponentsPanelTitle" defaultMessage="Existing Content"/>
            <icon id="craftercms.icons.BrowseComponents"/>
            <widgets>
                <widget id="craftercms.components.PreviewBrowseComponentsPanel"/>
            </widgets>
        </configuration>
    </xsl:template>

    <xsl:template match="/siteUi/widget[@id='craftercms.components.ICEToolsPanel']/configuration/widgets/widget[@id='craftercms.components.ToolsPanelPageButton']/configuration[title[@defaultMessage='Component Drop Targets']]">
        <configuration>
            <target id="icePanel"/>
            <title id="previewDropTargetsPanelTitle" defaultMessage="Drop Targets"/>
            <icon id="@mui/icons-material/MoveToInboxRounded"/>
            <widgets>
                <widget id="craftercms.components.PreviewDropTargetsPanel"/>
            </widgets>
        </configuration>
    </xsl:template>

</xsl:stylesheet>