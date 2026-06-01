package com.example.learnflash.giaoDien.thongKe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuDanhMuc
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.thucThe.DanhMuc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

// Lớp dữ liệu đóng gói số liệu thống kê tổng quan
data class DuLieuThongKe(
    val tongSoTu: Int = 0,
    val soTuDaThuoc: Int = 0,
    val soTuChuaThuoc: Int = 0,
    val trungBinhCapDoSrs: Double = 0.0,
    val caoNhatCapDoSrs: Int = 0,
    val thapNhatCapDoSrs: Int = 0,
    val soTuOnTapHomNay: Int = 0,
    val soLuotDaOnHomNay: Int = 0
)

// Lớp dữ liệu đóng gói thống kê tiến độ của một danh mục
data class ThongKeMotDanhMuc(
    val danhMuc: DanhMuc,
    val tongSo: Int,
    val soDaThuoc: Int
) {
    // Tính tỉ lệ phần trăm đã thuộc trong danh mục — logic Kotlin thuần
    val tiLeDaThuoc: Float get() = if (tongSo == 0) 0f else soDaThuoc.toFloat() / tongSo
}

// Lớp ViewModel quản lý luồng StateFlow thống kê tổng quan và theo danh mục
class ThongKeViewModel(
    private val khoDuLieu: KhoDuLieuTuVung,
    private val khoDuLieuDanhMuc: KhoDuLieuDanhMuc
) : ViewModel() {

    // StateFlow phát tán đối tượng thống kê tổng hợp xuống UI
    private val _duLieuThongKe = MutableStateFlow(DuLieuThongKe())
    val duLieuThongKe: StateFlow<DuLieuThongKe> = _duLieuThongKe.asStateFlow()

    // StateFlow phát tán danh sách thống kê tiến độ theo từng danh mục
    private val _thongKeTheoDanhMuc = MutableStateFlow<List<ThongKeMotDanhMuc>>(emptyList())
    val thongKeTheoDanhMuc: StateFlow<List<ThongKeMotDanhMuc>> = _thongKeTheoDanhMuc.asStateFlow()

    // Luồng StateFlow danh sách danh mục để join với dữ liệu thống kê
    private val danhSachDanhMuc: StateFlow<List<DanhMuc>> = khoDuLieuDanhMuc.layToanBoDanhMuc()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Khởi động thu thập dữ liệu thống kê tổng quan và theo danh mục
        thuThapDuLieuThongKe()
        thuThapThongKeTheoDanhMuc()
    }

    // Tính mốc thời gian đầu ngày và cuối ngày theo múi giờ thiết bị
    private fun tinhKhoangThoiGianHomNay(): Pair<Long, Long> {
        val lich = Calendar.getInstance()
        lich.set(Calendar.HOUR_OF_DAY, 0); lich.set(Calendar.MINUTE, 0)
        lich.set(Calendar.SECOND, 0); lich.set(Calendar.MILLISECOND, 0)
        val batDau = lich.timeInMillis
        lich.set(Calendar.HOUR_OF_DAY, 23); lich.set(Calendar.MINUTE, 59)
        lich.set(Calendar.SECOND, 59); lich.set(Calendar.MILLISECOND, 999)
        return Pair(batDau, lich.timeInMillis)
    }

    // Thu thập đồng thời 8 Flow thống kê tổng quan và tổng hợp thành StateFlow duy nhất
    private fun thuThapDuLieuThongKe() {
        val (batDauNgay, cuoiNgay) = tinhKhoangThoiGianHomNay()
        viewModelScope.launch {
            combine(
                khoDuLieu.demTongSoTuVung(),
                khoDuLieu.demSoTuDaThuoc(),
                khoDuLieu.demSoTuChuaThuoc(),
                khoDuLieu.tinhTrungBinhCapDoSrs(),
                khoDuLieu.layCaoNhatCapDoSrs(),
                khoDuLieu.layThapNhatCapDoSrs(),
                khoDuLieu.demTuVungOnTapHomNay(cuoiNgay),
                khoDuLieu.demSoLuotOnTapHomNay(batDauNgay, cuoiNgay)
            ) { mangGiaTri ->
                DuLieuThongKe(
                    tongSoTu = mangGiaTri[0] as Int,
                    soTuDaThuoc = mangGiaTri[1] as Int,
                    soTuChuaThuoc = mangGiaTri[2] as Int,
                    trungBinhCapDoSrs = (mangGiaTri[3] as? Double) ?: 0.0,
                    caoNhatCapDoSrs = (mangGiaTri[4] as? Int) ?: 0,
                    thapNhatCapDoSrs = (mangGiaTri[5] as? Int) ?: 0,
                    soTuOnTapHomNay = mangGiaTri[6] as Int,
                    soLuotDaOnHomNay = mangGiaTri[7] as Int
                )
            }.collect { _duLieuThongKe.value = it }
        }
    }

    // Thu thập dữ liệu thống kê theo danh mục — kết hợp Flow thống kê Room với danh sách DanhMuc
    private fun thuThapThongKeTheoDanhMuc() {
        viewModelScope.launch {
            // Kết hợp 2 Flow: thống kê từ DB và danh sách tên danh mục để hiển thị tên đúng
            combine(
                khoDuLieu.thongKeTheoDanhMuc(),
                danhSachDanhMuc
            ) { danhSachThongKe, danhSachDM ->
                // Ghép dữ liệu thống kê với tên danh mục tương ứng
                danhSachThongKe.mapNotNull { thongKe ->
                    val danhMuc = danhSachDM.find { it.id == thongKe.danhMucId }
                    if (danhMuc != null) {
                        ThongKeMotDanhMuc(
                            danhMuc = danhMuc,
                            tongSo = thongKe.tongSo,
                            soDaThuoc = thongKe.soDaThuoc
                        )
                    } else null
                }
                    // Sắp xếp theo tỉ lệ thuộc giảm dần để xem chủ đề đã giỏi trước
                    .sortedByDescending { it.tiLeDaThuoc }
            }.collect { _thongKeTheoDanhMuc.value = it }
        }
    }
}