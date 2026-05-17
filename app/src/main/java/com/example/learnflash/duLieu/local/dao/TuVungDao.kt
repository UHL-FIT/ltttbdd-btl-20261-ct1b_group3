package com.example.learnflash.duLieu.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learnflash.duLieu.local.thucThe.TuVung
import kotlinx.coroutines.flow.Flow

// Giao diện (Interface) định nghĩa các thao tác truy vấn với bảng tu_vung
@Dao
interface TuVungDao {

    // Lấy toàn bộ danh sách từ vựng dưới dạng luồng dữ liệu State (Flow)
    @Query("SELECT * FROM tu_vung ORDER BY id DESC")
    fun layToanBoTuVung(): Flow<List<TuVung>>

    // Lấy danh sách từ vựng cần ôn tập dựa trên thời gian hiện tại
    @Query("SELECT * FROM tu_vung WHERE ngayOnTapTiepTheo <= :thoiGianHienTai AND daThuoc = 0")
    fun layTuVungCanOnTap(thoiGianHienTai: Long): Flow<List<TuVung>>

    // Thực thi thao tác thêm một từ vựng mới, ghi đè nếu đã tồn tại trùng lặp ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun themTuVung(tuVung: TuVung)

    // Thực thi thao tác cập nhật thông tin từ vựng
    @Update
    suspend fun capNhatTuVung(tuVung: TuVung)

    // Thực thi thao tác xóa một từ vựng khỏi cơ sở dữ liệu
    @Delete
    suspend fun xoaTuVung(tuVung: TuVung)

    // Trả về số lượng tổng số từ vựng hiện có
    @Query("SELECT COUNT(*) FROM tu_vung")
    fun demTongSoTuVung(): Flow<Int>

    // Trả về số lượng từ vựng đã được đánh dấu là thuộc (daThuoc = 1)
    @Query("SELECT COUNT(*) FROM tu_vung WHERE daThuoc = 1")
    fun demSoTuDaThuoc(): Flow<Int>
}
