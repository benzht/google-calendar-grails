package com.craigburke


import static org.joda.time.DateTimeConstants.*
import grails.plugin.spock.*
import grails.test.mixin.*

import org.joda.time.*

import spock.lang.*

@TestFor(EventService)
@Mock(Event)
class EventServiceSpec extends Specification {

    @Shared DateTime now
    @Shared DateTime mondayNextWeek
    @Shared DateTime wednesdayNextWeek
    @Shared DateTime fridayNextWeek
    @Shared DateTime mondayAfterNext

    @Shared Event mwfEvent
    @Shared Event biWeeklyEvent


    void setupSpec() {
        now = new DateTime()
        mondayNextWeek = new DateTime().plusWeeks(1).withDayOfWeek(MONDAY).withTime(0,0,0,0)
        wednesdayNextWeek = mondayNextWeek.withDayOfWeek(WEDNESDAY)
        fridayNextWeek = mondayNextWeek.withDayOfWeek(FRIDAY)
        mondayAfterNext = mondayNextWeek.plusWeeks(1)

        mwfEvent = new Event(
                title: 'Repeating MWF Event',
                startTime: mondayNextWeek.toDate(),
                endTime: mondayNextWeek.plusHours(1).toDate(),
                location: "Regular location",
                recurType: EventRecurType.WEEKLY,
                isRecurring: true,
                recurDaysOfWeek: [MONDAY, WEDNESDAY, FRIDAY]
        )

//        biWeeklyEvent = new Event(mwfEvent.properties)	// FIXME This does not work! - why
		biWeeklyEvent = new Event(
			title: 'Repeating MWF Event',
			startTime: mondayNextWeek.toDate(),
			endTime: mondayNextWeek.plusHours(1).toDate(),
			location: "Regular location",
			recurType: EventRecurType.WEEKLY,
			isRecurring: true,
			recurDaysOfWeek: [MONDAY, WEDNESDAY, FRIDAY]
		)
		assert biWeeklyEvent == mwfEvent
        biWeeklyEvent.recurInterval = 2
    }

    @Unroll("next occurance of weekly event after #afterDate")
    void "next occurrence of a weekly event without excluded days"() {
        expect:
            service.findNextOccurrence(event, afterDate.toDate()) == expectedResult.toDate()

        where:
            event    | afterDate         | expectedResult
            mwfEvent | now               | mondayNextWeek
            mwfEvent | mondayNextWeek    | wednesdayNextWeek
            mwfEvent | wednesdayNextWeek | fridayNextWeek
    }

    @Unroll("next occurence of bi-weekly event after #afterDate")
    void "next occurrence of a bi-weekly event"() {
        expect:
        	service.findNextOccurrence(event, afterDate.toDate()) == expectedResult.toDate()

        where:
	        event         | afterDate           | expectedResult
	        biWeeklyEvent | now                 | mondayNextWeek
	        biWeeklyEvent | mondayNextWeek      | wednesdayNextWeek
	        biWeeklyEvent | wednesdayNextWeek   | fridayNextWeek
	        biWeeklyEvent | fridayNextWeek      | mondayNextWeek.plusWeeks(2)
	        biWeeklyEvent | mondayAfterNext     | mondayNextWeek.plusWeeks(2)
    }


    @Unroll("next occurence of weekly event after #afterDate with exclusion")
    void "next occurrence of a weekly event with an exclusion of next monday"() {
        setup:
            mwfEvent.addToExcludeDays(mondayNextWeek.toDate())

        expect:
            service.findNextOccurrence(event, afterDate.toDate()) == expectedResult.toDate()


        where:
            event    | afterDate           | expectedResult
            mwfEvent | now                 | wednesdayNextWeek
            mwfEvent | mondayNextWeek      | wednesdayNextWeek
            mwfEvent | wednesdayNextWeek   | fridayNextWeek
    }

    void "deleting an instance of a daily recurring event"() {
        def event = new Event(
                title: 'Repeating MWF Event',
                startTime: mondayNextWeek.toDate(),
                endTime: mondayNextWeek.plusHours(1).toDate(),
                location: "Regular location",
                recurType: EventRecurType.DAILY,
                isRecurring: true
        )

        when:
        	event.addToExcludeDays(mondayNextWeek.toDate())

        then:
        	def x= service.findNextOccurrence(event, mondayNextWeek.minusDays(1).toDate())
			x== mondayNextWeek.plusDays(1).toDate()
    }


