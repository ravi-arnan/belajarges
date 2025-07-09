# 🎓 BelajarGes: Aplikasi To-Do List untuk Mahasiswa

<img src="https://github.com/ravi-arnan/belajarges/raw/main/screenshots/image1.jpg" alt="Tampilan BelajarGes1" height="400" width="200"/><img src="https://github.com/ravi-arnan/belajarges/raw/main/screenshots/image2.jpg" alt="Tampilan BelajarGes2" height="400" width="200"/><img src="https://github.com/ravi-arnan/belajarges/raw/main/screenshots/image3.jpg" alt="Tampilan BelajarGes3" height="400" width="200"/><img src="https://github.com/ravi-arnan/belajarges/raw/main/screenshots/image4.jpg" alt="Tampilan BelajarGes4" height="400" width="200"/><img src="https://github.com/ravi-arnan/belajarges/raw/main/screenshots/image6.jpg" alt="Tampilan BelajarGes5" height="400" width="200"/>

**BelajarGes** adalah aplikasi Android yang dirancang khusus untuk membantu mahasiswa dalam mengelola tugas-tugas kuliah mereka. Dengan aplikasi ini, mahasiswa dapat mencatat, melacak, dan mengatur semua tugas berdasarkan mata kuliah, lengkap dengan pengingat deadline agar tidak ada tugas yang terlewat.

Aplikasi ini dibangun menggunakan **Java Native** dan terintegrasi dengan **Firebase** untuk otentikasi pengguna dan penyimpanan data secara real-time di cloud.

> 🖼️ *Ganti gambar di atas dengan screenshot aplikasi Anda di folder ``*

---

## ✨ Fitur Utama

- **📘 Manajemen Mata Kuliah (CRUD):** Tambah, lihat, edit, dan hapus data mata kuliah.
- **📝 Manajemen Tugas (CRUD):** Tambah, lihat, edit, dan hapus tugas yang terkait dengan setiap mata kuliah.
- **🔐 Otentikasi Pengguna:** Login aman menggunakan Google Sign-In via Firebase Authentication.
- **☁️ Penyimpanan Cloud:** Semua data disimpan di Firebase Firestore dan terikat pada akun pengguna.
- **⏱️ Data Real-time:** Perubahan langsung tersinkronisasi di semua perangkat yang login.
- **⏰ Pengingat Deadline:** Notifikasi muncul 1 jam sebelum tugas jatuh tempo.
- **✅ Status Tugas:** Tandai tugas sebagai "selesai" dengan checkbox (text akan dicoret).
- **🧼 UI Bersih dan Intuitif:** Antarmuka ramah pengguna dengan Material Design.

---

## 🛠️ Teknologi yang Digunakan

| Kategori        | Teknologi                    |
|-----------------|------------------------------|
| Bahasa          | Java                         |
| Platform        | Android                      |
| Database        | Firebase Firestore           |
| Otentikasi      | Firebase Authentication      |
| Komponen UI     | RecyclerView, CardView       |
| Notifikasi      | AlarmManager, NotificationCompat |
| UI Framework    | Material Components, ViewBinding |

---

## 🚀 Cara Setup dan Instalasi

### 1. Prasyarat

- Android Studio (disarankan versi terbaru)
- Akun Google untuk Firebase

### 2. Clone Repositori

```bash
git clone https://github.com/ravi-arnan/belajarges.git
cd BelajarGes
```
### 3. Setup Firebase
a. Buat Proyek di Firebase

- Kunjungi Firebase Console
- Klik "Add project", ikuti langkah-langkahnya

b. Tambahkan Aplikasi Android

- Di Firebase project dashboard, klik ikon Android
- Package name: com.dicoding.belajarges
- Klik Register App

c. Unduh dan Tempatkan google-services.json

- Setelah registrasi, unduh google-services.json
- Letakkan di dalam direktori: BelajarGes/app/google-services.json

d. Aktifkan Google Sign-In

- Firebase Console → Authentication → Sign-in method
- Aktifkan Google Sign-In dan isi email support (jika diminta)

e. Tambahkan SHA-1 Fingerprint

- Di Android Studio, buka panel Gradle (kanan layar)
- Navigasi ke: Tasks → android → klik signingReport
- Salin SHA-1, tambahkan ke Firebase → Project Settings → Android app → Add fingerprint


