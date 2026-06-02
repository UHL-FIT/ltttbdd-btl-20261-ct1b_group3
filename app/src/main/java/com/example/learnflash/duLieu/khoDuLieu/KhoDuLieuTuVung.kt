package com.example.learnflash.duLieu.khoDuLieu

import com.example.learnflash.duLieu.local.dao.LichSuOnTapDao
import com.example.learnflash.duLieu.local.dao.TuVungDao
import com.example.learnflash.duLieu.local.thucThe.LichSuOnTap
import com.example.learnflash.duLieu.local.thucThe.TuVung
import com.example.learnflash.duLieu.local.dao.ThongKeDanhMuc
import com.example.learnflash.duLieu.remote.api.DichThuatApi
import com.example.learnflash.duLieu.remote.api.TuVungApi
import com.example.learnflash.duLieu.remote.firebase.FirebaseNguonDuLieu
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

// Lớp Repository xử lý nghiệp vụ điều phối dữ liệu giữa Local Database và Remote API
class KhoDuLieuTuVung(
    private val tuVungDao: TuVungDao,
    private val lichSuOnTapDao: LichSuOnTapDao,
    private val tuVungApi: TuVungApi,
    private val dichThuatApi: DichThuatApi,
    private val firebaseNguonDuLieu: FirebaseNguonDuLieu
) {

    // Lấy toàn bộ từ vựng dưới dạng luồng dữ liệu (Flow) để UI phản ứng với thay đổi
    fun layToanBoTuVung(): Flow<List<TuVung>> = tuVungDao.layToanBoTuVung()

    // Lấy danh sách từ vựng tới hạn ôn tập dựa trên thời gian hệ thống
    fun layTuVungCanOnTap(thoiGianHienTai: Long): Flow<List<TuVung>> =
        tuVungDao.layTuVungCanOnTap(thoiGianHienTai)

    // Lấy danh sách từ vựng tới hạn ôn tập thuộc một danh mục cụ thể
    fun layTuVungCanOnTapTheoDanhMuc(thoiGianHienTai: Long, danhMucId: String): Flow<List<TuVung>> =
        tuVungDao.layTuVungCanOnTapTheoDanhMuc(thoiGianHienTai, danhMucId)

    // Truy vấn một từ vựng theo ID để phục vụ màn hình Sửa
    suspend fun layTuVungTheoId(id: Int): TuVung? = tuVungDao.layTuVungTheoId(id)

    // Thực thi thao tác thêm hoặc cập nhật từ vựng vào Room Database và đồng bộ lên Firestore
    suspend fun luuTuVung(tuVung: TuVung) {
        if (tuVung.id == 0) {
            tuVungDao.themTuVung(tuVung)
            firebaseNguonDuLieu.themTuVungLenFirestore(tuVung)
        } else {
            tuVungDao.capNhatTuVung(tuVung)
        }
    }

    // Thực thi thao tác chèn nhiều từ vựng cùng lúc vào Room Database
    suspend fun themNhieuTuVung(danhSach: List<TuVung>) {
        tuVungDao.themNhieuTuVung(danhSach)
    }

    // Lấy toàn bộ danh sách từ vựng đồng bộ dưới dạng List phục vụ đối soát trùng lặp
    suspend fun layDanhSachToanBoTuVung(): List<TuVung> {
        return tuVungDao.layDanhSachToanBoTuVung()
    }

    // Xóa sạch toàn bộ từ vựng hiện có trong Room Database
    suspend fun xoaSachToanBoTuVung() {
        tuVungDao.xoaSachToanBoTuVung()
    }

    // Cập nhật tiến độ SRS vào Room (chỉ lưu cục bộ, không đồng bộ lên Firestore để tránh xung đột giữa các máy)
    suspend fun capNhatTienDoSrs(tuVung: TuVung) {
        tuVungDao.capNhatTuVung(tuVung)
        // firebaseNguonDuLieu.dongBoTienDoSrs(tuVung)
    }

    // Thực thi thao tác xóa từ vựng khỏi Room Database
    suspend fun xoaTuVung(tuVung: TuVung) = tuVungDao.xoaTuVung(tuVung)

    // Kiểm tra Room và tải dữ liệu mặc định từ Firestore nếu cần — trả về true khi đã sẵn sàng
    suspend fun khoiTaoDuLieuMacDinh(): Boolean {
        return try {
            firebaseNguonDuLieu.khoiTaoDuLieuMacDinh()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Thực thi tra cứu phiên âm + loại từ từ Free Dictionary API và dịch nghĩa từ MyMemory API song song
    suspend fun traCuuVaDich(tuKhoa: String): Result<TuVung> {
        return try {
            coroutineScope {
                val congViecDichThuat = async { dichThuatApi.dichVanBan(vanBanNguon = tuKhoa) }
                val congViecPhienAm = async { tuVungApi.traCuuTuVung(tuKhoa) }

                val phanHoiDich = congViecDichThuat.await()
                val phanHoiPhienAm = congViecPhienAm.await()

                val nghiaTiengViet = if (phanHoiDich.isSuccessful &&
                    phanHoiDich.body()?.maKetQua == 200) {
                    phanHoiDich.body()?.duLieuKetQua?.vanBanDaDich ?: ""
                } else ""

                val duLieuPhienAm = if (phanHoiPhienAm.isSuccessful &&
                    !phanHoiPhienAm.body().isNullOrEmpty()) {
                    phanHoiPhienAm.body()!![0]
                } else null

                val phienAm = duLieuPhienAm?.phienAm ?: ""
                val loaiTu = duLieuPhienAm?.danhSachNghia?.firstOrNull()?.loaiTu ?: ""

                if (nghiaTiengViet.isNotEmpty()) {
                    Result.success(TuVung(tuKhoa = tuKhoa, nghiaTiengViet = nghiaTiengViet, phienAm = phienAm, loaiTu = loaiTu))
                } else {
                    Result.failure(Exception("Không tìm được bản dịch tiếng Việt cho từ \"$tuKhoa\""))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối mạng: ${e.message}"))
        }
    }

    // --- Các hàm phục vụ Màn hình Thống kê ---
    fun demTongSoTuVung(): Flow<Int> = tuVungDao.demTongSoTuVung()
    fun demSoTuDaThuoc(): Flow<Int> = tuVungDao.demSoTuDaThuoc()
    fun demSoTuChuaThuoc(): Flow<Int> = tuVungDao.demSoTuChuaThuoc()
    fun tinhTrungBinhCapDoSrs(): Flow<Double?> = tuVungDao.tinhTrungBinhCapDoSrs()
    fun layCaoNhatCapDoSrs(): Flow<Int?> = tuVungDao.layCaoNhatCapDoSrs()
    fun layThapNhatCapDoSrs(): Flow<Int?> = tuVungDao.layThapNhatCapDoSrs()
    fun demTuVungOnTapHomNay(cuoiNgayHomNay: Long): Flow<Int> = tuVungDao.demTuVungOnTapHomNay(cuoiNgayHomNay)
    fun demSoLuotOnTapHomNay(batDauNgay: Long, cuoiNgay: Long): Flow<Int> = lichSuOnTapDao.demSoLuotOnTapTrongNgay(batDauNgay, cuoiNgay)
    
    // Lấy tổng số từ vựng thực tế đã ôn tập trong ngày hôm nay
    fun tinhTongSoTuDaOnHomNay(batDauNgay: Long, cuoiNgay: Long): Flow<Int?> =
        lichSuOnTapDao.tinhTongSoTuDaOnTrongNgay(batDauNgay, cuoiNgay)

    // Lấy số lượng từ vựng ở một cấp độ SRS cụ thể
    fun demSoTuTheoCapDoSrs(capDoSrs: Int): Flow<Int> =
        tuVungDao.demSoTuTheoCapDoSrs(capDoSrs)

    fun layToanBoLichSu(): Flow<List<LichSuOnTap>> = lichSuOnTapDao.layToanBoLichSu()
    suspend fun themLichSuOnTap(lichSu: LichSuOnTap) = lichSuOnTapDao.themLichSu(lichSu)

    // Trả về luồng dữ liệu thống kê số từ đã thuộc và tổng số từ theo từng danh mục
    fun thongKeTheoDanhMuc(): Flow<List<ThongKeDanhMuc>> = tuVungDao.thongKeTheoDanhMuc()
}