<?xml version="1.0"?>

<!DOCTYPE xsl:stylesheet [
    <!ENTITY db_xsl_path        "../lib/docbook-xsl/">
    <!ENTITY callout_gfx_path   "images/callouts/">
    <!ENTITY admon_gfx_path     "images/admons/">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		version="1.0">

<!-- Import of the original stylesheet which "just" creates 
     a bunch of HTML files from any valid DocBook instance -->
    <xsl:import href="&db_xsl_path;/html/chunk.xsl"/>

<!--###################################################
                     HTML Settings
    ################################################### -->   

    <xsl:param name="chunk.section.depth">'5'</xsl:param>
    <xsl:param name="use.id.as.filename">'1'</xsl:param>
    <xsl:param name="html.stylesheet">html.css</xsl:param>

    <!-- These extensions are required for table printing and other stuff -->
    <xsl:param name="use.extensions">1</xsl:param>
    <xsl:param name="tablecolumns.extension">0</xsl:param>
    <xsl:param name="callout.extensions">1</xsl:param>
    <xsl:param name="graphicsize.extension">0</xsl:param>
    
<!--###################################################
                      Table Of Contents
    ################################################### -->   

    <!-- Generate the TOCs for named components only -->
    <xsl:param name="generate.toc">0</xsl:param>
    
    <!-- Show only Sections up to level 3 in the TOCs -->
    <!--xsl:param name="toc.section.depth">3</xsl:param-->

<!--###################################################
                         Labels
    ################################################### -->   

    <!-- Label Chapters and Sections (numbering) -->
    <xsl:param name="chapter.autolabel">1</xsl:param>
    <xsl:param name="section.autolabel" select="1"/>
    <xsl:param name="section.label.includes.component.label" select="1"/>
                
<!--###################################################
                         Callouts
    ################################################### -->   

    <!-- Use images for callouts instead of (1) (2) (3) -->
    <xsl:param name="callout.graphics">1</xsl:param>
    <xsl:param name="callout.graphics.path">&callout_gfx_path;</xsl:param>
    
    <!-- Place callout marks at this column in annotated areas -->
    <xsl:param name="callout.defaultcolumn">90</xsl:param>

<!--###################################################
                       Admonitions
    ################################################### -->   

    <!-- Use nice graphics for admonitions -->
    <xsl:param name="admon.graphics">'1'</xsl:param>
    <xsl:param name="admon.graphics.path">&admon_gfx_path;</xsl:param>

<!--###################################################
                          Misc
    ################################################### -->   

    <!-- Placement of titles -->
    <xsl:param name="formal.title.placement">
        figure after
        example before
        equation before
        table before
        procedure before
    </xsl:param>    

<!-- You must plug-in your custom templates here --> 
<xsl:template match="/">
  <!-- Call original code from the imported stylesheet -->
  <xsl:apply-imports/>

  <!-- Call custom templates for the ToC -->
  <xsl:call-template name="etoc"/>
</xsl:template>

<!-- Template for creating auxiliary ToC file -->
<xsl:template name="etoc">
  <xsl:call-template name="write.chunk">
    <xsl:with-param name="filename" select="'toc.xml'"/>
    <xsl:with-param name="method" select="'xml'"/>
    <xsl:with-param name="encoding" select="'utf-8'"/>
    <xsl:with-param name="indent" select="'yes'"/>
    <xsl:with-param name="content">

      <!-- Get the title of the root element -->
      <xsl:variable name="title">
        <xsl:apply-templates select="/*" mode="title.markup"/>
      </xsl:variable>
    
      <!-- Get HTML filename for the root element -->
      <xsl:variable name="href">
        <xsl:call-template name="href.target.with.base.dir">
          <xsl:with-param name="object" select="/*"/>
        </xsl:call-template>
      </xsl:variable>
      
      <!-- Create root element of ToC file -->
      <toc label="{$title}" topic="{$href}">
        <!-- Get ToC for all children of the root element -->
        <xsl:apply-templates select="/*/*" mode="etoc"/>
      </toc>
      
    </xsl:with-param>
  </xsl:call-template>
</xsl:template>

<!-- Template which converts all DocBook containers into 
     one entry in the ToC file -->
<xsl:template match="book|part|reference|preface|chapter|
                     bibliography|appendix|article|glossary|
                     section|sect1|sect2|sect3|sect4|sect5|
                     refentry|colophon|bibliodiv|index" 
              mode="etoc">
  <!-- Get the title of the current element -->
  <xsl:variable name="title">
    <xsl:apply-templates select="." mode="title.markup"/>
  </xsl:variable>

  <!-- Get HTML filename for the current element -->
  <xsl:variable name="href">
    <xsl:call-template name="href.target.with.base.dir"/>
  </xsl:variable>

  <!-- Create ToC entry for the current node and process its 
       container-type children further -->
  <topic label="{$title}" href="{$href}">
    <xsl:apply-templates select="part|reference|preface|chapter|
                                 bibliography|appendix|article|
                                 glossary|section|sect1|sect2|
                                 sect3|sect4|sect5|refentry|
                                 colophon|bibliodiv|index" 
                         mode="etoc"/>
  </topic>

</xsl:template>

<!-- Default processing in the etoc mode is no processing -->
<xsl:template match="text()" mode="etoc"/>


<xsl:template name="href.target.with.base.dir">
  <xsl:param name="object" select="."/>
  <xsl:value-of select="$base.dir"/>
  <xsl:call-template name="href.target">
    <xsl:with-param name="object" select="$object"/>
  </xsl:call-template>
</xsl:template>

</xsl:stylesheet>