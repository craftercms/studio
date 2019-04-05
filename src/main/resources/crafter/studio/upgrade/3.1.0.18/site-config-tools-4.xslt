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


    <xsl:template match="datasources">
        <datasources>
            <!-- copy all existing datasources -->
            <xsl:apply-templates select="node() | @*"/>

            <!-- add new ones if missing -->
            <xsl:if test="not(datasource/name = 'S3-repo')">
                <datasource>
                    <name>S3-repo</name>
                    <icon>
                        <class>fa-file-o</class>
                        <stackedclass>fa-amazon</stackedclass>
                    </icon>
                </datasource>
            </xsl:if>

            <xsl:if test="not(datasource/name = 'img-S3-repo')">
                <datasource>
                    <name>img-S3-repo</name>
                    <icon>
                        <class>fa-square</class>
                        <stackedclass>fa-amazon fa-inverse</stackedclass>
                    </icon>
                </datasource>
            </xsl:if>

            <xsl:if test="not(datasource/name = 'video-S3-repo')">
                <datasource>
                    <name>video-S3-repo</name>
                    <icon>
                        <class>fa-film</class>
                        <stackedclass>fa-amazon</stackedclass>
                    </icon>
                </datasource>
            </xsl:if>

            <xsl:if test="not(datasource/name = 'S3-upload')">
                <datasource>
                    <name>S3-upload</name>
                    <icon>
                        <class>fa-file-o</class>
                        <stackedclass>fa-amazon</stackedclass>
                    </icon>
                </datasource>
            </xsl:if>

            <xsl:if test="not(datasource/name = 'img-S3-upload')">
                <datasource>
                    <name>img-S3-upload</name>
                    <icon>
                        <class>fa-square</class>
                        <stackedclass>fa-amazon fa-inverse</stackedclass>
                    </icon>
                </datasource>
            </xsl:if>

            <xsl:if test="not(datasource/name = 'video-S3-upload')">
                <datasource>
                    <name>video-S3-upload</name>
                    <icon>
                        <class>fa-film</class>
                        <stackedclass>fa-amazon</stackedclass>
                    </icon>
                </datasource>
            </xsl:if>
        </datasources>
    </xsl:template>

    <xsl:template match="tools">
        <tools>
            <!-- copy all existing tools -->
            <xsl:apply-templates select="node() | @*"/>

            <!-- add new ones if missing -->
            <xsl:if test="not(tool/name = 'graphiql')">
                <tool>
                    <name>graphiql</name>
                    <label>GraphiQL</label>
                    <icon>
                        <class>fa-line-chart</class>
                    </icon>
                </tool>
            </xsl:if>

        </tools>
    </xsl:template>

    <!-- Remove the ones not needed any more -->
    <xsl:template match="tool[name/text()='logging']"/>

    <xsl:template match="tool[name/text()='groups']"/>

</xsl:stylesheet>