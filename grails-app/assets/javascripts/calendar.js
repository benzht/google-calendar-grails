//This is a javascript file with its top level require directives
// x require fullcalendar.css
//= require fullcalendar.js
//= require moment.js
$(document).ready(function() {
    $("#calendar").fullCalendar({
        events: 'list.json',
        header: {
            left: 'prev,next today',
            center: 'title',
            right: 'month,agendaWeek,agendaDay'
        }
    });

});