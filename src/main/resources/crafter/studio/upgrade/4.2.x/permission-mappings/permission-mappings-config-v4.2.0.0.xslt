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
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <!-- copy all elements -->
    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>

    <!-- Remove <rule> elements with regex="~DASHBOARD~" -->
    <xsl:template match="rule[@regex='~DASHBOARD~']"/>

    <!-- Match role elements to process their children -->
    <xsl:template match="role">
        <xsl:variable name="hasDashboardRule" select="rule[@regex='~DASHBOARD~']/allowed-permissions/permission = 'publish'" />
        <xsl:variable name="hasPublishByCommits" select="rule[@regex='.*']/allowed-permissions/permission = 'publish_by_commits'" />
        <xsl:copy>
            <xsl:apply-templates select="@*|node()[not(self::rule[@regex='.*'])]"/>
            <!-- Copy the rule with regex=".*" only if there is no DASHBOARD rule with publish permission -->
            <!-- Or there has already the permission publish_by_commits -->
            <xsl:if test="not($hasDashboardRule) or $hasPublishByCommits">
                <xsl:apply-templates select="rule[@regex='.*']"/>
            </xsl:if>
            <!-- If the role had a DASHBOARD rule with publish permission, add <permission>publish_by_commits</permission> to the rule with regex=".*" -->
            <xsl:if test="$hasDashboardRule">
                <xsl:if test="not(rule[@regex='.*']/allowed-permissions/permission = 'publish_by_commits')">
                    <rule regex=".*">
                        <allowed-permissions>
                            <xsl:apply-templates select="rule[@regex='.*']/allowed-permissions/permission"/>
                            <permission>publish_by_commits</permission>
                        </allowed-permissions>
                    </rule>
                </xsl:if>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
