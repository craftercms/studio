<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <!-- define parameter -->
    <xsl:param name="version" />

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
