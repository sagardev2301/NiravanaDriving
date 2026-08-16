package com.niravanadriving.app.data.repository

import com.niravanadriving.app.data.models.*

object MockDataRepository {
    
    val currentInstructor = Instructor(
        id = "inst_1",
        authUserId = "auth_1",
        fullName = "Vikram Rathore",
        email = "vikram.rathore@nirvanadrive.test",
        title = "Senior Instructor",
        licenseNumber = "DL-1987-45621",
        profilePhotoUrl = "https://randomuser.me/api/portraits/men/32.jpg"
    )

    val sampleVehicle = Vehicle(
        id = "veh_1",
        instructorId = "inst_1",
        makeModel = "Maruti Swift",
        registrationNumber = "DL 12 AB 1234",
        transmissionType = TransmissionType.MANUAL
    )

    data class InstructorStats(
        val attendanceRate: String,
        val totalClasses: Int,
        val punctuality: String
    )

    val currentInstructorStats = InstructorStats(
        attendanceRate = "98%",
        totalClasses = 240,
        punctuality = "4.8/5"
    )

    data class ActivityHistory(
        val date: String,
        val month: String,
        val classesCount: Int,
        val hoursDriven: Double,
        val distinctRoutes: Int
    )

    val activityHistory = listOf(
        ActivityHistory("24", "AUG", 8, 6.0, 4),
        ActivityHistory("23", "AUG", 6, 4.5, 3),
        ActivityHistory("22", "AUG", 7, 5.2, 5)
    )

    private val students = listOf(
        Student(
            id = "stud_1",
            instructorId = "inst_1",
            fullName = "Rahul Sharma",
            totalSessions = 15
        ).apply { sessionsCompleted = 10 },
        Student(
            id = "stud_2",
            instructorId = "inst_1",
            fullName = "Priya Singh",
            totalSessions = 15,
            phone = "9876543210"
        ).apply { sessionsCompleted = 8; balance = -2000.0 },
        Student(
            id = "stud_3",
            instructorId = "inst_1",
            fullName = "Amit Verma",
            totalSessions = 15
        ).apply { sessionsCompleted = 3 },
        Student(
            id = "stud_4",
            instructorId = "inst_1",
            fullName = "Sanya Khan",
            totalSessions = 15
        ).apply { sessionsCompleted = 5 },
        Student(
            id = "stud_5",
            instructorId = "inst_1",
            fullName = "Vikram J.",
            totalSessions = 15
        ).apply { sessionsCompleted = 2 },
        Student(
            id = "stud_6",
            instructorId = "inst_1",
            fullName = "Sarah Jenkins",
            totalSessions = 10
        ).apply { sessionsCompleted = 5; balance = 0.0 },
        Student(
            id = "stud_7",
            instructorId = "inst_1",
            fullName = "Marcus Reed",
            totalSessions = 10
        ).apply { sessionsCompleted = 2; balance = 1500.0 },
        Student(
            id = "stud_8",
            instructorId = "inst_1",
            fullName = "Elena Lopez",
            totalSessions = 10
        ).apply { sessionsCompleted = 8; balance = 0.0 },
        Student(
            id = "stud_9",
            instructorId = "inst_1",
            fullName = "John Doe",
            totalSessions = 10
        ).apply { sessionsCompleted = 9; balance = 0.0 },
        Student(
            id = "stud_10",
            instructorId = "inst_1",
            fullName = "Alice Smith",
            totalSessions = 10
        ).apply { sessionsCompleted = 3; balance = 0.0 }
    )

    fun getAllStudents(): List<Student> = students

    fun getTomorrowSchedule(): List<Lesson> {
        return listOf(
            Lesson(
                id = "lesson_4",
                instructorId = "inst_1",
                studentId = "stud_4",
                scheduledDate = "2023-10-26",
                scheduledTime = "08:00 AM",
                durationMinutes = 60,
                status = LessonStatus.SCHEDULED,
                pickupLocation = "City Dr, North",
                student = students[3],
                vehicle = sampleVehicle,
                notes = "1 hr"
            ),
            Lesson(
                id = "lesson_5",
                instructorId = "inst_1",
                studentId = "stud_5",
                scheduledDate = "2023-10-26",
                scheduledTime = "09:30 AM",
                durationMinutes = 90,
                status = LessonStatus.SCHEDULED,
                pickupLocation = "Hwy Pr, Downtown",
                student = students[4],
                vehicle = sampleVehicle,
                notes = "1.5 hrs"
            )
        )
    }

    fun getOngoingLesson(): Pair<Lesson, LessonSession> {
        val lesson = Lesson(
            id = "lesson_1",
            instructorId = "inst_1",
            studentId = "stud_1",
            scheduledDate = "2023-10-24",
            scheduledTime = "10:00:00",
            durationMinutes = 45,
            status = LessonStatus.IN_PROGRESS,
            pickupLocation = "Sector 15",
            route = "NH-48",
            notes = "Practice",
            vehicleId = "veh_1",
            student = students[0],
            vehicle = sampleVehicle
        )
        val session = LessonSession(
            id = "session_1",
            lessonId = "lesson_1",
            startedAt = "2023-10-24T10:05:00Z" // 40 mins passed roughly if now is 10:45
        )
        return Pair(lesson, session)
    }

    fun getTodaySchedule(): List<Lesson> {
        return listOf(
            Lesson(
                id = "lesson_2",
                instructorId = "inst_1",
                studentId = "stud_2",
                scheduledDate = "2023-10-24",
                scheduledTime = "10:30:00",
                durationMinutes = 45,
                status = LessonStatus.SCHEDULED,
                pickupLocation = "Mall Road",
                student = students[1],
                vehicle = sampleVehicle
            ),
            Lesson(
                id = "lesson_3",
                instructorId = "inst_1",
                studentId = "stud_3",
                scheduledDate = "2023-10-24",
                scheduledTime = "11:30:00",
                durationMinutes = 45,
                status = LessonStatus.SCHEDULED,
                pickupLocation = "Civil Lines",
                student = students[2],
                vehicle = sampleVehicle
            )
        )
    }
}
