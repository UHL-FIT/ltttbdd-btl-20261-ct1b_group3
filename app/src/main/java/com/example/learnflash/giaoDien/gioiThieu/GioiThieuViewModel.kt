package com.example.learnflash.giaoDien.gioiThieu

import android.content.Context
import android.net.Uri
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

// Định nghĩa các chế độ nạp dữ liệu từ tệp tin vào cơ sở dữ liệu
enum class CheDoNapDuLieu(val moTa: String) {
    // Chỉ thêm những từ vựng chưa tồn tại trong cơ sở dữ liệu
    THEM_MOI("Chỉ thêm từ mới"),

    // Cập nhật lại nghĩa và thông tin của từ vựng cũ nếu trùng lặp từ khóa
    CAP_NHAT_TRUNG("Cập nhật đè từ trùng"),

    // Xóa sạch toàn bộ từ vựng hiện có trong ứng dụng và nạp mới hoàn toàn
    THAY_THE_TAT_CA("Thay thế toàn bộ")
}

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

    // Thực thi xuất toàn bộ từ vựng ra file JSON thông qua Storage Access Framework
    fun xuatDuLieuJson(context: Context, uri: Uri) {
        _dangXuLy.value = true
        viewModelScope.launch {
            try {
                // Lấy toàn bộ từ vựng dưới database cục bộ Room
                val danhSach = khoDuLieu.layToanBoTuVung().firstOrNull() ?: emptyList()
                // Kiểm tra nếu danh sách từ vựng trống thì dừng tác vụ xuất
                if (danhSach.isEmpty()) {
                    _thongBaoKetQua.value = "Không có từ vựng nào trong cơ sở dữ liệu để xuất"
                    return@launch
                }
                // Chuyển đổi danh sách đối tượng sang chuỗi văn bản định dạng JSON
                val jsonString = Gson().toJson(danhSach)
                // Mở luồng ghi dữ liệu ghi nhận vào tệp do người dùng chọn
                context.contentResolver.openOutputStream(uri)?.use { luongGhi ->
                    luongGhi.write(jsonString.toByteArray())
                }
                _thongBaoKetQua.value = "Đã xuất JSON thành công: ${danhSach.size} từ vựng"
            } catch (e: Exception) {
                Log.e("LearnFlash", "Lỗi xuất JSON", e)
                _thongBaoKetQua.value = "Xuất JSON thất bại: ${e.message}"
            } finally {
                _dangXuLy.value = false
            }
        }
    }

    // Thực thi nạp danh sách từ vựng vào Room Database theo chế độ đã chọn
    private suspend fun thucThiNapDuLieu(danhSach: List<TuVung>, cheDo: CheDoNapDuLieu): Int {
        var soLuongThanhCong = 0
        when (cheDo) {
            CheDoNapDuLieu.THEM_MOI -> {
                // Lấy toàn bộ từ vựng hiện có để đối soát trùng lặp
                val danhSachHienTai = khoDuLieu.layDanhSachToanBoTuVung()
                // Tạo tập hợp từ khóa hiện tại chuyển về chữ thường để so sánh
                val tapHopTuKhoaHienTai = danhSachHienTai.map { it.tuKhoa.lowercase().trim() }.toSet()
                // Lọc danh sách từ vựng chưa có trong máy
                val danhSachThemMoi = danhSach.filter { 
                    val tuKhoaChuan = it.tuKhoa.lowercase().trim()
                    !tapHopTuKhoaHienTai.contains(tuKhoaChuan) 
                }.map { 
                    // Tạo bản sao mới đặt lại ID tự sinh
                    it.copy(id = 0)
                }
                if (danhSachThemMoi.isNotEmpty()) {
                    // Thêm hàng loạt từ vựng mới vào cơ sở dữ liệu
                    khoDuLieu.themNhieuTuVung(danhSachThemMoi)
                    soLuongThanhCong = danhSachThemMoi.size
                }
            }
            CheDoNapDuLieu.CAP_NHAT_TRUNG -> {
                // Lấy toàn bộ từ vựng hiện tại trong Room
                val danhSachHienTai = khoDuLieu.layDanhSachToanBoTuVung()
                // Bản đồ ánh xạ từ khóa sang đối tượng để tra cứu nhanh
                val banDoHienTai = danhSachHienTai.associateBy { it.tuKhoa.lowercase().trim() }
                // Danh sách từ vựng sẽ thêm mới
                val danhSachCanThem = mutableListOf<TuVung>()
                // Danh sách từ vựng sẽ cập nhật
                val danhSachCanCapNhat = mutableListOf<TuVung>()
                danhSach.forEach { tuMoi ->
                    val tuKhoaChuan = tuMoi.tuKhoa.lowercase().trim()
                    val tuHienTai = banDoHienTai[tuKhoaChuan]
                    if (tuHienTai != null) {
                        // Tạo đối tượng cập nhật dựa trên từ hiện tại nhưng lấy nghĩa mới
                        val tuCapNhat = tuMoi.copy(
                            id = tuHienTai.id,
                            capDoSrs = tuMoi.capDoSrs,
                            ngayOnTapTiepTheo = tuMoi.ngayOnTapTiepTheo,
                            daThuoc = tuMoi.daThuoc,
                            danhMucId = tuMoi.danhMucId.ifEmpty { tuHienTai.danhMucId }
                        )
                        danhSachCanCapNhat.add(tuCapNhat)
                    } else {
                        // Tạo bản sao mới với ID tự sinh
                        danhSachCanThem.add(tuMoi.copy(id = 0))
                    }
                }
                if (danhSachCanThem.isNotEmpty()) {
                    // Thêm mới các từ vựng chưa tồn tại
                    khoDuLieu.themNhieuTuVung(danhSachCanThem)
                    soLuongThanhCong += danhSachCanThem.size
                }
                if (danhSachCanCapNhat.isNotEmpty()) {
                    // Cập nhật đè các từ vựng đã tồn tại
                    khoDuLieu.themNhieuTuVung(danhSachCanCapNhat)
                    soLuongThanhCong += danhSachCanCapNhat.size
                }
            }
            CheDoNapDuLieu.THAY_THE_TAT_CA -> {
                // Xóa sạch toàn bộ từ vựng cũ trong cơ sở dữ liệu
                khoDuLieu.xoaSachToanBoTuVung()
                // Sao chép danh sách mới đặt lại ID tự sinh
                val danhSachNapMoi = danhSach.map { it.copy(id = 0) }
                if (danhSachNapMoi.isNotEmpty()) {
                    // Thêm toàn bộ danh sách từ vựng vào Room
                    khoDuLieu.themNhieuTuVung(danhSachNapMoi)
                    soLuongThanhCong = danhSachNapMoi.size
                }
            }
        }
        return soLuongThanhCong
    }

    // Thực thi đọc file JSON từ URI do người dùng chọn và nạp lại vào Room Database theo chế độ
    fun nhapDuLieuJson(context: Context, uri: Uri, cheDo: CheDoNapDuLieu) {
        _dangXuLy.value = true
        viewModelScope.launch {
            try {
                // Mở luồng đọc dữ liệu từ tệp tin người dùng chọn
                context.contentResolver.openInputStream(uri)?.use { luongDoc ->
                    val boDoc = luongDoc.bufferedReader()
                    val jsonString = boDoc.readText()
                    // Chuyển đổi chuỗi văn bản JSON sang mảng các đối tượng từ vựng
                    val danhSach = Gson().fromJson(jsonString, Array<TuVung>::class.java).toList()
                    if (danhSach.isEmpty()) {
                        _thongBaoKetQua.value = "Tệp tin JSON không chứa dữ liệu từ vựng hợp lệ"
                        return@launch
                    }
                    // Thực thi nạp dữ liệu theo chế độ đã chọn
                    val soLuongThanhCong = thucThiNapDuLieu(danhSach, cheDo)
                    _thongBaoKetQua.value = "Đã nhập JSON thành công: $soLuongThanhCong từ vựng (${cheDo.moTa})"
                }
            } catch (e: Exception) {
                Log.e("LearnFlash", "Lỗi nhập JSON", e)
                _thongBaoKetQua.value = "Nhập JSON thất bại: ${e.message}"
            } finally {
                _dangXuLy.value = false
            }
        }
    }

    // Thực thi xuất toàn bộ từ vựng ra file CSV thông qua Storage Access Framework
    fun xuatDuLieuCsv(context: Context, uri: Uri) {
        _dangXuLy.value = true
        viewModelScope.launch {
            try {
                // Lấy toàn bộ danh sách từ vựng dưới Room Database
                val danhSach = khoDuLieu.layToanBoTuVung().firstOrNull() ?: emptyList()
                // Kiểm tra nếu danh sách từ vựng trống thì dừng tác vụ xuất
                if (danhSach.isEmpty()) {
                    _thongBaoKetQua.value = "Không có từ vựng nào trong cơ sở dữ liệu để xuất"
                    return@launch
                }
                // Xây dựng chuỗi văn bản định dạng CSV có chứa tiêu đề
                val noiDungCsv = buildString {
                    appendLine(taoTieuDeCsv())
                    danhSach.forEach { appendLine(chuyenTuVungThanhDongCsv(it)) }
                }
                // Mở luồng ghi dữ liệu ghi nhận vào tệp do người dùng chọn
                context.contentResolver.openOutputStream(uri)?.use { luongGhi ->
                    luongGhi.write(noiDungCsv.toByteArray())
                }
                _thongBaoKetQua.value = "Đã xuất CSV thành công: ${danhSach.size} từ vựng"
            } catch (e: Exception) {
                Log.e("LearnFlash", "Lỗi xuất CSV", e)
                _thongBaoKetQua.value = "Xuất CSV thất bại: ${e.message}"
            } finally {
                _dangXuLy.value = false
            }
        }
    }

    // Thực thi đọc file CSV từ URI do người dùng chọn và nạp lại vào Room Database theo chế độ
    fun nhapDuLieuCsv(context: Context, uri: Uri, cheDo: CheDoNapDuLieu) {
        _dangXuLy.value = true
        viewModelScope.launch {
            try {
                // Mở luồng đọc dữ liệu từ tệp tin người dùng chọn
                context.contentResolver.openInputStream(uri)?.use { luongDoc ->
                    val boDoc = luongDoc.bufferedReader()
                    // Bỏ qua dòng tiêu đề đầu tiên trong file CSV
                    val cacDong = boDoc.readLines().drop(1)
                    // Chuyển đổi từng dòng văn bản sang đối tượng từ vựng
                    val danhSach = cacDong.mapNotNull { phanTichDongCsv(it) }
                    if (danhSach.isEmpty()) {
                        _thongBaoKetQua.value = "Tệp tin CSV không chứa dữ liệu từ vựng hợp lệ"
                        return@launch
                    }
                    // Thực thi nạp dữ liệu theo chế độ đã chọn
                    val soLuongThanhCong = thucThiNapDuLieu(danhSach, cheDo)
                    _thongBaoKetQua.value = "Đã nhập CSV thành công: $soLuongThanhCong từ vựng (${cheDo.moTa})"
                }
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