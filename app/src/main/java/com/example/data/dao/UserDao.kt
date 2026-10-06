package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM user_profiles WHERE id = :id")
  fun getUserById(id: Long): Flow<UserProfileEntity?>

  @Query("SELECT * FROM user_profiles ORDER BY id ASC")
  fun getAllUsers(): Flow<List<UserProfileEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserProfileEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<UserProfileEntity>)

  @Update
  suspend fun updateUser(user: UserProfileEntity)

  @Query("SELECT COUNT(*) FROM user_profiles")
  suspend fun getUserCount(): Int
}
