package com.example.learnflash.duLieu.remote.moHinhReMote

import com.google.gson.annotations.SerializedName

// Lớp dữ liệu đại diện cho một từ vựng trả về từ Free Dictionary API
data class MoHinhTuVungRemote(
    // Ánh xạ trường word từ chuỗi JSON vào biến tuKhoa
    @SerializedName("word") val tuKhoa: String,
    // Ánh xạ phiên âm (có thể null nếu API không cung cấp)
    @SerializedName("phonetic") val phienAm: String?,
    // Danh sách các lớp ý nghĩa phân chia theo loại từ
    @SerializedName("meanings") val danhSachNghia: List<MoHinhNghiaRemote>
)

// Lớp dữ liệu đại diện cho cụm ý nghĩa dựa trên loại từ (Danh từ, Động từ,...)
data class MoHinhNghiaRemote(
    // Ánh xạ loại từ (Ví dụ: noun, verb)
    @SerializedName("partOfSpeech") val loaiTu: String,
    // Danh sách các định nghĩa chi tiết tương ứng với loại từ
    @SerializedName("definitions") val danhSachDinhNghia: List<MoHinhDinhNghiaRemote>
)

// Lớp dữ liệu chứa chuỗi định nghĩa chi tiết
data class MoHinhDinhNghiaRemote(
    // Ánh xạ dòng mô tả ý nghĩa chính xác
    @SerializedName("definition") val dinhNghia: String
)
