package com.deniz0706.smartalarmtest.alarm

import com.deniz0706.smartalarmtest.model.Alarm
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class AlarmSchedulerTest {
    @Test fun nextOneShotMovesToTomorrowAfterTimePassed() {
        val now=ZonedDateTime.of(2026,9,30,9,0,0,0,ZoneId.of("UTC"))
        assertEquals(1, AlarmScheduler.nextOccurrence(Alarm(hour=8,minute=0),now).dayOfMonth)
    }
    @Test fun repeatingAlarmSelectsConfiguredWeekday() {
        val now=ZonedDateTime.of(2026,9,30,9,0,0,0,ZoneId.of("UTC")) // Wednesday
        assertEquals(5, AlarmScheduler.nextOccurrence(Alarm(hour=7,repeatDays=setOf(1)),now).dayOfMonth)
    }
}
