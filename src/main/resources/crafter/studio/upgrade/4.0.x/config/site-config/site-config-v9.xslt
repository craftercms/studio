<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

    <!-- Replace the previous tag with the new section -->
    <xsl:template match="site-config/default-timezone">
        <xsl:element name="locale">
            <xsl:comment>
    BCP 47 language tag (e.g. en-US) or unicode extension (e.g. "en-US-u-ca-buddhist").
    Leave empty for using the user's browser locale (i.e. dates/times will be displayed in each users's system locale).
    Specifying a locale code will apply those localization settings to *all* users regardless of their system settings
    or location. For example, if "en-US", is specified, all users will see dates as month/day/year instead of day/month/year
    regardless of their system (i.e. OS) locale preference.
    </xsl:comment>
            <xsl:element name="localeCode">
                <xsl:text>en-US</xsl:text>
            </xsl:element>
            <xsl:comment>
    Use `dateTimeFormatOptions` to customize how dates &amp; times get displayed on Studio UI.
    For full list of options and docs, visit: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/DateTimeFormat/DateTimeFormat
    </xsl:comment>
            <xsl:element name="dateTimeFormatOptions">
                <xsl:comment>
        Specifying a time zone (i.e. `timeZone` element) will express dates/times across the UI in the time zone you specify
        here. Leaving it unspecified, will display dates/times to each user in their own system time zone.
        </xsl:comment>
                <xsl:element name="timeZone">
                    <xsl:value-of select="text()" />
                </xsl:element>
                <xsl:element name="day">
                    <xsl:text>numeric</xsl:text>
                </xsl:element>
                <xsl:element name="month">
                    <xsl:text>numeric</xsl:text>
                </xsl:element>
                <xsl:element name="year">
                    <xsl:text>numeric</xsl:text>
                </xsl:element>
                <xsl:element name="hour">
                    <xsl:text>numeric</xsl:text>
                </xsl:element>
                <xsl:element name="minute">
                    <xsl:text>numeric</xsl:text>
                </xsl:element>
                <xsl:comment>
        Set `hour12` to "false" to show times in 24 hour format.
        </xsl:comment>
                <xsl:element name="hour12">
                    <xsl:text>true</xsl:text>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>