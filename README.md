## Hotel Reservation & Customer Tracking System (PRJ-4)

Java 17 + Swing + MySQL tabanlı otel rezervasyon ve müşteri takip uygulaması. CRUD katmanları ve istenen tasarım desenleri (Singleton, Factory, Observer, State, Strategy, Builder) uygulanmıştır. Abstract sınıflar: `BaseUser`, `Room`.

### Mimari
- `database/DatabaseConnection`: JDBC Singleton
- `model`: Domain modelleri
- `factory/RoomFactory`: Oda tip üretimi (Factory)
- `builder/CustomerBuilder`: Müşteri nesnesi oluşturma (Builder)
- `strategy/PricingStrategy`: Fiyat hesaplama (Strategy)
- `state/*`: Rezervasyon durum makinesi (State)
- `observer/*`: Bildirim kancaları (Observer)
- `dao`: CRUD/JDBC erişimi
- `service`: İş mantığı (auth, customer, room, reservation)
- `ui`: Swing ekranları (Login, CustomerPanel, StaffPanel)
-
- ![Uploading use-case-java.png…]()


### Özellikler
- Giriş: kullanıcı adı/e-posta/TCKN + şifre ile müşteri veya personel girişi.
- Müşteri: profil görüntüleme/güncelleme, şifre değiştirme, tarih aralığı + kapasite + oda tipi ile müsait oda arama, rezervasyon oluşturma/iptal, geçmiş konaklamaları görüntüleme.
- Personel: müşteri arama/ekleme, oda ekleme ve durumlarını görme, rezervasyonları müşteri/oda bilgisine göre filtreleme, müşteri adına rezervasyon oluşturma (tarih aralığına göre uygun oda arama), check-in/check-out/iptal işlemleri.
- Örnek veri: `schema.sql` içinde 2 personel, 3 oda ve 3 müşteri kayıtları bulunur.

### Veritabanı
- `src/main/resources/schema.sql` dosyasını MySQL üzerinde çalıştırın.
- `database/DatabaseConnection` içindeki URL/kullanıcı/şifre değerlerini kendi ortamınıza göre düzenleyin.

### Notlar
- Parolalar basitçe `password_hash` alanında saklanır; gerçek projelerde hashing ekleyin.
- Servis katmanı UI'dan çağrılır, DAO'lar yalnızca servisler tarafından kullanılır.
