package com.example.learnflash.giaoDien.gioiThieu

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuDanhMuc
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.thucThe.DanhMuc
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
class GioiThieuViewModel(
    private val khoDuLieu: KhoDuLieuTuVung,
    private val khoDuLieuDanhMuc: KhoDuLieuDanhMuc
) : ViewModel() {

    // Trạng thái (State) kiểm soát vòng xoay Loading khi đang xử lý tác vụ IO
    private val _dangXuLy = MutableStateFlow(false)
    val dangXuLy: StateFlow<Boolean> = _dangXuLy.asStateFlow()

    // Trạng thái (State) lưu thông báo kết quả sau khi xử lý xong để hiển thị SnackBar
    private val _thongBaoKetQua = MutableStateFlow("")
    val thongBaoKetQua: StateFlow<String> = _thongBaoKetQua.asStateFlow()

    // Trạng thái lưu trữ thông tin phiên bản hiển thị trên giao diện giới thiệu (Biến ghi nội bộ)
    private val _thongTinPhienBan = MutableStateFlow("Phiên bản 1.0.0")
    // Luồng dữ liệu công khai cung cấp thông tin phiên bản cho giao diện người dùng
    val thongTinPhienBan: StateFlow<String> = _thongTinPhienBan.asStateFlow()

    // Truy xuất thông tin phiên bản ứng dụng động từ hệ thống
    fun taiThongTinPhienBan(context: Context) {
        // Thực thi kiểm soát lỗi khi truy cập tài nguyên hệ thống
        try {
            // Kiểm tra điều kiện phiên bản Android để lấy thông tin gói cài đặt phù hợp
            val thongTinGoi = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                // Lấy thông tin gói trên Android 13 trở lên bằng cờ InfoFlags mới
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                // Tắt cảnh báo deprecated cho các hàm cũ
                @Suppress("DEPRECATION")
                // Lấy thông tin gói trên các phiên bản Android cũ hơn
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            // Trích xuất tên phiên bản hiển thị và gán giá trị mặc định nếu rỗng
            val tenPhienBan = thongTinGoi.versionName ?: "1.0.0"
            // Gán chuỗi kết quả phiên bản sạch vào StateFlow
            _thongTinPhienBan.value = "Phiên bản $tenPhienBan"
        } catch (ngoaiLe: Exception) {
            // Gán giá trị phiên bản mặc định khi xảy ra lỗi hệ thống
            _thongTinPhienBan.value = "Phiên bản 1.0.0"
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

    // Hàm phụ trợ chuẩn hóa và kiểm tra tính hợp lệ của danh sách từ vựng nhập vào
    private suspend fun chuanHoaVaKiemTraDanhSach(danhSach: List<TuVung>): List<TuVung> {
        // Lấy danh sách toàn bộ danh mục hiện có trong máy
        val danhSachDanhMuc = khoDuLieuDanhMuc.layToanBoDanhMuc().firstOrNull() ?: emptyList()
        // Tạo tập hợp ID danh mục có khả năng thay đổi để cập nhật ngầm
        val tapHopIdDanhMuc = danhSachDanhMuc.map { it.id }.toMutableSet()
        return danhSach.mapNotNull { tu ->
            // Kiểm tra các trường bắt buộc không được null hoặc rỗng
            val tuKhoaChuan = tu.tuKhoa.trim()
            val nghiaChuan = tu.nghiaTiengViet.trim()
            if (tuKhoaChuan.isEmpty() || nghiaChuan.isEmpty()) {
                return@mapNotNull null
            }
            // Đối soát danh mục: nếu rỗng thì đưa về danh mục mặc định
            val danhMucIdChuan = tu.danhMucId.trim()
            val danhMucFinal = if (danhMucIdChuan.isEmpty()) {
                "mac_dinh"
            } else {
                // Nếu danh mục chưa tồn tại trong máy thì tự động tạo mới
                if (!tapHopIdDanhMuc.contains(danhMucIdChuan)) {
                    val tenDanhMucMoi = danhMucIdChuan.replaceFirstChar { it.uppercase() }
                    val danhMucMoi = DanhMuc(
                        id = danhMucIdChuan,
                        ten = tenDanhMucMoi,
                        moTa = "Danh mục tự động tạo khi nhập dữ liệu",
                        laMacDinh = false
                    )
                    // Lưu danh mục mới vào Room cục bộ và Firestore ngầm
                    khoDuLieuDanhMuc.luuDanhMuc(danhMucMoi)
                    // Thêm vào tập hợp để không bị trùng lặp tạo lại cho các từ vựng sau
                    tapHopIdDanhMuc.add(danhMucIdChuan)
                }
                danhMucIdChuan
            }
            // Tạo đối tượng từ vựng đã được chuẩn hóa thông tin
            tu.copy(
                tuKhoa = tuKhoaChuan,
                nghiaTiengViet = nghiaChuan,
                phienAm = tu.phienAm.trim(),
                loaiTu = tu.loaiTu.trim(),
                danhMucId = danhMucFinal
            )
        }
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
                    // Chuẩn hóa và kiểm tra tính hợp lệ của dữ liệu nhập
                    val danhSachChuanHoa = chuanHoaVaKiemTraDanhSach(danhSach)
                    if (danhSachChuanHoa.isEmpty()) {
                        _thongBaoKetQua.value = "Tệp tin JSON không chứa dữ liệu từ vựng hợp lệ"
                        return@launch
                    }
                    // Thực thi nạp dữ liệu theo chế độ đã chọn
                    val soLuongThanhCong = thucThiNapDuLieu(danhSachChuanHoa, cheDo)
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



    // Đặt lại chuỗi thông báo về rỗng sau khi SnackBar đã hiển thị xong
    fun daHienThiThongBao() {
        _thongBaoKetQua.value = ""
    }
}