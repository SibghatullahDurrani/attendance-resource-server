package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.projections.organization.OrganizationTimeZone;
import com.main.face_recognition_resource_server.services.organization.OrganizationService;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class MarkAbsentScheduler {
    private final TaskScheduler taskScheduler;
    private final OrganizationService organizationService;
    private final AttendanceService attendanceService;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public MarkAbsentScheduler(OrganizationService organizationService, AttendanceService attendanceService) {
        this.organizationService = organizationService;
        this.attendanceService = attendanceService;

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("MarkAbsentScheduler-");
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    @PostConstruct
    public void init() {
        List<OrganizationTimeZone> organizationTimeZones = organizationService.getAllOrganizationTimeZonesWithIds();
        organizationTimeZones.forEach(this::initializeTodaysAttendance);
        organizationTimeZones.forEach(this::scheduleMarkAbsentTask);
    }

    private void initializeTodaysAttendance(OrganizationTimeZone organizationTimeZone) {
        ZoneId zoneId = ZoneId.of(organizationTimeZone.getTimeZone());
        ZonedDateTime startOfToday = ZonedDateTime.now(zoneId).toLocalDate().atStartOfDay(zoneId);
        Instant startOfTodayInstant = startOfToday.toInstant();
        attendanceService.markAbsentOfUsersOfOrganizationForDate(organizationTimeZone.getOrganizationId(), startOfTodayInstant, startOfToday.getDayOfWeek());
    }

    private void scheduleMarkAbsentTask(OrganizationTimeZone organizationTimeZone) {
        ZoneId zoneId = ZoneId.of(organizationTimeZone.getTimeZone());
        CronTrigger cronTrigger = new CronTrigger("1 0 0 * * *", zoneId);
        cancelScheduleMarkAbsentTask(organizationTimeZone.getOrganizationId());
        ScheduledFuture<?> scheduledTask = taskScheduler.schedule(() -> markAbsentTask(organizationTimeZone.getOrganizationId(), zoneId), cronTrigger);
        scheduledTasks.put(organizationTimeZone.getOrganizationId(), scheduledTask);
    }

    private void markAbsentTask(Long organizationId, ZoneId zoneId) {
        ZonedDateTime startOfToday = ZonedDateTime.now(zoneId).toLocalDate().atStartOfDay(zoneId);
        attendanceService.markAbsentOfUsersOfOrganizationForDate(organizationId, startOfToday.toInstant(), startOfToday.getDayOfWeek());
    }

    private void cancelScheduleMarkAbsentTask(Long organizationId) {
        ScheduledFuture<?> scheduledFuture = scheduledTasks.get(organizationId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledTasks.remove(organizationId);
        }
    }
}
