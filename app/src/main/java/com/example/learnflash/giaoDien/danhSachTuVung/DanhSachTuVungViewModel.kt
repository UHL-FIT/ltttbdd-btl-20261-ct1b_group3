package com.example.learnflash.giaoDien.danhSachTuVung

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.thucThe.TuVung
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Lớp ViewModel quản lý luồng State cho màn hình danh sách từ vựng theo danh mục
class DanhSachTuVungViewModel(
    private val khoDuLieu: KhoDuLieuTuVung,
    val danhMucId: String,
    val tenDanhMuc: String
) : ViewModel() {

    // Trạng thái StateFlow lưu chuỗi tìm kiếm người dùng đang nhập
    private val _tuKhoaTimKiem = MutableStateFlow("")
    val tuKhoaTimKiem: StateFlow<String> = _tuKhoaTimKiem.asStateFlow()

    // Luồng StateFlow danh sách từ vựng lọc theo danhMucId và chuỗi tìm kiếm
    val danhSachTuVung: StateFlow<List<TuVung>> = combine(
        khoDuLieu.layToanBoTuVung()
            .map { ds -> ds.filter { it.danhMucId == danhMucId } },
        _tuKhoaTimKiem
    ) { danhSach, tuKhoa ->
        // Lọc real-time theo từ khóa tìm kiếm — không cần truy vấn DB lần nào
        if (tuKhoa.isBlank()) danhSach
        else danhSach.filter { tuVung ->
            tuVung.tuKhoa.contains(tuKhoa, ignoreCase = true) ||
                    tuVung.nghiaTiengViet.contains(tuKhoa, ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Trạng thái lưu đối tượng TuVung đang chờ xác nhận xóa
    private val _tuVungCanXoa = MutableStateFlow<TuVung?>(null)
    val tuVungCanXoa: StateFlow<TuVung?> = _tuVungCanXoa.asStateFlow()

    // Hàm cập nhật chuỗi tìm kiếm khi người dùng nhập vào SearchBar
    fun capNhatTimKiem(tuKhoa: String) {
        _tuKhoaTimKiem.value = tuKhoa
    }

    // Sự kiện người dùng nhấn xóa — gán đối tượng vào State để mở hộp thoại xác nhận
    fun yeuCauXoaTuVung(tuVung: TuVung) {
        _tuVungCanXoa.value = tuVung
    }

    // Thực thi xóa từ vựng sau khi người dùng xác nhận
    fun xacNhanXoaTuVung() {
        _tuVungCanXoa.value?.let { tuVung ->
            viewModelScope.launch {
                khoDuLieu.xoaTuVung(tuVung)
                _tuVungCanXoa.value = null
            }
        }
    }

    // Hủy thao tác xóa — đóng hộp thoại xác nhận
    fun huyXoaTuVung() {
        _tuVungCanXoa.value = null
    }
}