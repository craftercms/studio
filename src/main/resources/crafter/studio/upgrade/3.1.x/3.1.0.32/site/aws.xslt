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

    <xsl:template match="aws">
        <aws>
            <!-- Check if there are any profiles for S3 -->
            <xsl:if test="s3/profile or profile/bucketName">
                <s3>
                    <!-- Check if there are profiles defined in the new format and copy them -->
                    <xsl:for-each select="s3/profile">
                        <xsl:copy>
                            <xsl:apply-templates select="node() | @*"/>
                        </xsl:copy>
                    </xsl:for-each>

                    <!-- Check if there are profiles defined in the previous format and copy them -->
                    <xsl:for-each select="profile">
                        <xsl:variable name="profileId" select="id"/>
                        <xsl:if test="contains($profileId, 's3') or bucketName">
                            <xsl:copy>
                                <xsl:apply-templates select="node() | @*"/>
                            </xsl:copy>
                        </xsl:if>
                    </xsl:for-each>
                </s3>
            </xsl:if>

            <!-- Repeat for elastiTrancoder -->
            <xsl:if test="elasticTranscoder/profile or profile/pipelineId">
                <elasticTranscoder>
                    <xsl:for-each select="elasticTranscoder/profile">
                        <xsl:copy>
                            <xsl:apply-templates select="node() | @*"/>
                        </xsl:copy>
                    </xsl:for-each>
                    <xsl:for-each select="profile">
                        <xsl:variable name="profileId" select="id"/>
                        <xsl:if test="contains($profileId, 'elastic-transcoder') or pipelineId">
                            <xsl:copy>
                                <xsl:apply-templates select="node() | @*"/>
                            </xsl:copy>
                        </xsl:if>
                    </xsl:for-each>
                </elasticTranscoder>
            </xsl:if>

            <!-- Repeat for mediaConvert -->
            <xsl:if test="mediaConvert/profile or profile/queue">
                <mediaConvert>
                    <xsl:for-each select="mediaConvert/profile">
                        <xsl:copy>
                            <xsl:apply-templates select="node() | @*"/>
                        </xsl:copy>
                    </xsl:for-each>
                    <xsl:for-each select="profile">
                        <xsl:variable name="profileId" select="id"/>
                        <xsl:if test="contains($profileId, 'mediaconvert') or queue">
                            <xsl:copy>
                                <xsl:apply-templates select="node() | @*"/>
                            </xsl:copy>
                        </xsl:if>
                    </xsl:for-each>
                </mediaConvert>
            </xsl:if>
        </aws>
    </xsl:template>
</xsl:stylesheet>
