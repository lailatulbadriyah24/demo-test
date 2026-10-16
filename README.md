# Demo Test API

REST API toko online sederhana (Spring Boot 4, Java 25, PostgreSQL) yang dipakai sebagai bahan latihan **QA / API testing**.

**Fitur:** register & login (JWT), role USER / ADMIN, kategori & produk (CRUD, search, filter, sort, pagination), keranjang (cart milik user), checkout & order (pay / cancel, stok otomatis berkurang & kembali).

## Dokumentasi API
- Scalar: `/api-docs-scalar`
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/api-docs`

## Menjalankan di lokal
1. Buat database PostgreSQL bernama `demo_test` (user/password sesuaikan di `application.properties` atau lewat env var di bawah).
2. Jalankan:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
3. Buka http://localhost:9090/api-docs-scalar

Akun admin dibuat otomatis saat aplikasi start (default `admin@demo.com` / `Admin12345`). Register selalu menghasilkan role USER.

## Environment variable
| Variable | Fungsi | Default |
|---|---|---|
| `PORT` | Port server | `9090` |
| `DB_URL` | JDBC URL, contoh `jdbc:postgresql://host/db?sslmode=require` | `jdbc:postgresql://localhost:5432/demo_test` |
| `DB_USERNAME` | User database | `postgres` |
| `DB_PASSWORD` | Password database | (lihat `application.properties`) |
| `JWT_SECRET` | Secret JWT, minimal 32 karakter | nilai demo |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Akun admin yang dibuat otomatis | `admin@demo.com` / `Admin12345` |
| `SHOW_SQL` | Tampilkan SQL di log | `true` |

## Testing
Postman collection ada di `postman/demo-test.postman_collection.json` (107 request, 348 assertion: sukses, validasi, boundary, otorisasi, alur checkout).

- Postman: **Import** file tersebut, jalankan aplikasi, lalu **Run collection** (urutan request jangan diubah).
- Terminal: `npx newman run postman/demo-test.postman_collection.json`
- Untuk server online, ubah variable `baseUrl` ke URL deploy.

## Deploy gratis (Render + Neon)
1. **Database:** daftar di https://neon.tech, buat project, lalu salin connection string. Ubah ke format JDBC, misalnya:
   `jdbc:postgresql://<host>/<dbname>?sslmode=require` (username dan password diisi terpisah).
2. **Backend:** daftar di https://render.com dengan akun GitHub, pilih **New → Web Service**, pilih repo ini dan branch yang ingin dideploy. Render otomatis mendeteksi `Dockerfile`. Pilih instance type **Free**.
3. Isi **Environment Variables**: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (acak, panjang), `ADMIN_EMAIL`, `ADMIN_PASSWORD` (kuat), `SHOW_SQL=false`. `PORT` diisi otomatis oleh Render.
4. Klik **Create Web Service**. Setelah selesai, buka `https://<nama-app>.onrender.com/api-docs-scalar`.

Catatan: pada tier gratis aplikasi tidur saat tidak dipakai, jadi request pertama bisa butuh 30–60 detik.
