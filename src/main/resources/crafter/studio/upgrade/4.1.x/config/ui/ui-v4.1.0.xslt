<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

    <!-- craftercms.components.Dashboard -> craftercms.components.SiteDashboard -->
    <xsl:template
            match="//widget[@id='craftercms.components.ToolsPanel']/configuration/widgets/widget[@id='craftercms.components.ToolsPanelEmbeddedAppViewButton']/configuration/widget[@id='craftercms.components.Dashboard']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="id">craftercms.components.SiteDashboard</xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- craftercms.components.PathNavigator -> craftercms.components.PathNavigatorTree -->
    <xsl:template
            match="//widget[@id='craftercms.components.ToolsPanel']/configuration/widgets/widget[@id='craftercms.components.PathNavigator']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="id">craftercms.components.PathNavigatorTree</xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- craftercms.components.LegacyInReviewDashlet -> craftercms.components.PendingApprovalDashlet -->
    <xsl:template
            match="/siteUi/widget[@id='craftercms.components.Dashboard']/configuration/widgets/widget[@id='craftercms.components.LegacyInReviewDashlet']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="id">craftercms.components.PendingApprovalDashlet</xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- craftercms.components.LegacyUnpublishedDashlet -> craftercms.components.UnpublishedDashlet -->
    <xsl:template
            match="/siteUi/widget[@id='craftercms.components.Dashboard']/configuration/widgets/widget[@id='craftercms.components.LegacyUnpublishedDashlet']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="id">craftercms.components.UnpublishedDashlet</xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- craftercms.components.ApprovedScheduledDashlet -> craftercms.components.ScheduledDashlet -->
    <xsl:template
            match="/siteUi/widget[@id='craftercms.components.Dashboard']/configuration/widgets/widget[@id='craftercms.components.ApprovedScheduledDashlet']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="id">craftercms.components.ScheduledDashlet</xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- craftercms.components.RecentActivityDashlet -> craftercms.components.MyRecentActivityDashlet -->
    <xsl:template
            match="/siteUi/widget[@id='craftercms.components.Dashboard']/configuration/widgets/widget[@id='craftercms.components.RecentActivityDashlet']">
        <xsl:copy>
            <xsl:attribute name="id">craftercms.components.MyRecentActivityDashlet</xsl:attribute>
            <xsl:element name="permittedRoles">
                <xsl:element name="role">admin</xsl:element>
                <xsl:element name="role">developer</xsl:element>
                <xsl:element name="role">publisher</xsl:element>
            </xsl:element>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- Remove craftercms.components.IconGuideDashlet widget -->
    <xsl:template
            match="/siteUi/widget[@id='craftercms.components.Dashboard']/configuration/widgets/widget[@id='craftercms.components.IconGuideDashlet']">
    </xsl:template>

    <xsl:template match="/siteUi/widget[@id='craftercms.components.Dashboard']/configuration/widgets">
        <xsl:element name="mainSection">
            <xsl:copy>
                <xsl:copy-of select="@*"/>

                <!-- Add the items in the desired order -->
                <xsl:apply-templates select="widget[@id='craftercms.components.RecentActivityDashlet']"/>
                <xsl:apply-templates select="widget[@id='craftercms.components.LegacyUnpublishedDashlet']"/>
                <xsl:apply-templates select="widget[@id='craftercms.components.LegacyInReviewDashlet']"/>
                <xsl:apply-templates select="widget[@id='craftercms.components.ApprovedScheduledDashlet']"/>
                <xsl:apply-templates select="widget[@id='craftercms.components.RecentlyPublishedDashlet']"/>

                <!-- Add new ExpiringDashlet widget -->
                <xsl:element name="widget">
                    <xsl:attribute name="id">craftercms.components.ExpiringDashlet</xsl:attribute>
                    <xsl:element name="permittedRoles">
                        <xsl:element name="role">admin</xsl:element>
                        <xsl:element name="role">developer</xsl:element>
                        <xsl:element name="role">publisher</xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:copy>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
