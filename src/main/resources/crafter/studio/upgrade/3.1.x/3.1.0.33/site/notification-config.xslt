<?xml version="1.0" encoding="UTF-8"?>
  <!--
  ~ Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
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
<xsl:output method="xml" indent="yes" cdata-section-elements="body content subject"  />
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

    <xsl:template match="notificationConfig/lang">
        <xsl:copy>
            <xsl:apply-templates/>
            <xsl:if test="not(repositoryMergeConflictNotification)">
                <xsl:comment>list of email addresses to notify in case of repository merge conflict</xsl:comment>
                <xsl:element name="repositoryMergeConflictNotification">
                    <xsl:element name="email">
                        <xsl:text>admin@example.com</xsl:text>
                    </xsl:element>
                    <xsl:element name="email">
                        <xsl:text>admin2@example.com</xsl:text>
                    </xsl:element>
                </xsl:element>
                <xsl:text>&#10;</xsl:text>
            </xsl:if>
        </xsl:copy>
    </xsl:template>


    <xsl:template match="notificationConfig/lang/emailTemplates">
        <xsl:copy >
            <xsl:apply-templates/>
            <xsl:if test="not(emailTemplate[@key='repositoryMergeConflict'])">
                <xsl:element name="emailTemplate">
                    <xsl:attribute name="key">repositoryMergeConflict</xsl:attribute>
                    <xsl:element name="subject">
                        <xsl:text>Repository merge conflict for site ${siteName}</xsl:text>
                    </xsl:element>
                    <xsl:element name="body">
                        <xsl:text disable-output-escaping="yes">
                            &lt;![CDATA[
                        <![CDATA[<html>
                            <head>
                                <meta charset="utf-8"/>
                            </head>
                            <body style=" font-size: 12pt;">
                                <p>
                                    The following content was unable to be merged:
                                    <ul>
                                        <#list files as file>
                                                <li>${file}</li>
                                        </#list>
                                    </ul>
                                </p>
                            </body>
                        </html>
]]>]]&gt;
                        </xsl:text>
                    </xsl:element>
                </xsl:element>
                <xsl:text>&#10;</xsl:text>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <!-- define parameter -->
    <xsl:param name="version" />

    <!-- update the version if it already exist -->
    <xsl:template match="version">
        <xsl:copy>
            <xsl:value-of select="$version"/>
        </xsl:copy>
    </xsl:template>

    <!-- add the version if it doesn't exist -->
    <xsl:template match="/*[1]">
        <xsl:choose>
            <xsl:when test="not(version)">
                <xsl:copy>
                    <xsl:text>&#10;</xsl:text>
                    <xsl:text>&#x9;</xsl:text>
                    <xsl:element name="version">
                        <xsl:value-of select="$version"/>
                    </xsl:element>
                    <xsl:apply-templates select="node() | @*"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="node() | @*"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