    void "deleting an instance of a bi-daily recurring event"() {
        def event = new Event(
                title: 'Repeating MWF Event',
                startTime: mondayNextWeek.toDate(),
                endTime: mondayNextWeek.plusHours(1).toDate(),
                location: "Regular location",
                recurInterval: 2,
                recurType: EventRecurType.DAILY,
                isRecurring: true
        )

        when:
        	event.addToExcludeDays(mondayNextWeek.toDate())

        then:
        	service.findNextOccurrence(event, mondayNextWeek.minusDays(1).toDate()) == mondayNextWeek.plusDays(2).toDate()
    }

    @Unroll("next occurence of daily event after #afterDate")
    void "repeating daily event"() {
        def event = new Event(
                title: 'Repeating Daily Event',
                startTime: now.withTime(0, 0, 0, 0).toDate(),
                endTime: now.withTime(0, 0, 0, 0).plusHours(1).toDate(),
                location: "Regular location",
                recurInterval: 1,
                recurType: EventRecurType.DAILY,
                isRecurring: true
        )

        expect:
        service.findNextOccurrence(event, afterDate.toDate()) == afterDate.plusDays(1).withTime(0,0,0,0).toDate()


        where:
        afterDate << [now, mondayNextWeek, wednesdayNextWeek]
    }

    @Unroll("next occurrence of event every other day after #afterDate")
	void "repeating every other day event"() {
        def event = new Event(
                title: 'Repeating Daily Event',
                startTime: mondayNextWeek.withTime(0, 0, 0, 0).toDate(),
                endTime: mondayNextWeek.withTime(0, 0, 0, 0).plusHours(1).toDate(),
                location: "Regular location",
                recurInterval: 2,
                recurType: EventRecurType.DAILY,
                isRecurring: true
        )

        expect:
        service.findNextOccurrence(event, date) == expectedResult


        where:
        date                                | expectedResult
        mondayNextWeek.toDate()             | mondayNextWeek.plusDays(2).toDate()
        mondayNextWeek.plusDays(1).toDate() | mondayNextWeek.plusDays(2).toDate()
        wednesdayNextWeek.toDate()          | wednesdayNextWeek.plusDays(2).toDate()

    }

    void "edit duration of all repeating events"()  {
        def event = new Event(
                title: 'Repeating Daily Event',
                startTime: mondayNextWeek.withTime(0, 0, 0, 0).toDate(),
                endTime: mondayNextWeek.withTime(0, 0, 0, 0).plusHours(1).toDate(),
                location: "Regular location",
                recurInterval: 2,
                recurType: EventRecurType.DAILY,
                isRecurring: true
        ).save(flush: true)

        when:
        def result = service.updateEvent(event, EventRecurActionType.ALL, occurenceStartTime, occurenceEndTime, null)

        then:
        result.error == null
        event.startTime == startTime
        event.endTime == endTime

        where:
        occurenceStartTime                                              | occurenceEndTime                                                         || startTime                                                  | endTime
        mondayNextWeek.withTime(0, 0, 0, 0).toDate()                    | mondayNextWeek.withTime(0, 0, 0, 0).plusHours(2).toDate()                || mondayNextWeek.withTime(0, 0, 0, 0).toDate()               | mondayNextWeek.withTime(0, 0, 0, 0).plusHours(2).toDate()
        mondayNextWeek.plusWeeks(1).withTime(0, 0, 0, 0).toDate()       | mondayNextWeek.plusWeeks(1).withTime(0, 0, 0, 0).plusHours(2).toDate()   || mondayNextWeek.withTime(0, 0, 0, 0).toDate()               | mondayNextWeek.withTime(0, 0, 0, 0).plusHours(2).toDate()
        wednesdayNextWeek.withTime(3, 30, 0, 0).toDate()                | wednesdayNextWeek.withTime(4, 30, 0, 0).toDate()                         || mondayNextWeek.withTime(3, 30, 0, 0).toDate()              | mondayNextWeek.withTime(4, 30, 0, 0).toDate()

    }

    void "edit individual repeating events"()  {
        def event = new Event(
                title: 'Repeating Daily Event',
                startTime: mondayNextWeek.withTime(0, 0, 0, 0).toDate(),
                endTime: mondayNextWeek.withTime(0, 0, 0, 0).plusHours(1).toDate(),
                location: "Regular location",
                recurInterval: 2,
                recurType: EventRecurType.DAILY,
                isRecurring: true
        ).save(flush: true)

        when:
        def result = service.updateEvent(event, EventRecurActionType.OCCURRENCE, occurenceStartTime, occurenceEndTime, params)

        then:
        def newEvent = Event.findByStartTimeAndEndTimeAndTitle(occurenceStartTime, occurenceEndTime, params.title)

        result.error == null

        event.excludeDays.contains(new DateTime(occurenceStartTime).withTime(0, 0, 0, 0).toDate())
        newEvent != null
        newEvent.isRecurring == false

        where:
        occurenceStartTime                                           | occurenceEndTime                                                         | params
        mondayNextWeek.withTime(0, 0, 0, 0).toDate()                 | mondayNextWeek.withTime(0, 0, 0, 0).plusHours(2).toDate()                | [title: 'FOO']
        mondayNextWeek.plusWeeks(1).withTime(0, 0, 0, 0).toDate()    | mondayNextWeek.plusWeeks(1).withTime(0, 0, 0, 0).plusHours(2).toDate()   | [title: 'BAR']
    }

