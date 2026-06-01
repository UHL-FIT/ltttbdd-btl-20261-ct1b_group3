package com.example.learnflash.giaoDien.thongKe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuDanhMuc
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.dataStore.CaiDatDataStore
import com.example.learnflash.duLieu.local.thucThe.DanhMuc
import com.example.learnflash.duLieu.local.thucThe.LichSuOnTap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
    val soLuotDaOnHomNay: Int = 0,
    // Số từ vựng thực tế đã ôn tập hôm nay
    val tongSoTuDaOnHomNay: Int = 0,
    // Mục tiêu học ngày lấy từ cài đặt
    val mucTieuHocNgay: Int = 10,
    // Số lượng từ vựng ở cấp độ SRS 0
    val soTuSrsCap0: Int = 0,
    // Số lượng từ vựng ở cấp độ SRS 1
    val soTuSrsCap1: Int = 0,
    // Số lượng từ vựng ở cấp độ SRS 2
    val soTuSrsCap2: Int = 0,
    // Số lượng từ vựng ở cấp độ SRS 3
    val soTuSrsCap3: Int = 0,
    // Số lượng từ vựng ở cấp độ SRS 4
    val soTuSrsCap4: Int = 0
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
    private val khoDuLieuDanhMuc: KhoDuLieuDanhMuc,
    private val caiDatDataStore: CaiDatDataStore
) : ViewModel() {

    // StateFlow phát tán đối tượng thống kê tổng hợp xuống UI
    private val _duLieuThongKe = MutableStateFlow(DuLieuThongKe())
    val duLieuThongKe: StateFlow<DuLieuThongKe> = _duLieuThongKe.asStateFlow()

    // StateFlow phát tán danh sách thống kê tiến độ theo từng danh mục
    private val _thongKeTheoDanhMuc = MutableStateFlow<List<ThongKeMotDanhMuc>>(emptyList())
    val thongKeTheoDanhMuc: StateFlow<List<ThongKeMotDanhMuc>> = _thongKeTheoDanhMuc.asStateFlow()

    // StateFlow phát tán danh sách 5 lịch sử ôn tập gần nhất xuống UI
    private val _lichSuGanDay = MutableStateFlow<List<LichSuOnTap>>(emptyList())
    val lichSuGanDay: StateFlow<List<LichSuOnTap>> = _lichSuGanDay.asStateFlow()

    // Luồng StateFlow danh sách danh mục để join với dữ liệu thống kê
    private val danhSachDanhMuc: StateFlow<List<DanhMuc>> = khoDuLieuDanhMuc.layToanBoDanhMuc()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Khởi động thu thập dữ liệu thống kê tổng quan
        thuThapDuLieuThongKe()
        // Khởi động thu thập thống kê tiến độ theo từng danh mục
        thuThapThongKeTheoDanhMuc()
        // Khởi động thu thập danh sách lịch sử ôn tập gần đây
        thuThapLichSuGanDay()
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

    // Thu thập đồng thời các Flow thống kê tổng quan và tổng hợp thành StateFlow duy nhất
    private fun thuThapDuLieuThongKe() {
        val (batDauNgay, cuoiNgay) = tinhKhoangThoiGianHomNay()
        viewModelScope.launch {
            val danhSachFlow = listOf(
                khoDuLieu.demTongSoTuVung().map { it as Any? },
                khoDuLieu.demSoTuDaThuoc().map { it as Any? },
                khoDuLieu.demSoTuChuaThuoc().map { it as Any? },
                khoDuLieu.tinhTrungBinhCapDoSrs().map { it as Any? },
                khoDuLieu.layCaoNhatCapDoSrs().map { it as Any? },
                khoDuLieu.layThapNhatCapDoSrs().map { it as Any? },
                khoDuLieu.demTuVungOnTapHomNay(cuoiNgay).map { it as Any? },
                khoDuLieu.demSoLuotOnTapHomNay(batDauNgay, cuoiNgay).map { it as Any? },
                khoDuLieu.tinhTongSoTuDaOnHomNay(batDauNgay, cuoiNgay).map { it as Any? },
                caiDatDataStore.mucTieuHocNgayFlow.map { it as Any? },
                khoDuLieu.demSoTuTheoCapDoSrs(0).map { it as Any? },
                khoDuLieu.demSoTuTheoCapDoSrs(1).map { it as Any? },
                khoDuLieu.demSoTuTheoCapDoSrs(2).map { it as Any? },
                khoDuLieu.demSoTuTheoCapDoSrs(3).map { it as Any? },
                khoDuLieu.demSoTuTheoCapDoSrs(4).map { it as Any? }
            )
            combine(danhSachFlow) { mangGiaTri: Array<Any?> ->
                DuLieuThongKe(
                    tongSoTu = mangGiaTri[0] as Int,
                    soTuDaThuoc = mangGiaTri[1] as Int,
                    soTuChuaThuoc = mangGiaTri[2] as Int,
                    trungBinhCapDoSrs = (mangGiaTri[3] as? Double) ?: 0.0,
                    caoNhatCapDoSrs = (mangGiaTri[4] as? Int) ?: 0,
                    thapNhatCapDoSrs = (mangGiaTri[5] as? Int) ?: 0,
                    soTuOnTapHomNay = mangGiaTri[6] as Int,
                    soLuotDaOnHomNay = mangGiaTri[7] as Int,
                    tongSoTuDaOnHomNay = (mangGiaTri[8] as? Int) ?: 0,
                    mucTieuHocNgay = mangGiaTri[9] as Int,
                    soTuSrsCap0 = mangGiaTri[10] as Int,
                    soTuSrsCap1 = mangGiaTri[11] as Int,
                    soTuSrsCap2 = mangGiaTri[12] as Int,
                    soTuSrsCap3 = mangGiaTri[13] as Int,
                    soTuSrsCap4 = mangGiaTri[14] as Int
                )
            }.collect { _duLieuThongKe.value = it }
        }
    }

    // Thu thập dữ liệu thống kê theo danh mục — kết hợp Flow thống kê Room với danh sách DanhMuc
    private fun thuThapThongKeTheoDanhMuc() {
        viewModelScope.launch {
            combine(
                khoDuLieu.thongKeTheoDanhMuc(),
                danhSachDanhMuc
            ) { danhSachThongKe, danhSachDM ->
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
                    .sortedByDescending { it.tiLeDaThuoc }
            }.collect { _thongKeTheoDanhMuc.value = it }
        }
    }

    // Thu thập danh sách 5 lịch sử ôn tập gần đây nhất
    private fun thuThapLichSuGanDay() {
        viewModelScope.launch {
            khoDuLieu.layToanBoLichSu().collect { danhSachLichSu ->
                // Chỉ lấy tối đa 5 bản ghi lịch sử mới nhất
                _lichSuGanDay.value = danhSachLichSu.take(5)
            }
        }
    }
}