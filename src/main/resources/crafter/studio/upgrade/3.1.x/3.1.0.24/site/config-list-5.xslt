<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
    <xsl:output method="xml" indent="yes" />
    <xsl:strip-space elements="*"/>

    <!-- copy all elements -->
    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>

    <!-- insert line breaks before and after top level comments -->
    <xsl:template match="/comment()">
        <xsl:text>&#10;</xsl:text><xsl:copy-of select="."/><xsl:text>&#10;</xsl:text>
    </xsl:template>

    <xsl:template name="search-and-replace">
        <xsl:param name="input"/>
        <xsl:param name="search-string"/>
        <xsl:param name="replace-string"/>
        <xsl:choose>
            <!-- See if the input contains the search string -->
            <xsl:when test="$search-string and
                           contains($input,$search-string)">
                <!-- If so, then concatenate the substring before the search
                string to the replacement string and to the result of
                recursively applying this template to the remaining substring.
                -->
                <xsl:value-of
                        select="substring-before($input,$search-string)"/>
                <xsl:value-of select="$replace-string"/>
                <xsl:call-template name="search-and-replace">
                    <xsl:with-param name="input"
                                    select="substring-after($input,$search-string)"/>
                    <xsl:with-param name="search-string"
                                    select="$search-string"/>
                    <xsl:with-param name="replace-string"
                                    select="$replace-string"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <!-- There are no more occurences of the search string so
                just return the current input string -->
                <xsl:value-of select="$input"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:param name="oldSamplePathRoot" select="'/studio/administration/samples/'" />
    <xsl:param name="newSamplePathRoot" select="''" />

    <xsl:template match="files/file/samplePath/text()">
        <xsl:value-of>
            <xsl:call-template name="search-and-replace">
                <xsl:with-param name="input" select="current()"/>
                <xsl:with-param name="search-string" select="$oldSamplePathRoot" />
                <xsl:with-param name="replace-string" select="$newSamplePathRoot" />
            </xsl:call-template>
        </xsl:value-of>
    </xsl:template>

</xsl:stylesheet>