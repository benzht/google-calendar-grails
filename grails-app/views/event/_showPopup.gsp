<%@ page import="org.joda.time.Instant" %>

<div class="eventPopup">

<div class="qtip-titlebar">${eventInstance.title}</div>
<p class="date">
    <g:formatDate date="${new Instant(occurrenceStart).toDate()}" format="dd-MM-yyyy, HH:mm"/>  –
    <g:formatDate date="${new Instant(occurrenceEnd).toDate()}" format="dd-MM-yyyy, HH:mm"/>
</p>
<p>
    <g:link action="show" id="${eventInstance.id}" params="[occurrenceStart: start, occurrenceEnd: end]">More details »</g:link>
</p>
</div>