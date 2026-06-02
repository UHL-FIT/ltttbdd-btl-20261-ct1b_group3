package com.example.learnflash.duLieu.khoDuLieu

import com.example.learnflash.duLieu.local.dao.DanhMucDao
import com.example.learnflash.duLieu.local.thucThe.DanhMuc
import com.example.learnflash.duLieu.remote.firebase.FirebaseNguonDuLieu
import kotlinx.coroutines.flow.Flow

// Lớp Repository trung gian quản lý nghiệp vụ dữ liệu danh mục từ vựng
class KhoDuLieuDanhMuc(
    private val danhMucDao: DanhMucDao,
    private val firebaseNguonDuLieu: FirebaseNguonDuLieu
) {

    // Lấy toàn bộ danh mục dưới dạng Flow để UI tự động cập nhật khi có thay đổi
    fun layToanBoDanhMuc(): Flow<List<DanhMuc>> = danhMucDao.layToanBoDanhMuc()

    // Truy vấn một danh mục theo ID — dùng khi cần hiển thị tên danh mục trên UI
    suspend fun layDanhMucTheoId(id: String): DanhMuc? = danhMucDao.layDanhMucTheoId(id)

    // Thực thi thêm mới hoặc cập nhật danh mục vào Room Database và đồng bộ lên Firestore
    suspend fun luuDanhMuc(danhMuc: DanhMuc) {
        danhMucDao.themHoacCapNhatDanhMuc(danhMuc)
        firebaseNguonDuLieu.themDanhMucLenFirestore(danhMuc)
    }

    // Thực thi xóa danh mục — chỉ cho phép xóa danh mục do người dùng tạo (laMacDinh = false)
    suspend fun xoaDanhMuc(danhMuc: DanhMuc): Result<Unit> {
        return if (danhMuc.laMacDinh) {
            Result.failure(Exception("Không thể xóa danh mục mặc định của hệ thống"))
        } else {
            danhMucDao.xoaDanhMuc(danhMuc)
            Result.success(Unit)
        }
    }

    // Kiểm tra số từ vựng còn thuộc danh mục trước khi xóa
    suspend fun demSoTuThuocDanhMuc(danhMucId: String): Int =
        danhMucDao.demSoTuThuocDanhMuc(danhMucId)
}