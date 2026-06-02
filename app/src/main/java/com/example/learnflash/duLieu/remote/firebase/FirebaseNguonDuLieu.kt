package com.example.learnflash.duLieu.remote.firebase

import android.util.Log
import com.example.learnflash.duLieu.local.dao.DanhMucDao
import com.example.learnflash.duLieu.local.dao.TuVungDao
import com.example.learnflash.duLieu.local.thucThe.DanhMuc
import com.example.learnflash.duLieu.local.thucThe.TuVung
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

// Lớp truy xuất và đồng bộ dữ liệu giữa Firebase Firestore và Room Database
class FirebaseNguonDuLieu(
    private val tuVungDao: TuVungDao,
    private val danhMucDao: DanhMucDao
) {
    // Hằng số định danh Tag ghi log Logcat cho các tác vụ Firebase
    private val logTag = "LearnFlash_Firebase"

    // Hằng số tên Collection trên Firestore
    private companion object {
        const val COLLECTION_DANH_MUC = "danhMuc"
        const val COLLECTION_TU_VUNG = "tuVung"
    }

    // Khởi tạo Instance Firestore với cấu hình cache offline persistent
    private val db = Firebase.firestore.also { instance ->
        val caiDat = firestoreSettings {
            // Cấu hình cache dạng persistent — dữ liệu đọc được ngay cả khi mất mạng
            setLocalCacheSettings(persistentCacheSettings {})
        }
        instance.firestoreSettings = caiDat
    }

    // Kiểm tra Room Database — nếu chưa có dữ liệu thì kéo toàn bộ từ Firestore về
    suspend fun khoiTaoDuLieuMacDinh() {
        try {
            val soTuHienCo = tuVungDao.demTongSoTuVungMacDinh()
            if (soTuHienCo == 0) {
                Log.d(logTag, "Room trống — bắt đầu tải dữ liệu mặc định từ Firestore")
                // Chạy song song 2 tác vụ tải Firestore để giảm thời gian chờ mạng
                coroutineScope {
                    val congViecDanhMuc = async { taiDanhMucTuFirestore() }
                    val congViecTuVung = async { taiTuVungTuFirestore() }
                    congViecDanhMuc.await()
                    congViecTuVung.await()
                }
                Log.d(logTag, "Tải dữ liệu mặc định từ Firestore hoàn tất")
            } else {
                Log.d(logTag, "Room đã có $soTuHienCo từ — bỏ qua bước tải dữ liệu mặc định")
            }
        } catch (e: Exception) {
            Log.e(logTag, "Lỗi khi kiểm tra hoặc tải dữ liệu mặc định: ${e.message}", e)
        }
    }

    // Tải toàn bộ danh mục từ Firestore và lưu hàng loạt vào Room bằng một transaction
    private suspend fun taiDanhMucTuFirestore() {
        val ketQua = db.collection(COLLECTION_DANH_MUC).get().await()
        // Gom toàn bộ Document thành List rồi insert 1 lần — Room tự gói trong transaction
        val danhSach = ketQua.documents.mapNotNull { doc ->
            val ten = doc.getString("ten") ?: ""
            if (ten.isEmpty()) return@mapNotNull null
            DanhMuc(
                id = doc.id,
                ten = ten,
                moTa = doc.getString("moTa") ?: "",
                laMacDinh = doc.getBoolean("laMacDinh") ?: true
            )
        }
        if (danhSach.isNotEmpty()) {
            danhMucDao.themNhieuDanhMuc(danhSach)
        }
        Log.d(logTag, "Đã tải ${danhSach.size} danh mục từ Firestore (batch insert)")
    }

    // Tải toàn bộ từ vựng từ Firestore và lưu hàng loạt vào Room bằng một transaction
    private suspend fun taiTuVungTuFirestore() {
        val ketQua = db.collection(COLLECTION_TU_VUNG).get().await()
        // Gom toàn bộ Document thành List rồi insert 1 lần thay vì 1756 lần riêng lẻ
        val danhSach = ketQua.documents.mapNotNull { doc ->
            val tuKhoa = doc.getString("tuKhoa") ?: ""
            if (tuKhoa.isEmpty()) return@mapNotNull null
            TuVung(
                tuKhoa = tuKhoa,
                nghiaTiengViet = doc.getString("nghiaTiengViet") ?: "",
                phienAm = doc.getString("phienAm") ?: "",
                loaiTu = doc.getString("loaiTu") ?: "",
                danhMucId = doc.getString("danhMucId") ?: "mac_dinh",
                // Ép các trường tiến độ học tập về giá trị mặc định lúc khởi tạo ban đầu để tránh dính tiến độ cũ trên Firestore
                capDoSrs = 0,
                ngayOnTapTiepTheo = System.currentTimeMillis(),
                daThuoc = false
            )
        }
        if (danhSach.isNotEmpty()) {
            tuVungDao.themNhieuTuVung(danhSach)
        }
        Log.d(logTag, "Đã tải ${danhSach.size} từ vựng từ Firestore (batch insert)")
    }

    // Đồng bộ tiến độ SRS của một từ vựng lên Firestore (đã tắt để giữ tiến độ học tập độc lập ở local)
    suspend fun dongBoTienDoSrs(tuVung: TuVung) {
        // Tắt tính năng này để giữ tiến độ học tập riêng tư trên từng thiết bị
        Log.d(logTag, "Đã bỏ qua đồng bộ SRS lên Firestore cho từ: ${tuVung.tuKhoa}")
    }

    // Đẩy một từ vựng mới do người dùng tạo lên Firestore (chỉ đẩy thông tin từ, không đẩy tiến độ học SRS cục bộ)
    suspend fun themTuVungLenFirestore(tuVung: TuVung) {
        try {
            // Chuẩn hóa từ khóa cần kiểm tra loại bỏ khoảng trắng dư thừa
            val tuKhoaChuan = tuVung.tuKhoa.trim()
            // Truy vấn kiểm tra từ khóa trùng khớp chính xác trên Firestore
            val ketQuaGoc = db.collection(COLLECTION_TU_VUNG)
                .whereEqualTo("tuKhoa", tuKhoaChuan)
                .get()
                .await()
            if (!ketQuaGoc.isEmpty) {
                // Ghi nhận log và bỏ qua không tải lên nếu đã tồn tại từ trùng khớp chính xác
                Log.d(logTag, "Từ vựng đã tồn tại trên Firestore, bỏ qua tải lên: $tuKhoaChuan")
                return
            }
            // Tạo từ khóa viết thường để kiểm tra trùng lặp không phân biệt hoa thường
            val tuKhoaThuong = tuKhoaChuan.lowercase()
            if (tuKhoaChuan != tuKhoaThuong) {
                // Truy vấn kiểm tra từ khóa viết thường trên Firestore
                val ketQuaThuong = db.collection(COLLECTION_TU_VUNG)
                    .whereEqualTo("tuKhoa", tuKhoaThuong)
                    .get()
                    .await()
                if (!ketQuaThuong.isEmpty) {
                    // Bỏ qua tải lên nếu đã có từ dạng viết thường trên Firestore
                    Log.d(logTag, "Từ vựng dạng chữ thường đã tồn tại trên Firestore, bỏ qua tải lên: $tuKhoaChuan")
                    return
                }
            }
            // Tạo từ khóa viết hoa chữ cái đầu để kiểm tra đối soát
            val tuKhoaHoaDau = tuKhoaChuan.replaceFirstChar { it.uppercase() }
            if (tuKhoaChuan != tuKhoaHoaDau) {
                // Truy vấn kiểm tra từ khóa viết hoa chữ cái đầu trên Firestore
                val ketQuaHoaDau = db.collection(COLLECTION_TU_VUNG)
                    .whereEqualTo("tuKhoa", tuKhoaHoaDau)
                    .get()
                    .await()
                if (!ketQuaHoaDau.isEmpty) {
                    // Bỏ qua tải lên nếu đã có từ dạng viết hoa chữ đầu trên Firestore
                    Log.d(logTag, "Từ vựng dạng viết hoa chữ đầu đã tồn tại trên Firestore, bỏ qua tải lên: $tuKhoaChuan")
                    return
                }
            }
            // Đóng gói các thông tin từ vựng cần tải lên
            val duLieu = mapOf(
                "tuKhoa" to tuVung.tuKhoa,
                "nghiaTiengViet" to tuVung.nghiaTiengViet,
                "phienAm" to tuVung.phienAm,
                "loaiTu" to tuVung.loaiTu,
                "danhMucId" to tuVung.danhMucId
            )
            // Thực thi lệnh thêm tài liệu mới lên Firestore
            db.collection(COLLECTION_TU_VUNG).add(duLieu).await()
            Log.d(logTag, "Thêm từ mới lên Firestore thành công (không kèm tiến độ SRS): ${tuVung.tuKhoa}")
        } catch (e: Exception) {
            Log.w(logTag, "Thêm từ lên Firestore thất bại: ${e.message}")
        }
    }

    // Đẩy một danh mục mới lên Firestore nếu tên danh mục chưa tồn tại trên Firestore
    suspend fun themDanhMucLenFirestore(danhMuc: DanhMuc) {
        try {
            // Chuẩn hóa tên danh mục cần kiểm tra
            val tenChuan = danhMuc.ten.trim()
            // Truy vấn kiểm tra tên danh mục trùng khớp chính xác trên Firestore
            val ketQuaGoc = db.collection(COLLECTION_DANH_MUC)
                .whereEqualTo("ten", tenChuan)
                .get()
                .await()
            if (!ketQuaGoc.isEmpty) {
                // Ghi nhận log và bỏ qua không tải lên nếu đã tồn tại danh mục trùng tên
                Log.d(logTag, "Danh mục đã tồn tại trên Firestore, bỏ qua tải lên: $tenChuan")
                return
            }
            // Tạo tên danh mục viết thường để kiểm tra trùng lặp không phân biệt hoa thường
            val tenThuong = tenChuan.lowercase()
            if (tenChuan != tenThuong) {
                // Truy vấn kiểm tra tên viết thường trên Firestore
                val ketQuaThuong = db.collection(COLLECTION_DANH_MUC)
                    .whereEqualTo("ten", tenThuong)
                    .get()
                    .await()
                if (!ketQuaThuong.isEmpty) {
                    // Bỏ qua tải lên nếu đã có danh mục trùng tên dạng viết thường
                    Log.d(logTag, "Danh mục dạng chữ thường đã tồn tại trên Firestore, bỏ qua tải lên: $tenChuan")
                    return
                }
            }
            // Tạo tên danh mục viết hoa chữ cái đầu để đối soát
            val tenHoaDau = tenChuan.replaceFirstChar { it.uppercase() }
            if (tenChuan != tenHoaDau) {
                // Truy vấn kiểm tra tên viết hoa chữ đầu trên Firestore
                val ketQuaHoaDau = db.collection(COLLECTION_DANH_MUC)
                    .whereEqualTo("ten", tenHoaDau)
                    .get()
                    .await()
                if (!ketQuaHoaDau.isEmpty) {
                    // Bỏ qua tải lên nếu đã có danh mục trùng tên dạng viết hoa chữ đầu
                    Log.d(logTag, "Danh mục dạng viết hoa chữ đầu đã tồn tại trên Firestore, bỏ qua tải lên: $tenChuan")
                    return
                }
            }
            // Đóng gói các thông tin danh mục cần tải lên
            val duLieu = mapOf(
                "ten" to danhMuc.ten,
                "moTa" to danhMuc.moTa,
                "laMacDinh" to danhMuc.laMacDinh
            )
            // Lưu thông tin danh mục lên Firestore sử dụng chính ID cục bộ làm ID tài liệu
            db.collection(COLLECTION_DANH_MUC).document(danhMuc.id).set(duLieu).await()
            Log.d(logTag, "Thêm danh mục mới lên Firestore thành công: ${danhMuc.ten}")
        } catch (e: Exception) {
            Log.w(logTag, "Thêm danh mục lên Firestore thất bại: ${e.message}")
        }
    }
}