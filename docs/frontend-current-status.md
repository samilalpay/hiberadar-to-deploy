# HibeRadar Frontend Durum Dokümanı

> Son güncelleme: 03.03.2026

## 1) Bu aşamada ne yapıldı?

Frontend için **temiz ve ölçeklenebilir bir temel iskelet** kuruldu.

- Teknoloji tabanı: **React + TypeScript + Vite**
- Uygulama yapısı: katmanlı mimari
  - `src/app` → router, layout, provider
  - `src/features` → domain bazlı sayfalar (public/auth/firm/admin)
  - `src/shared` → ortak yardımcılar, UI, config
- Auth temeli:
  - Auth context + role kontrol altyapısı
  - Token saklama (local storage)
  - Route guard altyapısı (guest / authenticated / role bazlı)
- API temeli:
  - Merkezi HTTP client
  - Environment config (`VITE_API_BASE_URL`)
  - Query client altyapısı
- Layout temeli:
  - Public layout (ziyaretçi ekranları)
  - Panel layout (firma/admin ekranları)
- Kod kalite:
  - TypeScript strict mod
  - Lint & build başarılı

---

## 2) Şu anki genel durum

Durum: **Foundation tamamlandı, iş ekranları placeholder seviyesinde.**

Yani:
- Uygulama açılıyor ✅
- Routing ve layout çalışıyor ✅
- Sayfalar arası geçiş ve guard mantığı hazır ✅
- API bağlantı iskeleti hazır ✅
- Detaylı iş bileşenleri (gerçek tablolar, filtreler, formlar) sıradaki adım ⏳

---

## 3) Sayfalar ve ne işe yarıyor?

Aşağıdaki sayfalar şu an **iskelet/placeholder** seviyesinde hazırlandı:

## Public / Auth
- **Login**
  - Kullanıcı giriş ekranı
- **Register**
  - Yeni kullanıcı kayıt ekranı
- **Public Landing / Robot giriş noktası (planlanan)**
  - Üye olmayan kullanıcıya teşvik robotu ile arama deneyimi verilecek alan

## Firma Paneli
- **Firma Dashboard**
  - Firma için özet metrikler, yaklaşan işler, hızlı aksiyonlar
- **Hibe Listesi**
  - Filtrelenebilir hibe çağrıları (aktif + kapalı görünecek)
- **Hibe Detay**
  - Çağrı özeti, uygunluk, resmi bağlantı, başvuru/randevu/ön analiz aksiyonları
- **Başvurularım**
  - Firmanın yaptığı başvurular ve durumları
- **Bildirimlerim**
  - Firma bildirim listesi, okundu işaretleme
- **Ön Analiz**
  - Ön analiz formu gönderme ve geçmiş talepler

## Admin Paneli
- **Admin Dashboard**
  - Operasyon özeti, bekleyen işler, hızlı aksiyonlar
- **Firma Kayıt Talepleri**
  - Firma taleplerini inceleme/onay-red
- **Hibe Yönetimi**
  - Manuel hibe ekleme/güncelleme/yayınlama/aktif-pasif
- **Başvuru Yönetimi**
  - Başvuruları listeleme, durum güncelleme, geçmiş
- **Ön Analiz Yönetimi**
  - Gelen ön analiz taleplerini değerlendirme
- **Kaynak Yönetimi**
  - Kaynak ekleme/güncelleme
- **Ingest Operasyon**
  - Job durumu/metric ekranları (scraper devre dışı, manuel operasyon odaklı)

---

## 4) Backend ile eşleşme durumu

Postman koleksiyonuna göre frontend-backend eşleşmesi planlandı:

- Auth ✅
- Grants ✅
- Applications + Meeting ✅
- Notifications ✅
- Pre-analysis ✅
- Admin (grants/sources/applications/firm-registrations) ✅

> Not: “Üye olmayan kullanıcı arama yapabilsin” hedefi için backend’de ilgili endpointlerin public erişimi netleştirilecek.

---

## 5) Kısa teknik notlar

- `tsc --noEmit` temiz
- `npm run lint` temiz
- `npm run build` temiz
- Geliştirme çalıştırma:
  - `cd frontend`
  - `npm install`
  - `npm run dev`

---

## 6) Sonraki adımlar (önerilen sıra)

1. **Public robot + hibe liste ekranı** (aktif/kapalı kart davranışı)
2. **Login/Register + gerçek API entegrasyonu**
3. **Firma hibe detay + başvuru + meeting slot akışı**
4. **Admin hibe yönetimi (manuel giriş)**
5. **Bildirim ve ön analiz uçtan uca**
6. UI polish (responsive, boş durumlar, loading/skeleton, hata ekranları)

---

## 7) Özet

Bu aşamada frontend tarafında:
- Mimari temel doğru şekilde atıldı.
- Ürün sayfaları ve route omurgası kuruldu.
- İş kuralları uygulanacak ekranların iskeleti hazır.
- Bir sonraki adım doğrudan “placeholder → gerçek iş ekranı” dönüşümüdür.