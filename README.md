# StarDevXSchool Offline

Versi offline dari StarDevXSchool: presentasi "satu gambar besar, zoom-pan
per slide" — tapi kali ini semuanya jalan **di HP, tanpa internet**, dan
hasil akhirnya APK.

## Isi project

```
StarDevXSchoolOffline/
├── app/src/main/
│   ├── assets/www/index.dat   <- SELURUH aplikasi (HTML+CSS+JS+html2canvas)
│   ├── java/.../MainActivity.java  <- WebView + file chooser + simpan file
│   ├── AndroidManifest.xml
│   └── res/
├── app/build.gradle
├── build.gradle
└── settings.gradle
```

`assets/www/index.dat` sengaja dibuat **satu file mandiri**. Artinya file
ini juga bisa langsung kamu buka di browser HP/laptop (tanpa APK sama
sekali) untuk coba-coba — satu-satunya bedanya, tanpa wrapper Android,
tombol Export akan memicu "download" browser biasa, bukan menulis
langsung ke folder Download/StarDevXSchool seperti di APK.

## Cara pakai aplikasinya

- **Beranda** — daftar presentasi tersimpan, tombol "+ Baru" dan "Import".
- **Buat** — upload foto (langsung jadi 1 kanvas), atau upload/tempel kode
  HTML (difoto otomatis pakai html2canvas yang sudah dibundel, jadi tetap
  jalan tanpa internet).
- **Edit** — klik-tarik di kanvas untuk gambar kotak slide baru, tarik
  kotak untuk pindah, tarik sudut untuk resize, atur urutan, simpan.
- **Tampilkan** — presentasi fullscreen dengan animasi zoom-pan, navigasi
  tombol/keyboard/swipe/fullscreen.
- **Export** (di Beranda) — menyimpan presentasi jadi SATU file `.json`
  nyata (gambar ikut ter-embed di dalamnya) ke `Download/StarDevXSchool/`
  di HP. File ini bisa kamu kirim ke HP lain / backup / taruh di Drive.
- **Import** — ambil file `.json` itu kembali, presentasinya muncul lagi
  di daftar.

## Alur Foto → Rasio → Export HTML

1. **+ Baru → Upload Foto**, pilih rasio slide: **HP 9:16** (default),
   **Laptop 16:9**, atau **Bebas**. Rasio bisa diganti lagi di editor
   (kotak slide otomatis menyesuaikan dan terkunci ke rasio itu).
2. **Editor**: klik-tarik di kanvas atau tombol **+ Slide** untuk membuat
   kotak slide. Atur subjudul pembuka, pembuka lotus, penutup "Terima Kasih".
3. **Export HTML** (tujuan: ditampilkan): satu file `.html` berisi semua
   slide + animasi pembuka lotus + tombol navigasi. Buka di browser HP /
   laptop tanpa app dan tanpa internet. File ini juga bisa di-**Import**
   lagi ke app untuk diedit.
   **Backup .json** (tujuan: dipindah ke HP lain yang punya app ini).
   Keduanya disimpan ke `Download/StarDevXSchool/`.

## Upload / Import HTML (presentasi siap pakai)

- **Upload HTML** (atau **Import** file `.html` di Beranda) = HTML disimpan
  APA ADANYA. Slide, animasi, dan navigasinya tetap milik HTML itu
  sendiri; app hanya menyimpan dan membukanya fullscreen lewat tombol
  **Tampilkan** (tombol ✕ untuk kembali).
- **Export** pada presentasi HTML mengembalikan file `.html` aslinya.
- Opsi "Ubah jadi kanvas zoom-pan" di halaman Upload HTML masih ada kalau
  mau cara lama (difoto per slide lalu diedit).

## Penyimpanan (storage)

- **IndexedDB** (di dalam WebView) — tempat semua presentasi hidup
  sehari-hari, otomatis tetap ada walau app ditutup-buka lagi. Ini milik
  app ini saja (sandboxed), tidak kelihatan dari aplikasi lain.
- **Folder Download/StarDevXSchool** (storage asli HP) — baru terisi kalau
  kamu pencet **Export**. Ini "file untuk disimpan" yang sebenarnya:
  bisa dibuka file manager, dikirim lewat WhatsApp/Bluetooth, dsb.
- Penulisan ke Download ditangani native di `MainActivity.java` lewat
  `MediaStore` (Android 10+, tanpa perlu izin apa pun) atau
  `WRITE_EXTERNAL_STORAGE` (Android 9 ke bawah, minta izin saat pertama
  export).

## Build APK (via GitHub Actions)

Project ini dibuild otomatis di GitHub, tidak perlu AIDE / Android Studio.

1. Buat repo baru di GitHub, upload semua isi folder ini (termasuk folder `.github`).
2. Buka tab **Actions** → workflow **Build APK** jalan sendiri tiap push
   (atau klik **Run workflow** untuk menjalankan manual).
3. Setelah hijau, buka run-nya → bagian **Artifacts** → unduh
   `StarDevXSchoolOffline-debug` (isinya `app-debug.apk`).
4. Mau APK nempel di halaman **Releases**? Buat tag, misal `v1.1`:
   `git tag v1.1 && git push origin v1.1`.

Versi toolchain: JDK 17, Gradle 8.7, Android Gradle Plugin 8.5.2,
compileSdk/targetSdk 34, minSdk 21.

## Keterbatasan yang perlu kamu tahu

- Upload HTML tetap pakai `html2canvas` (sudah dibundel offline, ~190KB),
  jadi CSS animasi/transisi di HTML sumber hanya tertangkap sebagai
  gambar diam saat difoto — sama seperti versi web sebelumnya.
- Konvensi `window.__slideDeck = {count, goto}` (supaya HTML multi-slide
  otomatis difoto semua) tetap berlaku sama seperti versi Vercel.
- Belum ada kompresi gambar otomatis — kanvas besar + banyak presentasi
  bisa makin menuhin penyimpanan HP pelan-pelan. IndexedDB di Android
  WebView biasanya punya plafon beberapa ratus MB per app, cukup longgar
  untuk pemakaian wajar.
- Tidak ada sinkronisasi antar HP — kalau mau pindah presentasi ke HP
  lain, pakai Export lalu Import filenya di HP tujuan.

## Update: poin judul & animasi

Di editor (langkah ② Atur slide) sekarang ada:
- **Subjudul** dan **Poin di bawah judul** (satu baris = satu poin, tampil bernomor di slide awal).
- **Animasi masuk** (bawah/atas/kanan/kiri/zoom/fade) dan **Kecepatan** (cepat/normal/pelan).
Presentasi lama tetap jalan (default: naik, normal).
