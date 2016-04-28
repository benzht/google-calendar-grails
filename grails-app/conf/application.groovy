grails.resources.modules = {
	core {
		resource url:'/js/jquery-2.2.0.min.js', disposition: 'head'
	}

	fullCalendar {
		dependsOn 'core'
		resource url:'/js/fullcalendar.js'
		resource url:'/css/fullcalendar.css'
	}

	calendar {
		dependsOn 'fullCalendar'

		resource url: '/js/calendar.js'
		resource url: '/css/calendar.css'

	}

}