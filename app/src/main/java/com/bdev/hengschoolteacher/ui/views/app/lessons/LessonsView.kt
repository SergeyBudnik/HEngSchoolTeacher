package com.bdev.hengschoolteacher.ui.views.app.lessons

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.bdev.hengschoolteacher.R
import com.bdev.hengschoolteacher.data.school.DayOfWeek
import com.bdev.hengschoolteacher.data.school.group.Group
import com.bdev.hengschoolteacher.data.school.group.GroupAndLesson
import com.bdev.hengschoolteacher.data.school.group.Lesson
import com.bdev.hengschoolteacher.services.lessons.LessonsService
import com.bdev.hengschoolteacher.services.students.StudentsStorageService
import com.bdev.hengschoolteacher.services.staff.StaffMembersStorageService
import com.bdev.hengschoolteacher.services.students.StudentsStorageServiceImpl
import com.bdev.hengschoolteacher.services.students_attendances.StudentsAttendancesProviderService
import com.bdev.hengschoolteacher.ui.activities.BaseActivity
import com.bdev.hengschoolteacher.ui.activities.lesson.info.LessonInfoActivityData
import com.bdev.hengschoolteacher.ui.activities.lesson.info.LessonInfoActivityLauncher
import com.bdev.hengschoolteacher.ui.adapters.BaseWeekItemsListAdapter
import com.bdev.hengschoolteacher.ui.utils.ViewVisibilityUtils.visibleElseGone
import kotlinx.android.synthetic.main.view_lesson_item.view.*
import kotlinx.android.synthetic.main.view_lessons.view.*
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EViewGroup
import java.util.*

@EViewGroup(R.layout.view_lesson_item)
open class LessonItemView : RelativeLayout {
    companion object {
        const val REQUEST_CODE_LESSON = 1
    }

    @Bean
    lateinit var studentsAttendancesProviderService: StudentsAttendancesProviderService
    @Bean
    lateinit var lessonsService: LessonsService
    @Bean
    lateinit var staffMembersStorageService: StaffMembersStorageService
    @Bean(StudentsStorageServiceImpl::class)
    lateinit var studentsStorageService: StudentsStorageService

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun bind(
            group: Group,
            lesson: Lesson,
            weekIndex: Int,
            showTeacher: Boolean
    ): LessonItemView {
        val students = lessonsService.getLessonStudents(lesson.id, weekIndex)

        lessonItemRowView.bind(group, lesson, students, weekIndex)

        lessonItemTeacherView.text = staffMembersStorageService.getStaffMember(lesson.teacherLogin)?.person?.name ?: ""
        lessonItemTeacherView.visibility = visibleElseGone(visible = showTeacher)

        return this
    }
}

class LessonsListAdapter(
        context: Context,
        private val showTeacher: Boolean
) : BaseWeekItemsListAdapter<GroupAndLesson>(context) {
    private var weekIndex = 0

    fun setWeekIndex(weekIndex: Int) {
        this.weekIndex = weekIndex
    }

    override fun getElementView(item: GroupAndLesson, convertView: View?): View {
        return if (convertView == null || convertView !is LessonItemView) {
            LessonItemView_.build(context)
        } else {
            convertView
        }.bind(
                group = item.group,
                lesson = item.lesson,
                weekIndex = weekIndex,
                showTeacher = showTeacher
        )
    }

    override fun getElementDayOfWeek(item: GroupAndLesson): DayOfWeek {
        return item.lesson.day
    }

    override fun getElementComparator(): Comparator<GroupAndLesson> {
        return GroupAndLesson.getComparator(Calendar.getInstance())
    }
}

@EViewGroup(R.layout.view_lessons)
open class LessonsView : RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private lateinit var adapter: LessonsListAdapter

    private var weekIndex = 0

    fun bind(showTeacher: Boolean) {
        adapter = LessonsListAdapter(
                context = context,
                showTeacher = showTeacher
        )

        lessonsListView.adapter = adapter

        lessonsListView.setOnItemClickListener { _, _, position, _ ->
            adapter.getItem(position).second?.let { groupAndLesson ->
                LessonInfoActivityLauncher.launchAsChild(
                        from = context as BaseActivity,
                        data = LessonInfoActivityData(
                                groupId = groupAndLesson.group.id,
                                lessonId = groupAndLesson.lesson.id,
                                weekIndex = weekIndex
                        ),
                        requestCode = LessonItemView.REQUEST_CODE_LESSON
                )
            }
        }
    }

    fun fill(lessons: List<GroupAndLesson>, weekIndex: Int) {
        this.weekIndex = weekIndex

        adapter.setItems(lessons)
        adapter.setWeekIndex(weekIndex)
        adapter.notifyDataSetChanged()
    }
}
