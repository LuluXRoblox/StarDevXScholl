# StarDevXSchool Offline

Aplikasi presentasi offline-first untuk Android WebView/AIDE. Tidak menambahkan dependency Android atau JavaScript eksternal.

## Struktur

- `app/src/main/assets/www/index.dat` — HTML/CSS dan aplikasi lama yang dipertahankan untuk kompatibilitas.
- `app/src/main/assets/www/editor.js` — project manager, model project multi-slide/multi-asset, editor, autosave/recovery, preview dan export mandiri.
- `app/src/main/java/.../MainActivity.java` — WebView, file chooser, dan jembatan simpan ke folder Download.

`editor.js` dimuat secara lokal dan offline. Wrapper melayani file dari `assets/www`; `index.dat` tetap menjadi entry point lama agar integrasi AIDE tidak berubah.

## Menggunakan editor

1. Pilih **New Project**, lalu **Open** project dari library.
2. Tambahkan slide, text, image/photo (termasuk small photo), sticker, atau shape. Drag untuk memindahkan, gunakan handle sudut untuk resize, dan panel kanan untuk posisi, ukuran, rotasi, opacity, font, alignment, warna, serta animasi element.
3. Shift/Ctrl-click memilih beberapa element. Toolbar mendukung copy/paste, duplicate/delete, pengaturan layer, undo/redo, slide duplicate/delete/reorder, dan background slide.
4. Project tersimpan otomatis ke IndexedDB; tombol **Save Project** menyimpan perubahan secara langsung. Snapshot recovery lokal tersedia setelah aplikasi dibuka kembali.
5. **Preview / Present** memisahkan tampilan presentasi dari UI editor; navigasi memakai tombol panah/keyboard. **Export HTML** menghasilkan satu file offline dengan asset tertanam, opening, animasi element, transition, slide, dan thank-you page.
6. **Import HTML / Project** membuka file project/HTML sebelumnya. HTML mandiri hasil export membawa metadata project agar bisa diimpor dan diedit kembali. HTML lain tetap bisa disimpan dan dipresentasikan sebagai HTML asli.

## Model data dan penyimpanan

- Project memiliki slides dan referensi asset. Setiap slide memiliki element, background, dan konfigurasi transition.
- File asset disimpan sekali pada object store IndexedDB `assets`; slide merujuk `assetId`. Penggunaan ulang asset tidak menyalin blob ke tiap slide.
- Object store project lama (`presentations`) dipertahankan. Database naik dari versi 1 ke 2 dan menambah store asset serta recovery; project/foto lama dinormalisasi saat dibuka dan foto lama dimigrasikan sebagai asset.
- Asset yang sedang dipakai tidak dapat dihapus dari Asset Library. Tidak ada pembersihan asset otomatis.
- Autosave/recovery tetap lokal di perangkat dan tidak memerlukan jaringan.

## Opening dan animation

Opening berdiri sendiri dari animasi element dan transition slide: lotus naik lalu mengambang (`fl`), diikuti title, subtitle, kemudian chips secara berurutan. File referensi `program-linear-slides2.html` tidak tersedia di repository saat implementasi, jadi motif lotus dan urutan/timing menggunakan implementasi pembuka yang sudah tertanam pada viewer lama. Thank-you page hanya menampilkan lotus dan ucapan; efek petals disembunyikan/dihapus.

## Build APK

Workflow GitHub Actions (`.github/workflows/build-apk.yml`) membangun APK. Toolchain yang disiapkan repository: JDK 17, Gradle 8.7, Android Gradle Plugin 8.5.2, compileSdk/targetSdk 34, minSdk 21. Tidak ada Gradle wrapper di checkout ini.

Untuk pengembangan browser lokal, sajikan folder `app/src/main/assets/www` melalui HTTP dan gunakan salinan `index.dat` bernama `index.html` untuk pengujian; WebView Android tetap membuka `index.dat`.
