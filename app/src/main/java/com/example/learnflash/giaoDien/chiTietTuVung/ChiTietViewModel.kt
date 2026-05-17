package com.example.learnflash.giaoDien.chiTietTuVung

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.thucThe.TuVung
import kotlinx.coroutines.launch

// Lớp ViewModel quản lý luồng State và nghiệp vụ cho màn hình Thêm/Sửa
class ChiTietViewModel(
    private val khoDuLieu: KhoDuLieuTuVung,
    private val idTuVung: Int
) : ViewModel() {

    // Trạng thái (State) quản lý chuỗi nhập liệu từ khóa
    private val _tuKhoa = mutableStateOf("")
    val tuKhoa: State<String> = _tuKhoa

    // Trạng thái (State) quản lý chuỗi nhập liệu ý nghĩa tiếng Việt
    private val _nghiaTiengViet = mutableStateOf("")
    val nghiaTiengViet: State<String> = _nghiaTiengViet

    // Trạng thái kiểm soát việc hiển thị Loading khi gọi mạng (API)
    private val _dangTai = mutableStateOf(false)
    val dangTai: State<Boolean> = _dangTai

    // Trạng thái lưu trữ chuỗi thông báo lỗi xác thực (Validation)
    private val _loiNhapLieu = mutableStateOf("")
    val loiNhapLieu: State<String> = _loiNhapLieu

    // Hàm cập nhật State khi người dùng nhập từ khóa
    fun capNhatTuKhoa(giaTriMoi: String) {
        _tuKhoa.value = giaTriMoi
        _loiNhapLieu.value = ""
    }

    // Hàm cập nhật State khi người dùng nhập ý nghĩa
    fun capNhatNghia(giaTriMoi: String) {
        _nghiaTiengViet.value = giaTriMoi
        _loiNhapLieu.value = ""
    }

    // Thực thi tác vụ gọi Repository tra cứu dữ liệu ý nghĩa qua kết nối Internet
    fun traCuuApi() {
        if (_tuKhoa.value.isBlank()) {
            _loiNhapLieu.value = "Vui lòng nhập từ khóa để tra cứu"
            return
        }
        _dangTai.value = true
        viewModelScope.launch {
            val ketQua = khoDuLieu.traCuuTuVungTrucTuyen(_tuKhoa.value)
            if (ketQua.isSuccess) {
                val tuVungApi = ketQua.getOrNull()
                _nghiaTiengViet.value = tuVungApi?.nghiaTiengViet ?: ""
            } else {
                _loiNhapLieu.value = ketQua.exceptionOrNull()?.message ?: "Lỗi kết nối mạng"
            }
            _dangTai.value = false
        }
    }

    // Thực thi xác thực và tiến hành lưu dữ liệu qua Repository
    fun luuTuVung(hoanThanh: () -> Unit) {
        if (_tuKhoa.value.isBlank() || _nghiaTiengViet.value.isBlank()) {
            _loiNhapLieu.value = "Từ khóa và Ý nghĩa không được để trống"
            return
        }
        viewModelScope.launch {
            val tuVungMoi = TuVung(
                id = idTuVung,
                tuKhoa = _tuKhoa.value,
                nghiaTiengViet = _nghiaTiengViet.value
            )
            khoDuLieu.luuTuVung(tuVungMoi)
            hoanThanh()
        }
    }
}
