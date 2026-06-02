package com.example.learnflash

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.learnflash.dieuHuong.DieuHuongApp
import com.example.learnflash.duLieu.remote.firebase.FirebaseNguonDuLieu
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuDanhMuc
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.database.AppDatabase
import com.example.learnflash.duLieu.local.dataStore.CaiDatDataStore
import com.example.learnflash.duLieu.remote.api.DichThuatApi
import com.example.learnflash.duLieu.remote.api.TuVungApi
import com.example.learnflash.tieuChuanGiaoDien.GiaoDienLearnFlash

// Lớp Activity chính - Nơi khởi chạy và quản lý vòng đời ứng dụng hệ thống
class MainActivity : ComponentActivity() {

    // Khai báo chuỗi hằng số Tag phục vụ việc lọc logcat trên màn hình console
    private val LOG_TAG = "LearnFlash_Lifecycle"

    // Callback khởi tạo Component, được gọi khi Activity vừa được tạo ra vào Memory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(LOG_TAG, "Trạng thái: onCreate - Khởi tạo tài nguyên giao diện và các phụ thuộc hệ thống")

        // Khởi tạo hệ thống cơ sở dữ liệu Room Database (Chỉ tạo Instance một lần duy nhất)
        val database = AppDatabase.layDatabase(this)
        val tuVungDao = database.tuVungDao()
        val lichSuDao = database.lichSuOnTapDao()
        val danhMucDao = database.danhMucDao()

        // Khởi tạo công cụ Retrofit thực thi HTTP Request tra cứu phiên âm
        val api = TuVungApi.khoiTaoApi()

        // Khởi tạo công cụ Retrofit thực thi HTTP Request dịch nghĩa tiếng Việt
        val dichThuatApi = DichThuatApi.khoiTaoApi()

        // Khởi tạo lớp truy xuất Firestore — tải dữ liệu mặc định và đồng bộ SRS
        val firebaseNguonDuLieu = FirebaseNguonDuLieu(tuVungDao, danhMucDao)

        // Bơm các phụ thuộc vào Kho dữ liệu từ vựng
        val khoDuLieu = KhoDuLieuTuVung(tuVungDao, lichSuDao, api, dichThuatApi, firebaseNguonDuLieu)

        // Bơm phụ thuộc vào Kho dữ liệu danh mục
        val khoDuLieuDanhMuc = KhoDuLieuDanhMuc(danhMucDao, firebaseNguonDuLieu)

        // Khởi tạo DataStore quản lý cài đặt người dùng (Dark Mode, Mục tiêu học ngày)
        val caiDatDataStore = CaiDatDataStore(this)

        // Cài đặt nội dung giao diện Jetpack Compose lên màn hình
        setContent {
            // Thu thập StateFlow trạng thái Dark Mode từ DataStore — tự động Recompose khi thay đổi
            val giaoDienToi by caiDatDataStore.giaoDienToiFlow.collectAsState(initial = false)

            // Truyền trạng thái Dark Mode thực tế từ DataStore vào Theme
            GiaoDienLearnFlash(toanGiaoDienToi = giaoDienToi) {
                DieuHuongApp(
                    khoDuLieu = khoDuLieu,
                    khoDuLieuDanhMuc = khoDuLieuDanhMuc,
                    caiDatDataStore = caiDatDataStore
                )
            }
        }
    }

    // Callback vòng đời được gọi khi Activity bắt đầu hiển thị nhưng chưa nhận tiêu điểm
    override fun onStart() {
        super.onStart()
        Log.d(LOG_TAG, "Trạng thái: onStart - Giao diện bắt đầu hiển thị với người dùng")
    }

    // Callback vòng đời được gọi khi Activity đã sẵn sàng nhận các sự kiện tương tác chạm
    override fun onResume() {
        super.onResume()
        Log.d(LOG_TAG, "Trạng thái: onResume - Sẵn sàng bắt sự kiện tương tác chạm")
    }

    // Callback vòng đời được gọi khi Activity bị che khuất một phần hoặc mất tiêu điểm
    override fun onPause() {
        super.onPause()
        Log.d(LOG_TAG, "Trạng thái: onPause - Tạm dừng các tác vụ chiếm tài nguyên UI")
    }

    // Callback vòng đời được gọi khi Activity bị ẩn hoàn toàn khỏi màn hình hệ thống
    override fun onStop() {
        super.onStop()
        Log.d(LOG_TAG, "Trạng thái: onStop - Bắt đầu giải phóng tài nguyên hiển thị tạm thời")
    }

    // Callback vòng đời được gọi trước khi Activity bị hệ điều hành tiêu diệt hoàn toàn
    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "Trạng thái: onDestroy - Xóa bỏ hoàn toàn Activity khỏi bộ nhớ máy")
    }
}