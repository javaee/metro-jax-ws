<!--
  writes table of contents, by using <section> tags as the key.

  this tag should be used to surround the entire content, like:

  <body><content>
    .... HTML ....
  </content></body>
-->
<j:jelly xmlns:j="jelly:core" xmlns:x="jelly:xml" xmlns:jsl="jelly:jsl" xmlns:d="jelly:define">
  <x:parse var="tocTree">
    <j:scope>
      <ROOT>
        <j:set var="mode" value="toc" />
        <d:invokeBody />
      </ROOT>
    </j:scope>
  </x:parse>
  <jsl:stylesheet var="tocStylesheet">
    <jsl:template match="TOC">
      <x:set var="href" select="string(@href)" />
      <x:set var="title" select="string(@title)" />
      <x:set var="prefix" select="string(@prefix)" />
      <li>${prefix} <a href="#${href}">${title}</a></li>
      <ol class="toc">
        <jsl:applyTemplates select="TOC" />
      </ol>
    </jsl:template>
  </jsl:stylesheet>
  <h2>Contents</h2>
  <ol class="toc">
    <jsl:style stylesheet="${tocStylesheet}" select="$tocTree" />
  </ol>
  <j:set var="prefix" value=""/>
  <d:invokeBody />
</j:jelly>