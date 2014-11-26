<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" exclude-result-prefixes="xs" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema">
	<xsl:output indent="yes"/>
		<xsl:param name="contentType"></xsl:param>
		<xsl:param name="contentTypeLabel"></xsl:param>

	<xsl:template match="/">
		<form>
			<title><xsl:value-of select="$contentTypeLabel"/></title>
			<description/>
			<content-type>/page/<xsl:value-of select="$contentType"/></content-type>
			<objectType>page</objectType>
			<properties>
			    <property>
					<name>placeInNav</name>
					<label>Show In Nav</label>
					<value>true</value>
					<type>boolean</type>
				</property>
				<property>
					<name>display-template</name>
					<label>Display Template</label>
					<value>/templates/web/<xsl:value-of select="$contentType"/>.ftl</value>
					<type>template</type>
				</property>
				<property>
					<name>content-type</name>
					<label>Content Type</label>
					<value>/page/<xsl:value-of select="$contentType"/></value>
					<type>string</type>
				</property>
				<property>
					<name>descriptor-mapper</name>
					<label>Descriptor Mapper</label>
					<value>hierarchical-mapper</value>
					<type>string</type>
				</property>
			</properties>
			<sections>
				<section>
					<title>Properties</title>
					<description/>
					<defaultOpen>true</defaultOpen>
					<fields>
						<field>
							<type>file-name</type>
							<id>file-name</id>
							<iceId></iceId>
							<title>pageURL</title>
							<description>DESC</description>
							<defaultValue></defaultValue>
							<help>HELP</help>
							<properties>
								<property>
									<name>size</name>
									<value>50</value>
									<type>int</type>
								</property>
								<property>
									<name>maxlength</name>
									<value>50</value>
									<type>int</type>
								</property>
								<property>
									<name>path</name>
									<value></value>
									<type>string</type>
								</property>
							</properties>
							<constraints>
							</constraints>
						</field>
						<field>
							<type>input</type>
							<id>internal-name</id>
							<iceId>optional</iceId>
							<title>Internal Title</title>
							<description>optional</description>
							<defaultValue></defaultValue>
							<help>optional</help>
							<properties>
								<property>
									<name>size</name>
									<value>50</value>
									<type>int</type>
								</property>
								<property>
									<name>maxlength</name>
									<value>60</value>
									<type>int</type>
								</property>
							</properties>
							<constraints>
								<constraint>
									<name>required</name>
									<value>true</value>
								<type>int</type>
								</constraint>
							</constraints>
						</field>
						
						<xsl:apply-templates/>
					</fields>
				</section>
			</sections>
		</form>
	</xsl:template>

	<xsl:template match="//xs:element">
		<xsl:call-template name="handleField">
			<xsl:with-param name="field" select="." ></xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="handleField">
		<xsl:param name="field"></xsl:param>
		<xsl:param name="isRepeat"></xsl:param>

		<xsl:if test="$field/@maxOccurs = 1 or $isRepeat='1'">
			<xsl:choose>
				<xsl:when test="$field/@type = 'xs:normalizedString'"><xsl:call-template name="input"><xsl:with-param name="field" select="$field"/></xsl:call-template></xsl:when>
				<xsl:when test="$field/@type = 'xs:string'"><xsl:call-template name="rte"><xsl:with-param name="field" select="$field"/></xsl:call-template></xsl:when>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="$field/@maxOccurs &gt; 1 or $field/@maxOccurs = 'unbounded' and $isRepeat !='1' ">
			<xsl:call-template name="repeat"><xsl:with-param name="field" select="$field"/></xsl:call-template>
		</xsl:if>

	</xsl:template>
           
           <!-- //////////////////////// -->
           <!--         Field Templates      -->
           <!-- //////////////////////// -->
           
	<xsl:template name="repeat">
		<xsl:param name="field"/>
		<field>
			<type>repeat</type>
			<id><xsl:value-of select="$field/@name"/></id>
			<iceId><xsl:value-of select="$field/@name"/></iceId>
			<title><xsl:value-of select="$field/@name"/></title>
			<minOccurs><xsl:value-of select="$field/@minOccurs"/></minOccurs>
			<maxOccurs><xsl:if test="$field/@maxOccurs='unbounded'">*</xsl:if><xsl:if test="$field/@maxOccurs!='unbounded'"><xsl:value-of select="$field/@maxOccurs"/></xsl:if></maxOccurs>
			<description/>
			<defaultValue/>
			<help/>
			<properties>
			</properties>
			<constraints>
			</constraints>
			<fields>
				<xsl:call-template name="handleField">
					<xsl:with-param name="field" select="$field"></xsl:with-param>
					<xsl:with-param name="isRepeat">1</xsl:with-param>
				</xsl:call-template>
			</fields>
		</field>
	</xsl:template>
	
	<xsl:template name="input">
		<xsl:param name="field"/>
		<field>
			<type>input</type>
			<id><xsl:value-of select="$field/@name"/></id>
			<iceId><xsl:value-of select="$field/@name"/></iceId>
			<title><xsl:value-of select="$field/@name"/></title>
			<description/>
			<defaultValue/>
			<help/>
			<properties>
				<property>
					<name>size</name>
					<value>50</value>
					<type>int</type>
				</property>
				<property>
					<name>maxlength</name>
					<value>50</value>
					<type>int</type>
				</property>
			</properties>
			<constraints>
				<constraint>
					<name>required</name>
					<value><xsl:if test="$field/@minOccurs>0">true</xsl:if><xsl:if test="$field/@minOccurs=0">false</xsl:if></value>
					<type>boolean</type>
				</constraint>
			</constraints>
		</field>
	</xsl:template>

	<xsl:template name="rte">
		<xsl:param name="field"/>
		<field>
			<type>rte</type>
			<id><xsl:value-of select="$field/@name"/></id>
			<iceId><xsl:value-of select="$field/@name"/></iceId>
			<title><xsl:value-of select="$field/@name"/></title>
			<description/>
			<defaultValue/>
			<help/>
			<properties>
				<property>
					<name>height</name>
					<value>500</value>
					<type>int</type>
				</property>
				<property>
					<name>width</name>
					<value>500</value>
					<type>int</type>
				</property>
				<property>
					<name>maxlength</name>
					<value>50</value>
					<type>int</type>
				</property>
			</properties>
			<constraints>
				<constraint>
					<name>required</name>
					<value><xsl:if test="$field/@minOccurs>0">true</xsl:if><xsl:if test="$field/@minOccurs=0">false</xsl:if></value>
					<type>boolean</type>
				</constraint>
			</constraints>
		</field>
	</xsl:template>

</xsl:stylesheet>