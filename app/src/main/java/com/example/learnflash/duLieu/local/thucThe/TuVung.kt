package com.example.learnflash.duLieu.local.thucThe

import androidx.room.Entity
import androidx.room.PrimaryKey

// Bảng dữ liệu lưu trữ từ vựng trong Room Database
@Entity(tableName = "tu_vung")
data class TuVung(
    // Khóa chính tự động tăng
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // Từ vựng nguyên bản
    val tuKhoa: String,
    // Nghĩa của từ
    val nghiaTiengViet: String,
    // Phiên âm từ vựng
    val phienAm: String = "",
    // Loại từ (Danh từ, Động từ,...)
    val loaiTu: String = "",
    // Cấp độ ghi nhớ phục vụ thuật toán lặp ngắt quãng (Spaced Repetition)
    val capDoSrs: Int = 0,
    // Thời điểm cần ôn tập lại (tính bằng milliseconds)
    val ngayOnTapTiepTheo: Long = System.currentTimeMillis(),
    // Đánh dấu từ vựng đã được học thuộc hoàn toàn hay chưa
    val daThuoc: Boolean = false
)
