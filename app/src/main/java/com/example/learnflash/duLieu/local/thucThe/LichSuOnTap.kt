package com.example.learnflash.duLieu.local.thucThe

import androidx.room.Entity
import androidx.room.PrimaryKey

// Bảng dữ liệu lưu trữ thống kê lịch sử ôn tập
@Entity(tableName = "lich_su_on_tap")
data class LichSuOnTap(
    // Khóa chính tự động tăng
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // Ngày thực hiện ôn tập (tính bằng milliseconds)
    val ngayOnTap: Long = System.currentTimeMillis(),
    // Tổng số lượng từ đã duyệt qua trong buổi học
    val soTuDaHoc: Int,
    // Số từ nhớ đúng
    val soTuDung: Int,
    // Số từ nhớ sai
    val soTuSai: Int
)
