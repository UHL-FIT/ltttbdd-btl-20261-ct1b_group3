package com.example.learnflash.duLieu.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learnflash.duLieu.local.thucThe.DanhMuc
import kotlinx.coroutines.flow.Flow

// Giao diện truy vấn dữ liệu bảng danh_muc trong Room Database
@Dao
interface DanhMucDao {

    // Lấy toàn bộ danh mục dưới dạng Flow để UI phản ứng theo thời gian thực
    @Query("SELECT * FROM danh_muc ORDER BY laMacDinh DESC, ten ASC")
    fun layToanBoDanhMuc(): Flow<List<DanhMuc>>

    // Lấy một danh mục cụ thể theo khóa chính ID
    @Query("SELECT * FROM danh_muc WHERE id = :id LIMIT 1")
    suspend fun layDanhMucTheoId(id: String): DanhMuc?

    // Thêm mới hoặc thay thế danh mục nếu ID đã tồn tại
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun themHoacCapNhatDanhMuc(danhMuc: DanhMuc)

    // Thêm nhiều danh mục cùng lúc (batch insert) trong một transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun themNhieuDanhMuc(danhSach: List<DanhMuc>)

    // Cập nhật thông tin danh mục đã có
    @Update
    suspend fun capNhatDanhMuc(danhMuc: DanhMuc)

    // Xóa một danh mục khỏi Room Database
    @Delete
    suspend fun xoaDanhMuc(danhMuc: DanhMuc)

    // Đếm số từ vựng thuộc một danh mục cụ thể
    @Query("SELECT COUNT(*) FROM tu_vung WHERE danhMucId = :danhMucId")
    suspend fun demSoTuThuocDanhMuc(danhMucId: String): Int
}