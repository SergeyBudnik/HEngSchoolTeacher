package com.bdev.hengschoolteacher.services.lessons

import com.bdev.hengschoolteacher.data.school.DayOfWeek
import com.bdev.hengschoolteacher.data.school.Time
import com.bdev.hengschoolteacher.data.school.group.Group
import com.bdev.hengschoolteacher.data.school.group.GroupAndLesson
import com.bdev.hengschoolteacher.data.school.group.Lesson
import com.bdev.hengschoolteacher.data.school.student.Student
import com.bdev.hengschoolteacher.services.groups.GroupsStorageService
import com.bdev.hengschoolteacher.services.groups.GroupsStorageServiceImpl
import com.bdev.hengschoolteacher.services.students.StudentsStorageService
import com.bdev.hengschoolteacher.services.students.StudentsStorageServiceImpl
import com.bdev.hengschoolteacher.utils.TimeUtils
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EBean
import java.util.*
import kotlin.collections.ArrayList

@EBean
open class LessonsService {
    @Bean(GroupsStorageServiceImpl::class)
    lateinit var groupsStorageService: GroupsStorageService
    @Bean(StudentsStorageServiceImpl::class)
    lateinit var studentsStorageService: StudentsStorageService

    fun getLesson(lessonId: Long): GroupAndLesson? {
        for (group in groupsStorageService.getAll()) {
            for (lesson in group.lessons) {
                if (lesson.id == lessonId) {
                    return GroupAndLesson(
                            group = group,
                            lesson = lesson
                    )
                }
            }
        }

        return null
    }

    fun getAllLessons(weekIndex: Int): List<GroupAndLesson> {
        return getLessonsByCondition { _, lesson -> lessonTimeMatches(lesson, weekIndex) }
    }

    fun getLessonStudents(lessonId: Long, weekIndex: Int): List<Student> {
        val groupAndLesson = getLesson(lessonId) ?: return emptyList()

        val lessonStartTime = getLessonStartTime(lessonId, weekIndex)
        val lessonFinishTime = getLessonFinishTime(lessonId, weekIndex)

        return studentsStorageService
                .getAll()
                .filter { student -> student.studentGroups
                        .filter { it.groupId == groupAndLesson.group.id }
                        .filter { it.startTime < lessonStartTime }
                        .filter { lessonFinishTime < it.finishTime }
                        .any()
                }
    }

    fun getTeacherLessons(teacherLogin: String, weekIndex: Int): List<GroupAndLesson> {
        return getLessonsByCondition { _, lesson ->
            val teacherMatches = lesson.teacherLogin == teacherLogin
            val timeMatches = lessonTimeMatches(lesson, weekIndex)

            teacherMatches && timeMatches
        }
    }

    // ToDo: add info about time
    fun getStudentLessons(studentLogin: String): List<GroupAndLesson> {
        val student = studentsStorageService.getByLogin(studentLogin) ?: throw RuntimeException()

        return getLessonsByCondition { group, _ -> student.studentGroups.map { it.groupId }.contains(group.id) }
    }

    private fun getLessonsByCondition(condition: (Group, Lesson) -> Boolean): List<GroupAndLesson> {
        val lessons = ArrayList<GroupAndLesson>()

        groupsStorageService
                .getAll()
                .forEach { group ->
                    group.lessons
                            .filter { lesson -> condition.invoke(group, lesson) }
                            .forEach { lesson -> lessons.add(GroupAndLesson(group, lesson)) }
                }

        return lessons
    }

    fun getLessonsAmountInMonth(groupId: Long, month: Int): Int {
        val calendar = Calendar.getInstance()

        with (calendar) {
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 0)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE,      0)
            set(Calendar.SECOND,      0)
            set(Calendar.MILLISECOND, 0)
        }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val group = groupsStorageService.getById(groupId) ?: throw RuntimeException()

        var lessonsAmount = 0

        for (i in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, i)

            val calendarDayOfWeek = TimeUtils().getDayOfWeek(calendar)

            for (lesson in group.lessons) {
                if (lesson.day == calendarDayOfWeek) {
                    lessonsAmount++
                }
            }
        }

        return lessonsAmount
    }

    fun getLessonStartTime(lessonId: Long, weekIndex: Int): Long {
        val groupAndLesson = getLesson(lessonId) ?: throw RuntimeException()

        return getLessonTime(
                day = groupAndLesson.lesson.day,
                time = groupAndLesson.lesson.startTime,
                weekIndex = weekIndex
        )
    }

    fun getLessonFinishTime(lessonId: Long, weekIndex: Int): Long {
        val groupAndLesson = getLesson(lessonId) ?: throw RuntimeException()

        return getLessonTime(
                day = groupAndLesson.lesson.day,
                time = groupAndLesson.lesson.finishTime,
                weekIndex = weekIndex
        )
    }

    private fun lessonTimeMatches(lesson: Lesson, weekIndex: Int): Boolean {
        val lessonStartTime = getLessonStartTime(lesson.id, weekIndex)

        val creationTimeMatches = lesson.creationTime <= lessonStartTime
        val deactivationTimeMatches = (lesson.deactivationTime == null) || lessonStartTime <= lesson.deactivationTime

        return creationTimeMatches && deactivationTimeMatches
    }

    private fun getLessonTime(day: DayOfWeek, time: Time, weekIndex: Int): Long {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.WEEK_OF_YEAR, calendar.get(Calendar.WEEK_OF_YEAR) + weekIndex)
        calendar.set(Calendar.DAY_OF_WEEK, TimeUtils().getCalendayDayOfWeek(day))
        calendar.set(Calendar.HOUR_OF_DAY, TimeUtils().getCalendarHour(time))
        calendar.set(Calendar.MINUTE,      TimeUtils().getCalendarMinute(time))
        calendar.set(Calendar.SECOND,      0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }
}