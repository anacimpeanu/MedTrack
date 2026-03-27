package com.medtrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtrack.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationsDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY sent_at DESC")
    fun observeNotifications(userId: Long): Flow<List<NotificationEntity>>
}