    void "edit following repeating events"()  {
        def event = new Event(
                title: 'Repeating Daily Event',
                startTime: mondayNextWeek.withTime(0, 0, 0, 0).toDate(),
                endTime: mondayNextWeek.withTime(0, 0, 0, 0).plusHours(1).toDate(),
                location: "Regular location",
                recurInterval: 2,
                recurType: EventRecurType.DAILY,
                isRecurring: true
        ).save(flush: true)

        when:
        def result = service.updateEvent(event, EventRecurActionType.FOLLOWING, occurenceStartTime, occurenceEndTime, params)

        then:
        def followingEvent = Event.findByStartTimeAndEndTimeAndTitle(occurenceStartTime, occurenceEndTime, params.title)

        result.error == null
        event.recurUntil == new DateTime(occurenceStartTime).withTime(0,0,0,0).toDate()
        followingEvent != null
        followingEvent.isRecurring == true
        followingEvent.startTime == occurenceStartTime
        followingEvent.endTime == occurenceEndTime
        followingEvent.recurUntil == null

        where:
        occurenceStartTime                                           | occurenceEndTime                                                         | params
        mondayNextWeek.withTime(0, 0, 0, 0).toDate()                 | mondayNextWeek.withTime(0, 0, 0, 0).plusHours(2).toDate()                | [title: 'FOO']
        mondayNextWeek.plusWeeks(1).withTime(0, 0, 0, 0).toDate()    | mondayNextWeek.plusWeeks(1).withTime(0, 0, 0, 0).plusHours(2).toDate()   | [title: 'BAR']
    }


    void "delete of all repeating events"()  {
        def event = new Event(
                title: 'Repeating Daily Event',
                startTime: mondayNextWeek.withTime(0, 0, 0, 0).toDate(),
                endTime: mondayNextWeek.withTime(0, 0, 0, 0).plusHours(1).toDate(),
                location: "Regular location",
                recurInterval: 2,
                recurType: EventRecurType.DAILY,
                isRecurring: true
        ).save(flush: true)

        when:
        def result = service.deleteEvent(event, EventRecurActionType.ALL, occurenceStartTime)

        then:
        result.error == null
        Event.get(event.id) == null

        where:
        occurenceStartTime << [mondayNextWeek.withTime(0, 0, 0, 0).toDate(),  mondayNextWeek.plusWeeks(1).withTime(0, 0, 0, 0).toDate()]
    }

    void "delete an individual instance of a repeating event"()  {
        def event = new Event(
                title: 'Repeating Daily Event',
                startTime: mondayNextWeek.withTime(0, 0, 0, 0).toDate(),
                endTime: mondayNextWeek.withTime(0, 0, 0, 0).plusHours(1).toDate(),
                location: "Regular location",
                recurInterval: 2,
                recurType: EventRecurType.DAILY,
                isRecurring: true
        ).save(flush: true)

        when:
        def result = service.deleteEvent(event, EventRecurActionType.OCCURRENCE, occurenceStartTime)

        then:
        result.error == null
        event.excludeDays.contains(new DateTime(occurenceStartTime).withTime(0,0,0,0).toDate())

        where:
        occurenceStartTime << [mondayNextWeek.withTime(0, 0, 0, 0).toDate(),  mondayNextWeek.plusWeeks(1).withTime(0, 0, 0, 0).toDate()]
    }

    void "delete all following instances of a repeating event"()  {
        def event = new Event(
                title: 'Repeating Daily Event',
                startTime: mondayNextWeek.withTime(0, 0, 0, 0).toDate(),
                endTime: mondayNextWeek.withTime(0, 0, 0, 0).plusHours(1).toDate(),
                location: "Regular location",
                recurInterval: 2,
                recurType: EventRecurType.DAILY,
                isRecurring: true
        ).save(flush: true)

        when:
        def result = service.deleteEvent(event, EventRecurActionType.FOLLOWING, occurenceStartTime)

        then:
        result.error == null
        event.recurUntil == new DateTime(occurenceStartTime).withTime(0, 0, 0, 0).toDate()

        where:
        occurenceStartTime << [mondayNextWeek.withTime(0, 0, 0, 0).toDate(),  mondayNextWeek.plusWeeks(1).withTime(0, 0, 0, 0).toDate()]
    }


}
