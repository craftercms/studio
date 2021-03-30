<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
        <html>
            <body>
                <xsl:apply-templates select="./*"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match='*'>
        <xsl:choose>
            <xsl:when test="count(*) > 0">
                <xsl:choose>
                    <xsl:when test="../..">
                        <tr>
                            <td style='font-weight:bold;'>
                                <xsl:attribute name='data-var'>
                                    <xsl:value-of select='local-name()'/>
                                </xsl:attribute>
                                <xsl:value-of select='local-name()'/>
                            </td>
                            <td>
                                <table>
                                    <xsl:apply-templates select="./*"/>
                                </table>
                            </td>
                        </tr>
                    </xsl:when>
                    <xsl:otherwise>
                        <table>
                            <xsl:apply-templates select="./*"/>
                        </table>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <tr>
                    <td style='font-weight:bold;'>
                        <xsl:attribute name='data-var'>
                            <xsl:value-of select='local-name()'/>
                        </xsl:attribute>
                        <xsl:value-of select='local-name()'/>
                    </td>
                    <td>
                        <xsl:value-of select='.'/>
                    </td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
