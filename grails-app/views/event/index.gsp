<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'event.label', default: 'Event')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    <asset:javascript src="calendar.js"/>
	<asset:stylesheet href="calendar.css"/>
    </head>
    <body>
        <a href="#list-event" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
		    <ul>
		        <li><a href="${createLink(uri: '/')}" class="home">Home</a></li>
		        <li><g:link action="index" class="calendar">Calendar</g:link></li>
		        <li><g:link action="create" class="create">New Event</g:link></li>
		    </ul>
		</div>

	    <div id="calendar"></div>

    </body>
</html>