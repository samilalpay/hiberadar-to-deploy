# HibeRadar Frontend Sunum Notu (Guest + Firma + Admin)

Bu dokuman sunumda direkt okunabilir bir akisla hazirlandi.
Amac: urunun tum frontendini is odakli sekilde anlatmak, her rolun menu ve sayfalarini net gostermek.

## 1. Sunum Acilisi (1-2 dk)

Konusma metni:
- HibeRadar, tesvik ve hibe sureclerini tek platformda yoneten bir urun.
- Frontend tarafinda 3 ana rol deneyimi var: Guest, Firma, Admin/Teknopark.
- Her rol kendi ihtiyacina gore ayrik menu, ayrik ekran ve kontrollu yetkiyle calisiyor.

Ana mesaj:
- Guest: kesif ve ilk temas.
- Firma: basvuru ve operasyon.
- Admin: yonetim ve karar mekanizmasi.

---

## 2. Bilgi Mimarisi Ozet (1 dk)

Konusma metni:
- Public alan: herkesin gorebildigi sayfalar.
- Panel alan: giris yapmis kullanicilarin (firma/admin) calisma alani.
- Yetki kontrolu route seviyesinde uygulaniyor; firma ve admin ekranlari birbirinden ayrik.

Ana mesaj:
- Guvenlik + net kullanici deneyimi + rol bazli sadelik.

---

## 3. Guest Deneyimi (5 dk)

### 3.1 Public Ust Menu
Guest kullanici su menuleri gorur:
- Tesvik Robotu
- Hibeler
- Giris
- Kayit Ol
- Hizli aksiyon: Robotu Dene / Hibeleri Tara

Konusma metni:
- Buradaki hedef kullaniciyi kayit olmaya zorlamadan deger gostermek.
- Once robot ve hibe kesfi ile ilgiyi artiriyoruz.

### 3.2 Ana Sayfa
- Urunun deger onerisini verir.
- Kullaniciyi robot veya hibe arama akisina tasir.

### 3.3 Tesvik Robotu
- Kullaniciya hizli bir on-eslesme deneyimi sunar.
- Kisa sorularla uygun desteklerin bulunmasina yardim eder.

### 3.4 Hibeler (Public Liste)
- Acik cagrilarin kesfi.
- Arama/filtre ile dogru hibeye hizli ulasim.

### 3.5 Hibe Detay (Public)
- Program ozeti, kapsam, kritik bilgiler.
- Firma olmasi gereken noktada giris/kayit cagrisi.

### 3.6 Giris ve Kayit
- Guest'ten Firma/Admin deneyimine gecis kapisi.
- Rol bazli yonlendirme ile ilgili panele gecis.

Sunum ipucu:
- Guest bolumunu "ilk 3 dakika deger gosterimi" olarak konumlandir.

---

## 4. Firma Deneyimi (10-12 dk)

### 4.1 Firma Sol Menu (Panel)
Calisma Alani:
- Dashboard
- Tesvik Robotu
- Hibeler
- Uygun Hibeler

Takip:
- Basvurularim
- Randevular
- Bildirimler
- On Analiz

Konusma metni:
- Firma paneli "kesif -> basvuru -> randevu -> takip" zincirini uc uca yonetir.

### 4.2 Dashboard
- Firmanin o anki durumunu tek bakista gosterir.
- Kisa yol aksiyonlari ve odak alanlari.

### 4.3 Hibeler
- Filtrelenebilir liste.
- Hibe detayina gecis.

### 4.4 Uygun Hibeler
- Firma profiline gore daha hedefli liste.
- Sunumda "zaman kazanci" degerini vurgula.

### 4.5 Basvurularim
- Basvuru durumlari ve surec takibi.
- Karar notlari/surec gorunurlugu.

### 4.6 Randevularim (guclendirilen ekran)
Bolumler:
- Takvimden gun ve saat secerek randevu talebi olusturma.
- Onay bekleyen / onaylanan / gecmis randevularin ayrik gorunumu.
- Renk kodlu durum badge + timeline + filtre/siralama.

Anlatim noktasi:
- Is akisi seffafligi: hangi talep bekliyor, hangisi onaylandi, hangisi gecmise dustu net.
- Operasyonel netlik: saat cakismlari ve talepler kontrollu yonetiliyor.

### 4.7 Bildirimler
- Talep/karar/randevu degisikliklerini anlik gorme.
- Okundu-okunmadi takibi.

### 4.8 On Analiz Gecmisi
- Gecmis analiz taleplerinin izlenmesi.
- Firma tarafinda karar destek hafizasi olusturur.

