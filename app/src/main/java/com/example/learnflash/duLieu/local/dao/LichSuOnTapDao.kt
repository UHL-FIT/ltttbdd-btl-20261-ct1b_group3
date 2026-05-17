package com.example.learnflash.duLieu.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.learnflash.duLieu.local.thucThe.LichSuOnTap
import kotlinx.coroutines.flow.Flow

// Giao diện (Interface) định nghĩa các thao tác truy vấn với bảng lich_su_on_tap
@Dao
interface LichSuOnTapDao {

    // Lấy danh sách toàn bộ lịch sử học tập
    @Query("SELECT * FROM lich_su_on_tap ORDER BY ngayOnTap DESC")
    fun layToanBoLichSu(): Flow<List<LichSuOnTap>>

    // Thực thi thao tác thêm một bản ghi lịch sử mới
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun themLichSu(lichSu: LichSuOnTap)
}
