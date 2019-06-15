package com.bdev.hengschoolteacher.service.teacher

import com.bdev.hengschoolteacher.dao.TeachersDao
import com.bdev.hengschoolteacher.dao.TeachersModel
import com.bdev.hengschoolteacher.data.school.teacher.Teacher
import com.bdev.hengschoolteacher.service.UserPreferencesService
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EBean

@EBean
open class TeacherStorageService {
    @Bean
    lateinit var teachersDao: TeachersDao
    @Bean
    lateinit var userPreferencesService: UserPreferencesService

    fun getAllTeachers(): List<Teacher> {
        return teachersDao.readValue().teachers
    }

    fun getTeacherById(id: Long): Teacher? {
        return teachersDao.readValue().teachers.find { it.id == id }
    }

    fun getTeacherByLogin(login: String): Teacher? {
        return teachersDao.readValue().teachers.find { it.login == login }
    }

    fun setTeachers(teachers: List<Teacher>) {
        teachersDao.writeValue(TeachersModel(teachers))
    }
}