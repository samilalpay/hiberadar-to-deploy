# Frontend Sayfa Referansı (Ne İşe Yarıyor?)

> Amaç: Projede oluşturulan sayfaların rolünü netleştirmek ve geliştirme sırasında karışıklığı önlemek.

## 1) Genel Not

Bu sayfaların çoğu şu an **placeholder** (iskelet) durumda.  
Yani route ve ekran kabuğu hazır; iş mantığı/API ve detay UI adım adım doldurulacak.

---

## 2) Public / Auth Sayfaları

### `LoginPage.tsx`
- **Ne işe yarar?** Kullanıcı giriş ekranı.
- **API:** `POST /api/auth/login`
- **Sonraki adım:** Form validasyonu, hata mesajları, token kaydı, role göre yönlendirme.

### `RegisterPage.tsx`
- **Ne işe yarar?** Yeni kullanıcı kayıt ekranı.
- **API:** `POST /api/auth/register`
- **Sonraki adım:** Şifre kuralları, başarı sonrası login veya yönlendirme.

### `PublicLandingPage.tsx` (veya benzer public ana sayfa)
- **Ne işe yarar?** Üye olmayan kullanıcıya ürün giriş noktası.
- **Sonraki adım:** “Teşvik Robotu” arama alanı + CTA.

### `PublicGrantSearchPage.tsx` / `PublicGrantsPage.tsx` (planlanan/varsa)
- **Ne işe yarar?** Ziyaretçi arama yapar, hibeleri listeler.
- **Kural:** Kapalı çağrılar görünür ama tıklanamaz (buzlu kart).
- **API (public yapılmalı):** `GET /api/grants`, `GET /api/grants/{id}`

---

## 3) Firma Sayfaları

### `FirmDashboardPage.tsx`
- **Ne işe yarar?** Firma özet ekranı (yaklaşan tarihler, bildirim, kısa metrikler).
- **API:** bildirim, başvuru ve uygun hibe özetleri.

### `FirmGrantsPage.tsx` / `GrantsListPage.tsx`
- **Ne işe yarar?** Filtrelenebilir hibe listesi.
- **API:** `GET /api/grants?status=...&q=...&sourceId=...&nace=...`
- **Sonraki adım:** filtre paneli, pagination, aktif/kapalı sekmeleri.

### `FirmGrantDetailPage.tsx` / `GrantDetailPage.tsx`
- **Ne işe yarar?** Hibe detay, kısa özet ve aksiyonlar.
- **API:** `GET /api/grants/{grantId}`
- **Aksiyonlar:** başvuru oluştur, ön analiz, resmi sayfaya git, randevu sürecine geç.

### `MyApplicationsPage.tsx`
- **Ne işe yarar?** Firmanın başvuru listesi.
- **API:** `GET /api/applications/me?page=&size=`

### `ApplicationMeetingPage.tsx` (veya başvuru detay içinde bölüm)
- **Ne işe yarar?** Firma meeting slotlarını görür, randevu talep eder.
- **API:**  
  - `GET /api/applications/{applicationId}/meeting-slots`  
  - `PATCH /api/applications/{applicationId}/meeting`

### `NotificationsPage.tsx`
- **Ne işe yarar?** Firma bildirim merkezi.
- **API:**  
  - `GET /api/notifications/me`  
  - `PATCH /api/notifications/{notificationId}/read`

### `PreAnalysisCreatePage.tsx`
- **Ne işe yarar?** Ön analiz formu gönderme.
- **API:** `POST /api/pre-analysis`

### `PreAnalysisHistoryPage.tsx`
- **Ne işe yarar?** Firmanın geçmiş ön analiz talepleri.
- **API:** `GET /api/pre-analysis/me`

---

## 4) Admin Sayfaları

### `AdminDashboardPage.tsx`
- **Ne işe yarar?** Operasyon özeti, bekleyen işler, hızlı aksiyonlar.
- **API:** özet kartları için admin list endpointleri (aggregate).

### `AdminFirmRegistrationsPage.tsx`
- **Ne işe yarar?** Firma kayıt taleplerini listeleme/inceleme.
- **API:**  
  - `GET /api/admin/firm-registrations`  
  - `GET /api/admin/firm-registrations/{id}`  
  - `PATCH /api/admin/firm-registrations/{id}/review`

### `AdminGrantsPage.tsx`
- **Ne işe yarar?** Manuel hibe yönetimi (liste, oluştur, güncelle, yayınla).
- **API:**  
  - `POST /api/admin/grants`  
  - `PUT /api/admin/grants/{id}`  
  - `PATCH /api/admin/grants/{id}/status`  
  - `PATCH /api/admin/grants/{id}/active`  
  - `GET /api/admin/grants/{id}`

### `AdminApplicationsPage.tsx` (veya `AdminApplicationPage.tsx`)
- **Ne işe yarar?** Tüm başvuruların yönetim listesi.
- **API:** `GET /api/admin/applications?...`

### `AdminApplicationDetailPage.tsx` (varsa)
- **Ne işe yarar?** Tek başvurunun detayı, geçmişi, karar işlemleri.
- **API:**  
  - `GET /api/admin/applications/{id}`  
  - `GET /api/admin/applications/{id}/history`  
  - `PATCH /api/admin/applications/{id}/status`

### `AdminMeetingManagementPage.tsx` (varsa)
- **Ne işe yarar?** Başvuruya slot ekleme ve randevu onayı.
- **API:**  
  - `POST /api/admin/applications/{id}/meeting-slots`  
  - `PATCH /api/admin/applications/{id}/meeting`

### `AdminPreAnalysisPage.tsx`
- **Ne işe yarar?** Gelen ön analiz taleplerini inceleme/değerlendirme.
- **API:**  
  - `GET /api/admin/pre-analysis`  
  - `PATCH /api/admin/pre-analysis/{id}`

### `AdminSourcesPage.tsx`
- **Ne işe yarar?** Kaynak yönetimi (manuel kaynak CRUD).
- **API:**  
  - `GET /api/sources`  
  - `POST /api/admin/sources`  
  - `PUT /api/admin/sources/{id}`

### `AdminIngestOpsPage.tsx`
- **Ne işe yarar?** Ingest operasyonlarını izleme/çalıştırma.
- **API:**  
  - `POST /api/admin/ingest/run`  
  - `GET /api/admin/ingest/status`  
  - `GET /api/admin/ingest/jobs`  
  - `GET /api/admin/ingest/metrics`
- **Not:** Scraper kullanılmayacaksa bu ekran “manuel veri operasyon log/izleme” odağına çekilir.

---

## 5) Sistem Sayfaları (Genel)

### `UnauthorizedPage.tsx`
- **Ne işe yarar?** Yetkisiz erişim ekranı (401/403 durumları).

### `NotFoundPage.tsx`
- **Ne işe yarar?** Tanımsız route ekranı (404).

---

## 6) Özet

- Mevcut sayfalar doğru bir temel sağlıyor.
- Şu an ana durum: **route + layout + role guard hazır**, iş ekranları adım adım doldurulacak.
- Öncelik sırası:  
  1. Public robot + hibe liste  
  2. Login/Register gerçek API  
  3. Firma başvuru/randevu  
  4. Admin manuel hibe & başvuru yönetimi