package com.bdev.hengschoolteacher.ui.views.app.lesson

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.bdev.hengschoolteacher.R
import com.bdev.hengschoolteacher.data.school.group.Group
import com.bdev.hengschoolteacher.data.school.group.Lesson
import com.bdev.hengschoolteacher.data.school.lesson.LessonStatus
import com.bdev.hengschoolteacher.data.school.student.Student
import com.bdev.hengschoolteacher.data.school.student.StudentAttendanceType
import com.bdev.hengschoolteacher.service.*
import kotlinx.android.synthetic.main.view_lesson_row.view.*
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EViewGroup

@EViewGroup(R.layout.view_lesson_row)
open class LessonRowView : LinearLayout {
    @Bean
    lateinit var studentsAttendancesService: StudentsAttendancesService
    @Bean
    lateinit var lessonsService: LessonsService
    @Bean
    lateinit var lessonsAttendancesService: LessonsAttendancesService
    @Bean
    lateinit var lessonStatusService: LessonStatusService
    @Bean
    lateinit var lessonStateService: LessonStateService

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun bind(group: Group, lesson: Lesson, students: List<Student>, weekIndex: Int) {
        lessonRowGroupIconView.bind(group)

        lessonRowStartTimeView.text = context.getString(lesson.startTime.translationId)
        lessonRowFinishTimeView.text = context.getString(lesson.finishTime.translationId)

        listOf(lessonRowStudent1View, lessonRowStudent2View, lessonRowStudent3View, lessonRowStudent4View, lessonRowStudent5View, lessonRowStudent6View)
                .forEachIndexed { index, it ->
                    val student = if (students.size > index) { students[index] } else { null }
                    val attendance = if (student != null) { studentsAttendancesService.getAttendance(lesson.id, student.login, weekIndex) } else { null }

                    setStudentIcon(student, attendance, it)
                }

        setLessonIcon(lesson, weekIndex)
        setLessonStatus(lesson, weekIndex)
    }

    private fun setStudentIcon(student: Student?, attendanceType: StudentAttendanceType?, studentView: ImageView) {
        val icon = if (student == null) {
            R.drawable.ic_user_linear
        } else {
            R.drawable.ic_user_filled
        }

        val color = if (student == null) {
            R.color.fill_text_basic
        } else {
            when (attendanceType) {
                null -> R.color.fill_text_basic
                StudentAttendanceType.VISITED -> R.color.fill_text_basic_positive
                StudentAttendanceType.VALID_SKIP -> R.color.fill_text_basic_warning
                StudentAttendanceType.INVALID_SKIP -> R.color.fill_text_basic_negative
                StudentAttendanceType.FREE_LESSON -> R.color.fill_text_basic_action_link
            }
        }

        studentView.setImageDrawable(resources.getDrawable(icon))
        studentView.setColorFilter(resources.getColor(color))
    }

    private fun setLessonIcon(lesson: Lesson, weekIndex: Int) {
        val isLessonFinished = lessonStateService.isLessonFinished(
                lessonId = lesson.id,
                weekIndex = weekIndex
        )

        val isLessonFilled = lessonsAttendancesService.isLessonAttendanceFilled(
                lessonId = lesson.id,
                weekIndex = weekIndex
        )

        val isLessonMarked = lessonStatusService.getLessonStatus(
                lessonId = lesson.id,
                lessonTime = lessonsService.getLessonStartTime(lesson.id, weekIndex)
        ) != null

        val iconId = if (isLessonFinished) {
            if (isLessonFilled) {
                R.drawable.ic_lesson_status_finished
            } else {
                R.drawable.ic_lesson_status_unknown
            }
        } else {
            R.drawable.ic_lesson_status_not_finished
        }

        val colorId = if (isLessonFinished) {
            if (isLessonFilled && isLessonMarked) {
                R.color.fill_text_basic_positive
            } else {
                R.color.fill_text_basic_negative
            }
        } else {
            R.color.fill_text_basic_warning
        }

        lessonRowStatusView.setImageDrawable(resources.getDrawable(iconId))
        lessonRowStatusView.setColorFilter(resources.getColor(colorId), PorterDuff.Mode.SRC_IN)
    }

    private fun setLessonStatus(lesson: Lesson, weekIndex: Int) {
        val lessonStatus = lessonStatusService.getLessonStatus(lesson.id, lessonsService.getLessonStartTime(lesson.id, weekIndex))

        lessonRowLessonStatusView.visibility = if (lessonStatus != null) { View.VISIBLE } else { View.GONE }

        if (lessonStatus != null) {
            lessonRowLessonStatusView.text = when (lessonStatus.type) {
                LessonStatus.Type.FINISHED -> "Проведено"
                LessonStatus.Type.CANCELED -> "Отменено"
                LessonStatus.Type.MOVED -> "Перенесено"
            }
        }
    }
}
