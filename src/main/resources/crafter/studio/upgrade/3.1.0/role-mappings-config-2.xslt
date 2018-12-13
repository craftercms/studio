<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <!-- define parameter -->
    <xsl:param name="site_id" />

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

    <!-- update the group name attribute -->
    <xsl:template match="@name">
        <xsl:attribute name="name">
            <xsl:choose>
                <!-- if the name already has the site_id or it is a predefined group -->
                <xsl:when test="contains(., $site_id) or contains(., 'site_')">
                    <xsl:value-of select="lower-case(.)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="lower-case(concat($site_id, '_', .))"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:attribute>
    </xsl:template>

</xsl:stylesheet>
