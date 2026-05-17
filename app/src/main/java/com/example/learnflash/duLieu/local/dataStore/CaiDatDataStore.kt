package com.example.learnflash.duLieu.local.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Mở rộng Context để khởi tạo DataStore lưu trữ dữ liệu tùy chọn
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cai_dat_ung_dung")

// Lớp quản lý các tùy chọn cấu hình của người dùng
class CaiDatDataStore(private val context: Context) {

    // Khai báo các khóa (Keys) tĩnh để thao tác với DataStore
    companion object {
        val GIAO_DIEN_TOI = booleanPreferencesKey("giao_dien_toi")
        val MUC_TIEU_HOC_NGAY = intPreferencesKey("muc_tieu_hoc_ngay")
    }

    // Biến tạo luồng dữ liệu đọc trạng thái Theme tối (Mặc định trả về false)
    val giaoDienToiFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[GIAO_DIEN_TOI] ?: false
        }

    // Biến tạo luồng dữ liệu đọc mục tiêu số từ vựng cần học mỗi ngày (Mặc định trả về 10 từ)
    val mucTieuHocNgayFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[MUC_TIEU_HOC_NGAY] ?: 10
        }

    // Hàm thực thi tác vụ lưu trạng thái giao diện tối
    suspend fun luuTrangThaiGiaoDien(laGiaoDienToi: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[GIAO_DIEN_TOI] = laGiaoDienToi
        }
    }

    // Hàm thực thi tác vụ lưu mục tiêu học hàng ngày
    suspend fun luuMucTieuHocNgay(mucTieu: Int) {
        context.dataStore.edit { preferences ->
            preferences[MUC_TIEU_HOC_NGAY] = mucTieu
        }
    }
}
