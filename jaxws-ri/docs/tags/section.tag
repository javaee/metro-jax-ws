<!-- section marker. Takes "title" attribute -->
<j:jelly xmlns:j="jelly:core" xmlns:x="jelly:xml" xmlns:jsl="jelly:jsl" xmlns:d="jelly:define">
	<j:set var="sectionNumber" value="${sectionNumber+1}" scope="parent" />
	<j:set var="prefix" value="${prefix}${sectionNumber}." />
	<j:set var="sectionNumber" value="0" /> <!-- start a new sequence for children -->
	
	<!-- name escaped for anchor and file names -->
	<j:set var="safeName" value="${title.replaceAll('[^A-Za-z0-9]','_')}" />
	
	<j:choose>
		<j:when test="${mode=='toc'}">
			<!-- generate XML fragment for TOC -->
			<TOC href="${safeName}" title="${title}" prefix="${prefix}">
				<d:invokeBody />
			</TOC>
		</j:when>
		<j:otherwise>
			<a name="${safeName}">
				<x:element name="h${depth+2}">
					<a href="#${safeName}">${prefix}</a> ${title}
				</x:element>
			</a>
			
			<j:set var="depth" value="${depth+1}" />
	  	<d:invokeBody />
			<j:set var="depth" value="${depth-1}" />
		</j:otherwise>
	</j:choose>
</j:jelly>
