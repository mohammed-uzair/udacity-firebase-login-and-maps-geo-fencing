package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO

class FakeDao : RemindersDao {
    private val dataSource = mutableListOf<ReminderDTO>()

    override suspend fun saveReminder(reminder: ReminderDTO) {
        dataSource.add(reminder)
    }

    override suspend fun getReminders(): List<ReminderDTO> = dataSource

    override suspend fun getReminderById(reminderId: String): ReminderDTO? =
        dataSource.firstOrNull { it.id == reminderId }

    override suspend fun deleteAllReminders() {
        dataSource.clear()
    }
}