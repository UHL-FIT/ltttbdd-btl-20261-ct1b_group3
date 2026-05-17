package com.example.learnflash.duLieu.khoDuLieu

import com.example.learnflash.duLieu.local.dao.LichSuOnTapDao
import com.example.learnflash.duLieu.local.dao.TuVungDao
import com.example.learnflash.duLieu.local.thucThe.LichSuOnTap
import com.example.learnflash.duLieu.local.thucThe.TuVung
import com.example.learnflash.duLieu.remote.api.TuVungApi
import kotlinx.coroutines.flow.Flow

// Lớp Repository xử lý nghiệp vụ điều phối dữ liệu giữa Local Database và Remote API
class KhoDuLieuTuVung(
    private val tuVungDao: TuVungDao,
    private val lichSuOnTapDao: LichSuOnTapDao,
    private val tuVungApi: TuVungApi
) {

    // Lấy toàn bộ từ vựng dưới dạng luồng dữ liệu (Flow) để UI phản ứng với thay đổi
    fun layToanBoTuVung(): Flow<List<TuVung>> {
        return tuVungDao.layToanBoTuVung()
    }

    // Lấy danh sách từ vựng tới hạn ôn tập dựa trên thời gian hệ thống
    fun layTuVungCanOnTap(thoiGianHienTai: Long): Flow<List<TuVung>> {
        return tuVungDao.layTuVungCanOnTap(thoiGianHienTai)
    }

    // Thực thi thao tác thêm hoặc cập nhật từ vựng vào Room Database
    suspend fun luuTuVung(tuVung: TuVung) {
        if (tuVung.id == 0) {
            tuVungDao.themTuVung(tuVung)
        } else {
            tuVungDao.capNhatTuVung(tuVung)
        }
    }

    // Thực thi thao tác xóa từ vựng khỏi Room Database
    suspend fun xoaTuVung(tuVung: TuVung) {
        tuVungDao.xoaTuVung(tuVung)
    }

    // Thực thi thao tác tra cứu ý nghĩa trực tuyến qua Coroutines
    suspend fun traCuuTuVungTrucTuyen(tuKhoa: String): Result<TuVung> {
        return try {
            // Gọi HTTP GET để nhận phản hồi từ API
            val phanHoi = tuVungApi.traCuuTuVung(tuKhoa)

            // Kiểm tra trạng thái phản hồi và độ toàn vẹn của danh sách
            if (phanHoi.isSuccessful && !phanHoi.body().isNullOrEmpty()) {
                val duLieuRemote = phanHoi.body()!![0]

                // Trích xuất loại từ và ý nghĩa từ cấu trúc cây JSON
                val loaiTu = duLieuRemote.danhSachNghia.firstOrNull()?.loaiTu ?: ""
                val yNghia = duLieuRemote.danhSachNghia.firstOrNull()?.danhSachDinhNghia?.firstOrNull()?.dinhNghia ?: ""

                // Ánh xạ thành thực thể TuVung và trả về kết quả thành công
                val tuVungMoi = TuVung(
                    tuKhoa = duLieuRemote.tuKhoa,
                    phienAm = duLieuRemote.phienAm ?: "",
                    loaiTu = loaiTu,
                    nghiaTiengViet = yNghia
                )
                Result.success(tuVungMoi)
            } else {
                // Trả về Exception khi máy chủ báo lỗi hoặc không tìm thấy từ
                Result.failure(Exception("Không tìm thấy thông tin từ vựng trên từ điển trực tuyến."))
            }
        } catch (e: Exception) {
            // Trả về Exception nếu mất kết nối Internet hoặc lỗi định dạng dữ liệu
            Result.failure(e)
        }
    }

    // Trả về luồng dữ liệu đếm số lượng tổng từ vựng hiện có
    fun demTongSoTuVung(): Flow<Int> = tuVungDao.demTongSoTuVung()

    // Trả về luồng dữ liệu đếm số lượng từ vựng đã được học thuộc
    fun demSoTuDaThuoc(): Flow<Int> = tuVungDao.demSoTuDaThuoc()

    // Lấy luồng dữ liệu thống kê lịch sử ôn tập dưới dạng Flow
    fun layToanBoLichSu(): Flow<List<LichSuOnTap>> = lichSuOnTapDao.layToanBoLichSu()

    // Ghi nhận một phiên ôn tập mới vào Room Database
    suspend fun themLichSuOnTap(lichSu: LichSuOnTap) = lichSuOnTapDao.themLichSu(lichSu)
}
