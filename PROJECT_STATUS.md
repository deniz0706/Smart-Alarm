# SmartAlarm Test — Proje Durumu

## Uygulanan özellikler

- Türkçe, Material 3 ve koyu tema öncelikli Compose arayüzü; boş durum, yaklaşan alarm özeti, alarm kartları ve ayarlar.
- Alarm oluşturma/düzenleme/silme, saat seçimi, haftanın günleri, titreşim, erteleme süresi ve etkinlik durumu.
- DataStore üzerinde JSON tabanlı kalıcı alarm verisi ve anında gözlemlenebilir UI güncellemeleri.
- AlarmManager ile kesin alarm planlama, tekrar günü hesabı, çakışmayan ayrı erteleme intent'i, düzenlemede yeniden planlama ve kapatmada iptal.
- Açılış/paket güncellemesi sonrasında etkin alarmları yeniden planlayan alıcı.
- Bildirim/full-screen intent tarafından açılan alarm deneyimi ile Activity'den bağımsız foreground service üzerinde alarm sesi, titreşim ve erteleme. Sistem tam ekran açılışı reddederse alarm bildirimi üzerinden kullanılabilir kalır.
- Normal, toplama/çıkarma/çarpma içeren üç soruluk matematik ve dizi hatırlama görevleri. QR modu yalnızca “yakında” önizlemesi olarak gösterilir ve seçilemez.
- Tek seferlik alarm tetiklendikten sonra kalıcı veride otomatik olarak devre dışı bırakılır; tekrar eden alarm açık kalır ve bir sonraki güne planlanır.
- Android 13+ bildirim izni talebi ve kesin alarm sistem ayarına geçiş.

## Mimari

- `model`: değişmez alarm modeli ve görev türleri.
- `data`: Preferences DataStore deposu.
- `alarm`: planlayıcı, bildirim, broadcast receiver'lar, çalma foreground service'i ve görev Activity'si.
- `ui`: ekranlar, gözlemlenebilir ViewModel ve Material tema.

Bu prototip bilinçli olarak küçük, anlaşılır bir katman yapısı kullanır. ViewModel veri değişikliklerini depoya yazar ve planlayıcıyı aynı işlem akışında günceller.

## Önemli izinler

- `SCHEDULE_EXACT_ALARM`: Android 12+ cihazlarda kesin zamanlama için kullanıcı tarafından etkinleştirilebilir.
- `POST_NOTIFICATIONS`: Android 13+ alarm bildirimleri için çalışma zamanında istenir.
- `USE_FULL_SCREEN_INTENT`: çalan alarm ekranı için.
- `VIBRATE`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`: titreşim, uyandırma davranışı ve yeniden planlama için.

## Bilinen sınırlamalar

- QR tarama uygulanmamıştır ve kullanıcıya çalışan bir doğrulamaymış gibi sunulmaz: seçenek açıkça “Yakında” olarak işaretlidir ve yeni alarmlarda seçilemez. Üretimde CameraX ve ML Kit ile kayıtlı QR eşleştirmesi eklenmelidir.
- Sistem üreticileri arka plan başlatma ve pil optimizasyonu kurallarını farklı uygulayabilir.
- Tema sistemin açık/koyu tercihine uyar; uygulama içi tema seçimi henüz kalıcı ayar olarak sunulmaz.

## Fiziksel cihaz testi gerekenler

- Kilit ekranındaki tam ekran intent davranışı ve üreticiye özgü pil optimizasyonları.
- Gerçek alarm ses seviyesi, titreşim motoru ve erteleme zamanlaması.
- Yeniden başlatma sonrası `BOOT_COMPLETED` planlaması.
- Android 12–15 kesin alarm ve Android 13+ bildirim izin akışları.
