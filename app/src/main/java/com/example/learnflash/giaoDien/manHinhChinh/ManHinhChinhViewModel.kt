package com.example.learnflash.giaoDien.manHinhChinh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.thucThe.TuVung
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Lớp ViewModel xử lý luồng dữ liệu một chiều cho Màn Hình Chính
class ManHinhChinhViewModel(private val khoDuLieu: KhoDuLieuTuVung) : ViewModel() {

    // Luồng dữ liệu StateFlow tự động thu thập và cập nhật danh sách từ vựng từ Repository
    val danhSachTuVung: StateFlow<List<TuVung>> = khoDuLieu.layToanBoTuVung()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Luồng dữ liệu StateFlow tổng hợp hai nguồn thống kê (Tổng số từ và Số từ đã thuộc)
    val thongKeTuVung: StateFlow<Pair<Int, Int>> = combine(
        khoDuLieu.demTongSoTuVung(),
        khoDuLieu.demSoTuDaThuoc()
    ) { tongSo, daThuoc ->
        Pair(tongSo, daThuoc)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(0, 0)
    )

    // Trạng thái (State) quản lý việc đóng/mở hộp thoại xác nhận xóa từ vựng
    val tuVungCanXoa = MutableStateFlow<TuVung?>(null)

    // Thực thi thao tác xóa từ vựng trong Coroutine sau khi người dùng đồng ý
    fun xacNhanXoaTuVung() {
        tuVungCanXoa.value?.let { tuVung ->
            viewModelScope.launch {
                khoDuLieu.xoaTuVung(tuVung)
                tuVungCanXoa.value = null
            }
        }
    }

    // Sự kiện người dùng nhấn nút xóa, mở hộp thoại yêu cầu xác nhận
    fun yeuCauXoaTuVung(tuVung: TuVung) {
        tuVungCanXoa.value = tuVung
    }
}
