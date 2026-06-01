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

    // Lấy danh sách toàn bộ lịch sử học tập sắp xếp mới nhất lên đầu
    @Query("SELECT * FROM lich_su_on_tap ORDER BY ngayOnTap DESC")
    fun layToanBoLichSu(): Flow<List<LichSuOnTap>>

    // Đếm số lượt ôn tập đã thực hiện trong khoảng thời gian một ngày cụ thể
    @Query("SELECT COUNT(*) FROM lich_su_on_tap WHERE ngayOnTap >= :batDauNgay AND ngayOnTap <= :cuoiNgay")
    fun demSoLuotOnTapTrongNgay(batDauNgay: Long, cuoiNgay: Long): Flow<Int>

    // Tính tổng số từ vựng đã ôn trong khoảng thời gian một ngày cụ thể
    @Query("SELECT SUM(soTuDaHoc) FROM lich_su_on_tap WHERE ngayOnTap >= :batDauNgay AND ngayOnTap <= :cuoiNgay")
    fun tinhTongSoTuDaOnTrongNgay(batDauNgay: Long, cuoiNgay: Long): Flow<Int?>

    // Thực thi thao tác thêm một bản ghi lịch sử mới
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun themLichSu(lichSu: LichSuOnTap)
}