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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xls="http://www.w3.org/1999/XSL/Transform">

    <!-- to keep the right formatting -->
    <xsl:output method="xml" indent="yes"/>
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

    <!-- Update rules for 'content-type' dependencies -->
    <xsl:template match="/dependency-resolver/item-types/item-type[name='content-type']/dependency-types">
        <xsl:copy>
            <xsl:for-each select="dependency-type">
                <!-- Keep dependency-type elements except rendering-template and script -->
                <xsl:if test="not(name='rendering-template' or name='script')">
                    <xsl:copy-of select="."/>
                </xsl:if>
            </xsl:for-each>
            <!-- Add the new dependency-type elements -->
            <xsl:element name="dependency-type">
                <xsl:element name="name">
                    <xsl:text>rendering-template</xsl:text>
                </xsl:element>
                <xsl:element name="includes">
                    <xsl:element name="pattern">
                        <xsl:element name="find-regex">
                            <xsl:text>/templates/([^&lt;"]+)\.ftl</xsl:text>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
            <xsl:element name="dependency-type">
                <xsl:element name="name">
                    <xsl:text>script</xsl:text>
                </xsl:element>
                <xsl:element name="includes">
                    <xsl:element name="pattern">
                        <xsl:element name="find-regex">
                            <xsl:text>&lt;content-type&gt;/(.*)/(.*)&lt;/content-type&gt;</xsl:text>
                        </xsl:element>
                        <xsl:element name="transforms">
                            <xsl:element name="transform">
                                <xsl:element name="match">
                                    <xsl:text>&lt;content-type&gt;/(.*)/(.*)&lt;/content-type&gt;</xsl:text>
                                </xsl:element>
                                <xsl:element name="replace">
                                    <xsl:text>/scripts/$1s/$2.groovy</xsl:text>
                                </xsl:element>
                            </xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>