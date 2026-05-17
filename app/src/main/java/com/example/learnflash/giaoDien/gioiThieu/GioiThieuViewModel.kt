package com.example.learnflash.giaoDien.gioiThieu

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.thucThe.TuVung
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

// Lớp ViewModel xử lý nghiệp vụ xuất/nhập dữ liệu trong màn hình Giới thiệu
class GioiThieuViewModel(private val khoDuLieu: KhoDuLieuTuVung) : ViewModel() {

    // Thực thi truy xuất toàn bộ từ vựng và chuyển đổi thành định dạng JSON
    fun xuatDuLieuJson(context: Context, hoanThanh: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // Thu thập danh sách từ vựng hiện tại
                val danhSach = khoDuLieu.layToanBoTuVung().firstOrNull() ?: emptyList()
                val jsonString = Gson().toJson(danhSach)

                // Khởi tạo tệp tin trong bộ nhớ đệm nội bộ của ứng dụng
                val file = File(context.filesDir, "dulieu_tuvung.json")
                file.writeText(jsonString)
                hoanThanh("Đã xuất JSON thành công tại: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e("LearnFlash", "Lỗi xuất dữ liệu", e)
                hoanThanh("Xuất thất bại: ${e.message}")
            }
        }
    }

    // Thực thi đọc tệp JSON và nạp dữ liệu trở lại Room Database
    fun nhapDuLieuJson(context: Context, hoanThanh: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val file = File(context.filesDir, "dulieu_tuvung.json")
                if (!file.exists()) {
                    hoanThanh("Không tìm thấy tệp dulieu_tuvung.json")
                    return@launch
                }

                // Chuyển đổi JSON text thành mảng đối tượng
                val jsonString = file.readText()
                val danhSach = Gson().fromJson(jsonString, Array<TuVung>::class.java).toList()

                // Thực thi chèn từng bản ghi vào Database với ID làm mới
                danhSach.forEach { tuVung ->
                    khoDuLieu.luuTuVung(tuVung.copy(id = 0))
                }
                hoanThanh("Đã nhập thành công ${danhSach.size} từ vựng.")
            } catch (e: Exception) {
                Log.e("LearnFlash", "Lỗi nhập dữ liệu", e)
                hoanThanh("Nhập thất bại: ${e.message}")
            }
        }
    }
}
