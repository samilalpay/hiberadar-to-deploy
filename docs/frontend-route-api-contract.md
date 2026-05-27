# HibeRadar Frontend Route + API Contract (Guest/Firm/Admin)

Bu doküman, mevcut backend ve PRD gereksinimlerine göre frontend bilgi mimarisi, route yetkileri ve API sözleşmesini netleştirir.

## 1) Ürün Tipi Kararı

Platform bir **web uygulamasıdır**.
- Public katmanda: landing + teşvik robotu (guest arama)
- Auth katmanda: firma paneli
- Admin katmanda: admin/teknopark operasyon paneli

UI kabuğu:
- Public: üst menü + footer
- Authenticated: topbar + sidebar + içerik paneli
- Admin: topbar + sidebar + tablo/operasyon odaklı ekranlar

---

## 2) Role Bazlı Route Matrisi

## Guest (üyelik olmadan erişim)
- `/` → Landing + ürün anlatımı + CTA
- `/robot` → Teşvik robotu (NACE, sektör, ciro, çalışan, faaliyet alanı)
- `/grants` → Public hibe listesi
- `/grants/:id` → Public hibe kısa detay
- `/login`
- `/register`

Not: Guest kullanıcı başvuru/randevu yapmak istediğinde login/register’a yönlendirilir.

## Firm (FIRMA)
- `/app/dashboard`
- `/app/profile` (zorunlu tamamlama)
- `/app/grants` (aktif/kapalı sekmeli)
- `/app/grants/:id`
- `/app/grants/matches`
- `/app/applications`
- `/app/applications/:id/meeting`
- `/app/notifications`
- `/app/pre-analysis/new`
- `/app/pre-analysis/history`

## Admin / Teknopark
- `/admin/dashboard`
- `/admin/firm-registrations`
- `/admin/grants`
- `/admin/grants/new`
- `/admin/grants/:id`
- `/admin/applications`
- `/admin/applications/:id`
- `/admin/pre-analysis`
- `/admin/sources`
- `/admin/ingest`

---

## 3) Ekran Davranış Kuralları (PRD uyumlu)

- **Kapalı çağrılar listede görünür** fakat kart buzlu/düşük opak ve tıklanamaz.
- **Aktif çağrılar tıklanabilir** ve detay sayfasına gider.
- Detay sayfasında üç ana aksiyon:
  1. Resmi kaynağa git
  2. Uzmanla görüşme planla
  3. Ön analiz talebi oluştur
- Üye olmayan kullanıcı arama ve liste görebilir; işlem adımlarında auth zorunlu olur.

---

## 4) Frontend State Modeli

## Global State
- `auth`: token, role, username, profileCompleted
- `ui`: sidebar açık/kapalı, theme, locale

## Server State (query cache)
- grants list + filters + pagination
- grant detail
- matches
- applications + histories + meeting slots
- notifications + unread count
- pre-analysis list
- admin: firm registrations, grants, sources, applications, ingest

## Form State
- robot filtre formu
- profil formu
- pre-analysis formu
- admin grant/source create-update formları

---

## 5) API Sözleşmesi (Mevcut endpointlere göre)

## Auth
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

## Firm Registration
- `POST /api/firm-registrations` (guest)
- `GET /api/admin/firm-registrations`
- `GET /api/admin/firm-registrations/{id}`
- `PATCH /api/admin/firm-registrations/{id}/review`

## Grants
- `GET /api/grants`
- `GET /api/grants/{id}`
- `GET /api/grants/matches/me`
- `POST /api/admin/grants`
- `PUT /api/admin/grants/{id}`
- `PATCH /api/admin/grants/{id}/status`
- `PATCH /api/admin/grants/{id}/active`
- `GET /api/admin/grants/{id}`

## Applications + Meeting
- `POST /api/applications`
- `GET /api/applications/me`
- `GET /api/applications/{id}/meeting-slots`
- `PATCH /api/applications/{id}/meeting`
- `GET /api/admin/applications`
- `GET /api/admin/applications/{id}`
- `GET /api/admin/applications/{id}/history`
- `PATCH /api/admin/applications/{id}/status`
- `POST /api/admin/applications/{id}/meeting-slots`
- `PATCH /api/admin/applications/{id}/meeting`

## Notifications
- `GET /api/notifications/me`
- `GET /api/notifications/me/unread-count`
- `PATCH /api/notifications/{id}/read`

## Pre-analysis
- `POST /api/pre-analysis`
- `GET /api/pre-analysis/me`
- `GET /api/admin/pre-analysis`
- `PATCH /api/admin/pre-analysis/{id}`

## Sources / Ingest
- `GET /api/sources`
- `POST /api/admin/sources`
- `PUT /api/admin/sources/{id}`
- `POST /api/admin/ingest/run`
- `GET /api/admin/ingest/status`
- `GET /api/admin/ingest/jobs`
- `GET /api/admin/ingest/metrics`

---

## 6) Backend’de Gerekli Değişiklik (Guest Robot için)

Mevcut security konfigürasyonuna göre `anyRequest().authenticated()` aktif olduğu için guest kullanıcı `/api/grants` ve `/api/grants/{id}` endpointlerine erişemez.

Guest robot için aşağıdaki endpointler public olmalı:
- `GET /api/grants`
- `GET /api/grants/{id}`

Önerilen değişiklik yeri:
- `src/main/java/com/hiberadar/common/security/SecurityConfig.java`

Önerilen yaklaşım:
- Sadece yukarıdaki iki GET endpointini `permitAll` yap
- Diğer tüm endpointler mevcut rol korumalarıyla devam etsin

---

## 7) Scraper Kararı (Kapsam Dışı)

PRD’de geçen kaynak tarama/scraping kısmı bu fazda kapsam dışıdır.
- Kaynak yönetimi admin ekranından manuel yapılır.
- Hibe girişleri admin tarafından manuel oluşturulur/güncellenir.
- Ingest ekranı operasyonel görünürlük için kalabilir; scraping bağımlılığı olmadan kullanılmalıdır.

---

## 8) MVP Öncelik Sırası

1. Public robot + grants public list/detail
2. Auth + profile completion
3. Firm grants/applications/meeting/notifications/pre-analysis
4. Admin grants/sources/applications/pre-analysis
5. Admin ingest dashboard

Bu sıralama ile hem PRD hedefleri hem mevcut backend olgunluğu en hızlı şekilde üretime yaklaşır.
