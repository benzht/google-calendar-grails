//This is a javascript file with its top level require directives
//= require jquery.qtip.css
//= require jquery.qtip.js
//= require fullcalendar.js
//= require lang/en-ie.js
//= require moment.js
$("#calendar").fullCalendar({
        events: 'list.json',
        header: {
            left: 'prev,next today',
            center: 'title',
            right: 'month,agendaWeek,agendaDay'
        },

        eventRender: function(event, element) {
            $(element).addClass(event.cssClass);

            var occurrenceStart = event.start.time();
            var occurrenceEnd = event.end.time();

            var data = {id: event.id, occurrenceStart: occurrenceStart, occurrenceEnd: occurrenceEnd};

            $(element).qtip({
                content: {
                    text: function(event, api) {
                        $.ajax({
                            url: "show",
                            traditional:true,
                            data: data
                        })
                        .then(function(content) {
                            // Set the tooltip content upon successful retrieval
                            api.set('content.text', content);
                        }, function(xhr, status, error) {
                            // Upon failure... set the tooltip content to the status and error value
                            api.set('content.text', status + ': ' + error);
                        });

                        return 'Loading...'; // Set some initial text
                    }
                },
                show: {
                    event: 'click',
                    solo: true
                },
                hide: {
                    event: 'click'
                },
//                style: {
//                	classes: 'qtip-light'
//                },
                position: {
                    my: 'bottom middle',
                    at: 'top middle',
                    viewport: true
                }
            });
        }
    });