<?xml version="1.0" encoding="UTF-8"?>
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
            <xsl:if test="not(datasource/name = 'WebDAV-repo')">
                <datasource>
                    <name>WebDAV-repo</name>
                    <icon>
                        <class>fa-square-o fa-server</class>
                    </icon>
                </datasource>
            </xsl:if>

            <xsl:if test="not(datasource/name = 'img-WebDAV-repo')">
                <datasource>
                    <name>img-WebDAV-repo</name>
                    <icon>
                        <class>fa-square-o fa-server</class>
                    </icon>
                </datasource>
            </xsl:if>

            <xsl:if test="not(datasource/name = 'video-WebDAV-repo')">
                <datasource>
                    <name>video-WebDAV-repo</name>
                    <icon>
                        <class>fa-square-o fa-server</class>
                    </icon>
                </datasource>
            </xsl:if>

            <xsl:if test="not(datasource/name = 'WebDAV-upload')">
                <datasource>
                    <name>WebDAV-upload</name>
                    <icon>
                        <class>fa-square-o fa-server</class>
                    </icon>
                </datasource>
            </xsl:if>

            <xsl:if test="not(datasource/name = 'img-WebDAV-upload')">
                <datasource>
                    <name>img-WebDAV-upload</name>
                    <icon>
                        <class>fa-square-o fa-server</class>
                    </icon>
                </datasource>
            </xsl:if>

            <xsl:if test="not(datasource/name = 'video-WebDAV-upload')">
                <datasource>
                    <name>video-WebDAV-upload</name>
                    <icon>
                        <class>fa-square-o fa-server</class>
                    </icon>
                </datasource>
            </xsl:if>
        </datasources>
    </xsl:template>

</xsl:stylesheet>