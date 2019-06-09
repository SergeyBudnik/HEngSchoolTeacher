package com.bdev.hengschoolteacher.ui.activities.profile

import android.annotation.SuppressLint
import android.view.View
import com.bdev.hengschoolteacher.R
import com.bdev.hengschoolteacher.service.LessonStatusService
import com.bdev.hengschoolteacher.service.LessonsService
import com.bdev.hengschoolteacher.service.teacher.TeacherStorageService
import com.bdev.hengschoolteacher.service.UserPreferencesService
import com.bdev.hengschoolteacher.service.profile.ProfileService
import com.bdev.hengschoolteacher.ui.activities.BaseActivity
import com.bdev.hengschoolteacher.ui.utils.HeaderElementsUtils
import com.bdev.hengschoolteacher.ui.views.app.AppMenuView
import kotlinx.android.synthetic.main.activity_profile_payment.*
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EActivity

@SuppressLint("Registered")
@EActivity(R.layout.activity_profile_payment)
open class ProfilePaymentActivity : BaseActivity() {
    @Bean
    lateinit var userPreferencesService: UserPreferencesService
    @Bean
    lateinit var teacherStorageService: TeacherStorageService
    @Bean
    lateinit var profileService: ProfileService
    @Bean
    lateinit var lessonStatusService: LessonStatusService
    @Bean
    lateinit var lessonsService: LessonsService

    private var calendarEnabled = false

    @AfterViews
    fun init() {
        profilePaymentHeaderView
                .setLeftButtonAction { profilePaymentMenuLayoutView.openMenu() }
                .setFirstRightButtonAction { toggleCalendar() }
                .setFirstRightButtonColor(getHeaderButtonColor(calendarEnabled))

        profilePaymentMenuLayoutView.setCurrentMenuItem(AppMenuView.Item.MY_PROFILE)

        val me = profileService.getMe()

        if (me != null) {
            profilePaymentWeekSelectionBarView.init { weekIndex ->
                profilePaymentTeacherSalaryView.init(me.id, weekIndex)
            }
        }
    }

    private fun toggleCalendar() {
        calendarEnabled = !calendarEnabled

        profilePaymentHeaderView.setFirstRightButtonColor(getHeaderButtonColor(calendarEnabled))

        profilePaymentWeekSelectionBarView.visibility = if (calendarEnabled) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun getHeaderButtonColor(enabled: Boolean): Int {
        return HeaderElementsUtils.getColor(this, enabled)
    }
}