### 4.9 Profil ve Sifre
- Ust barda kullanici adina tiklaninca acilan menu:
  - Profilim
  - Sifremi Guncelle
- Modern profil formu + ayri sifre guncelleme sekmesi.

Anlatim noktasi:
- Kullanici kendi hesabini panelden cikmadan yonetebiliyor.

---

## 5. Admin/Teknopark Deneyimi (10-12 dk)

### 5.1 Admin Sol Menu
Yonetim:
- Dashboard
- Firma Kayitlari
- Hibe Yonetimi
- Basvurular
- Randevu Yonetimi
- Bildirimler

Operasyon:
- On Analiz
- Kaynaklar
- Veri Akisi

Konusma metni:
- Admin panelinin hedefi: karar alma, surec hizlandirma, operasyon gorunurlugu.

### 5.2 Dashboard
- Operasyon KPI ve onceliklerin ozet ekrani.

### 5.3 Firma Kayitlari
- Gelen firma taleplerini degerlendirme (onay/red sureci).

### 5.4 Hibe Yonetimi
- Manuel hibe ekleme, guncelleme, yayinlama, aktif/pasif yonetimi.

### 5.5 Basvurular
- Tum firma basvurularini toplu goruntuleme ve durum yonetimi.

### 5.6 Randevu Yonetimi (kritik ekran)
- Onay bekleyen talepleri onay/red.
- Onayli randevuyu notla iptal etme.
- Iptalde firma tarafa bildirim gitmesi ve saatin tekrar bosalmasi.

Anlatim noktasi:
- Admin kararinin etkisi hem takvimde hem bildirimde aninda gorunur.

### 5.7 Bildirimler
- Operasyonel alarm ve talep akisinin toplu takibi.

### 5.8 On Analiz / Kaynaklar / Veri Akisi
- Analiz talepleri, kaynak butunlugu ve veri operasyon sureclerinin merkezi yonetimi.

### 5.9 Profil ve Sifre
- Firma ile ayni mantik: ad soyad/username alanindan Profilim + Sifre Guncelle.

---

## 6. Ust Bar ve Navigasyon Mantigi (2 dk)

Konusma metni:
- Sol menu: rol bazli sabit navigasyon.
- Ust bar: aktif sayfa baglami (baslik + aciklama).
- Kullanici menusu: profil ve guvenlik islemleri.
- Bildirim rozeti: eylem gerektiren durumlari one cikarir.

Ana mesaj:
- "Neredeyim, ne yapabilirim, siradaki aksiyon ne?" sorularina her ekranda cevap var.

---

## 7. Uc Deger Cikarmasi (1 dk)

Sunum kapanis metni:
- Hiz: Guest'ten firma aksiyonuna gecis hizli.
- Kontrol: Firma ve admin surecleri izlenebilir ve karar odakli.
- Guven: Rol bazli yetki, net akis, guclu takip.

---

## 8. Canli Demo Akisi (onerilen)

1) Guest olarak robot + hibe listesi
2) Giris yap ve firma paneline gec
3) Firma tarafinda: hibe detayi -> basvuru -> randevu talebi
4) Admin tarafina gec: talebi gor -> onay/red/iptal
5) Firma tarafina don: bildirim + randevu durum degisimi
6) Profilim ve sifre guncelleme akislarini goster

Toplam sure: 20-25 dk

---

## 9. Olasi Soru-Cevap Hazirligi

S: Guest kullanici nereye kadar ilerleyebilir?
C: Kesif ve deger gorme adimlarina kadar; aksiyonel islemde giris gerekir.

S: Firma ve admin neden farkli menude?
C: Is rolleri farkli oldugu icin karar kalabaligini azaltmak ve yanlisi engellemek icin.

S: Randevu cakismlari nasil yonetiliyor?
C: Takvim + durum kontrolu + admin kararlari + bildirim zinciri ile.

S: Sifre guvenligi nasil?
C: Mevcut sifre dogrulamasi, minimum kural, ayni sifre tekrarina engel ile guncellenir.

---

## 10. Sunumda Kullanabilecegin Kisa One-linerlar

- "HibeRadar'da herkes ayni urunu degil, kendi rolune uygun urun parcasini goruyor."
- "Guest kesfeder, firma aksiyon alir, admin sureci kapatir."
- "Randevu ve bildirim akisi karar sureclerini gorunur ve olculebilir yapiya getiriyor."
- "Profil ve sifre yonetimi, kullanici guveni icin panelin dogal parcasi olarak konumlandi."
