package com.example.learnflash.giaoDien.gioiThieu

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.thucThe.TuVung
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

// Lớp ViewModel xử lý nghiệp vụ xuất/nhập dữ liệu cho màn hình Giới thiệu
class GioiThieuViewModel(private val khoDuLieu: KhoDuLieuTuVung) : ViewModel() {

    // Trạng thái (State) kiểm soát vòng xoay Loading khi đang xử lý tác vụ IO
    private val _dangXuLy = MutableStateFlow(false)
    val dangXuLy: StateFlow<Boolean> = _dangXuLy.asStateFlow()

    // Trạng thái (State) lưu thông báo kết quả sau khi xử lý xong để hiển thị SnackBar
    private val _thongBaoKetQua = MutableStateFlow("")
    val thongBaoKetQua: StateFlow<String> = _thongBaoKetQua.asStateFlow()

    // Hàm tiện ích tạo phần đầu tiêu đề CSV theo thứ tự các trường của Entity TuVung
    private fun taoTieuDeCsv(): String {
        return "id,tuKhoa,nghiaTiengViet,phienAm,loaiTu,capDoSrs,ngayOnTapTiepTheo,daThuoc"
    }

    // Hàm tiện ích chuyển đổi một đối tượng TuVung thành một dòng văn bản CSV
    private fun chuyenTuVungThanhDongCsv(tuVung: TuVung): String {
        return "${tuVung.id},\"${tuVung.tuKhoa}\",\"${tuVung.nghiaTiengViet}\"," +
                "\"${tuVung.phienAm}\",\"${tuVung.loaiTu}\"," +
                "${tuVung.capDoSrs},${tuVung.ngayOnTapTiepTheo},${tuVung.daThuoc}"
    }

    // Hàm tiện ích phân tích một dòng văn bản CSV thành đối tượng TuVung
    private fun phanTichDongCsv(dong: String): TuVung? {
        return try {
            // Tách dòng CSV theo dấu phẩy, loại bỏ ký tự nháy kép
            val cacCot = dong.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
            val loaiBoNhay = { s: String -> s.trim().removeSurrounding("\"") }
            TuVung(
                id = 0,
                tuKhoa = loaiBoNhay(cacCot[1]),
                nghiaTiengViet = loaiBoNhay(cacCot[2]),
                phienAm = loaiBoNhay(cacCot[3]),
                loaiTu = loaiBoNhay(cacCot[4]),
                capDoSrs = cacCot[5].trim().toIntOrNull() ?: 0,
                ngayOnTapTiepTheo = cacCot[6].trim().toLongOrNull() ?: System.currentTimeMillis(),
                daThuoc = cacCot[7].trim().toBoolean()
            )
        } catch (e: Exception) {
            Log.e("LearnFlash", "Lỗi phân tích dòng CSV: $dong", e)
            null
        }
    }

    // Thực thi xuất toàn bộ từ vựng ra file JSON trong thư mục nội bộ
    fun xuatDuLieuJson(context: Context) {
        _dangXuLy.value = true
        viewModelScope.launch {
            try {
                val danhSach = khoDuLieu.layToanBoTuVung().firstOrNull() ?: emptyList()
                val jsonString = Gson().toJson(danhSach)
                val file = File(context.filesDir, "learnflash_tuvung.json")
                file.writeText(jsonString)
                _thongBaoKetQua.value = "Đã xuất JSON: ${danhSach.size} từ vựng vào ${file.name}"
            } catch (e: Exception) {
                Log.e("LearnFlash", "Lỗi xuất JSON", e)
                _thongBaoKetQua.value = "Xuất JSON thất bại: ${e.message}"
            } finally {
                _dangXuLy.value = false
            }
        }
    }

    // Thực thi đọc file JSON từ thư mục nội bộ và nạp lại vào Room Database
    fun nhapDuLieuJson(context: Context) {
        _dangXuLy.value = true
        viewModelScope.launch {
            try {
                val file = File(context.filesDir, "learnflash_tuvung.json")
                if (!file.exists()) {
                    _thongBaoKetQua.value = "Không tìm thấy file learnflash_tuvung.json"
                    return@launch
                }
                val danhSach = Gson().fromJson(file.readText(), Array<TuVung>::class.java).toList()
                // Đặt lại ID về 0 để Room tự tạo khóa chính mới, tránh xung đột
                danhSach.forEach { khoDuLieu.luuTuVung(it.copy(id = 0)) }
                _thongBaoKetQua.value = "Đã nhập JSON thành công: ${danhSach.size} từ vựng"
            } catch (e: Exception) {
                Log.e("LearnFlash", "Lỗi nhập JSON", e)
                _thongBaoKetQua.value = "Nhập JSON thất bại: ${e.message}"
            } finally {
                _dangXuLy.value = false
            }
        }
    }

    // Thực thi xuất toàn bộ từ vựng ra file CSV trong thư mục nội bộ
    fun xuatDuLieuCsv(context: Context) {
        _dangXuLy.value = true
        viewModelScope.launch {
            try {
                val danhSach = khoDuLieu.layToanBoTuVung().firstOrNull() ?: emptyList()
                // Ghép tiêu đề và từng dòng dữ liệu thành nội dung CSV hoàn chỉnh
                val noiDungCsv = buildString {
                    appendLine(taoTieuDeCsv())
                    danhSach.forEach { appendLine(chuyenTuVungThanhDongCsv(it)) }
                }
                val file = File(context.filesDir, "learnflash_tuvung.csv")
                file.writeText(noiDungCsv)
                _thongBaoKetQua.value = "Đã xuất CSV: ${danhSach.size} từ vựng vào ${file.name}"
            } catch (e: Exception) {
                Log.e("LearnFlash", "Lỗi xuất CSV", e)
                _thongBaoKetQua.value = "Xuất CSV thất bại: ${e.message}"
            } finally {
                _dangXuLy.value = false
            }
        }
    }

    // Thực thi đọc file CSV từ thư mục nội bộ và nạp lại vào Room Database
    fun nhapDuLieuCsv(context: Context) {
        _dangXuLy.value = true
        viewModelScope.launch {
            try {
                val file = File(context.filesDir, "learnflash_tuvung.csv")
                if (!file.exists()) {
                    _thongBaoKetQua.value = "Không tìm thấy file learnflash_tuvung.csv"
                    return@launch
                }
                // Bỏ qua dòng tiêu đề, phân tích từng dòng dữ liệu còn lại
                val cacDong = file.readLines().drop(1)
                val danhSach = cacDong.mapNotNull { phanTichDongCsv(it) }
                danhSach.forEach { khoDuLieu.luuTuVung(it) }
                _thongBaoKetQua.value = "Đã nhập CSV thành công: ${danhSach.size} từ vựng"
            } catch (e: Exception) {
                Log.e("LearnFlash", "Lỗi nhập CSV", e)
                _thongBaoKetQua.value = "Nhập CSV thất bại: ${e.message}"
            } finally {
                _dangXuLy.value = false
            }
        }
    }

    // Đặt lại chuỗi thông báo về rỗng sau khi SnackBar đã hiển thị xong
    fun daHienThiThongBao() {
        _thongBaoKetQua.value = ""
    }
}