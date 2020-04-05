package com.bdev.hengschoolteacher.service.alerts.monitoring

import com.bdev.hengschoolteacher.service.StudentPaymentsDeptService
import com.bdev.hengschoolteacher.service.StudentsService
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EBean

@EBean
open class AlertsMonitoringStudentsService {
    @Bean
    lateinit var studentsService: StudentsService
    @Bean
    lateinit var studentPaymentsDeptService: StudentPaymentsDeptService

    fun haveAlerts(): Boolean {
        return studentsService
                .getAllStudents()
                .map { studentPaymentsDeptService.getStudentDept(it.login) > 0 }
                .fold(false) { amount, value -> amount or value }
    }
}
