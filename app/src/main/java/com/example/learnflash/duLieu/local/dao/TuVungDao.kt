package com.example.learnflash.duLieu.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learnflash.duLieu.local.thucThe.TuVung
import kotlinx.coroutines.flow.Flow

// Lớp dữ liệu hứng kết quả truy vấn thống kê nhóm theo danh mục từ Room
data class ThongKeDanhMuc(
    val danhMucId: String,
    val tongSo: Int,
    val soDaThuoc: Int
)

// Giao diện (Interface) định nghĩa các thao tác truy vấn với bảng tu_vung
@Dao
interface TuVungDao {

    // Lấy toàn bộ danh sách từ vựng dưới dạng luồng dữ liệu (Flow)
    @Query("SELECT * FROM tu_vung ORDER BY id DESC")
    fun layToanBoTuVung(): Flow<List<TuVung>>

    // Lấy danh sách từ vựng tới hạn ôn tập dựa trên thời gian hiện tại
    @Query("SELECT * FROM tu_vung WHERE ngayOnTapTiepTheo <= :thoiGianHienTai AND daThuoc = 0")
    fun layTuVungCanOnTap(thoiGianHienTai: Long): Flow<List<TuVung>>

    // Lấy danh sách từ vựng tới hạn ôn tập lọc theo danh mục cụ thể
    @Query("SELECT * FROM tu_vung WHERE ngayOnTapTiepTheo <= :thoiGianHienTai AND daThuoc = 0 AND danhMucId = :danhMucId")
    fun layTuVungCanOnTapTheoDanhMuc(thoiGianHienTai: Long, danhMucId: String): Flow<List<TuVung>>

    // Lấy một từ vựng cụ thể theo khóa chính ID (dùng cho màn hình Sửa)
    @Query("SELECT * FROM tu_vung WHERE id = :id LIMIT 1")
    suspend fun layTuVungTheoId(id: Int): TuVung?

    // Thực thi thao tác thêm một từ vựng mới, ghi đè nếu đã tồn tại trùng lặp ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun themTuVung(tuVung: TuVung)

    // Thực thi thao tác thêm nhiều từ vựng cùng lúc (batch insert) trong một transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun themNhieuTuVung(danhSach: List<TuVung>)

    // Thực thi thao tác cập nhật thông tin từ vựng
    @Update
    suspend fun capNhatTuVung(tuVung: TuVung)

    // Thực thi thao tác xóa một từ vựng khỏi cơ sở dữ liệu
    @Delete
    suspend fun xoaTuVung(tuVung: TuVung)

    // Trả về tổng số từ vựng hiện có dưới dạng Flow để UI theo dõi thay đổi
    @Query("SELECT COUNT(*) FROM tu_vung")
    fun demTongSoTuVung(): Flow<Int>

    // Trả về tổng số từ vựng dạng suspend — dùng một lần để kiểm tra Room trước khi tải Firebase
    @Query("SELECT COUNT(*) FROM tu_vung")
    suspend fun demTongSoTuVungMacDinh(): Int

    // Trả về số từ vựng đã được đánh dấu là thuộc (daThuoc = 1)
    @Query("SELECT COUNT(*) FROM tu_vung WHERE daThuoc = 1")
    fun demSoTuDaThuoc(): Flow<Int>

    // Trả về số từ vựng chưa thuộc (daThuoc = 0)
    @Query("SELECT COUNT(*) FROM tu_vung WHERE daThuoc = 0")
    fun demSoTuChuaThuoc(): Flow<Int>

    // Trả về giá trị trung bình cấp độ SRS của toàn bộ từ vựng
    @Query("SELECT AVG(capDoSrs) FROM tu_vung")
    fun tinhTrungBinhCapDoSrs(): Flow<Double?>

    // Trả về giá trị cấp độ SRS cao nhất (Max)
    @Query("SELECT MAX(capDoSrs) FROM tu_vung")
    fun layCaoNhatCapDoSrs(): Flow<Int?>

    // Trả về giá trị cấp độ SRS thấp nhất (Min)
    @Query("SELECT MIN(capDoSrs) FROM tu_vung")
    fun layThapNhatCapDoSrs(): Flow<Int?>

    // Trả về số từ vựng tới hạn ôn tập trong ngày hôm nay
    @Query("SELECT COUNT(*) FROM tu_vung WHERE ngayOnTapTiepTheo <= :cuoiNgayHomNay AND daThuoc = 0")
    fun demTuVungOnTapHomNay(cuoiNgayHomNay: Long): Flow<Int>

    // Trả về danh sách thống kê số từ đã thuộc và tổng số từ theo từng danh mục
    @Query("""
        SELECT danhMucId,
               COUNT(*) AS tongSo,
               SUM(CASE WHEN daThuoc = 1 THEN 1 ELSE 0 END) AS soDaThuoc
        FROM tu_vung
        GROUP BY danhMucId
    """)
    fun thongKeTheoDanhMuc(): Flow<List<ThongKeDanhMuc>>
}