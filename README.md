## Hotel Reservation & Customer Tracking System (PRJ-4)

Java 17 + Swing + MySQL mimarisiyle otel rezervasyon ve müşteri takip sistemi. Zorunlu tasarım desenleri ve CRUD katmanları uygulanmıştır.

### Mimarî
- `database/DatabaseConnection`: JDBC Singleton
- `model`: Domain modelleri, `BaseUser`, `Room` abstract sınıfları
- `factory/RoomFactory`: Oda tipleri üretimi (Factory)
- `builder/CustomerBuilder`: Customer nesnesi kurucu (Builder)
- `strategy/PricingStrategy`: Fiyat hesaplama (Strategy)
- `state/*`: Rezervasyon durum makinasi (State)
- `observer/*`: Bildirim sistemi, NotificationService (Observer)
- `dao`: CRUD/JDBC erişimi
- `service`: İş mantığı (auth, customer, room, reservation)
- `ui`: Swing arayüzleri (Login, CustomerPanel, StaffPanel)

### Veritabanı
- `src/main/resources/schema.sql` dosyasını MySQL’e uygulayın.
- `database/DatabaseConnection` içindeki URL/kullanıcı/şifreyi kendi ortamınıza göre değiştirin.

### Ekranlar
- **LoginForm**: Müşteri/Personel girişi, müşteri kaydı açma
- **CustomerPanel**: Profil güncelleme, oda arama/rezerve, rezervasyon iptal, bildirim listesi
- **StaffPanel**: Müşteri listesi, oda ekleme/listeleme, rezervasyon listesi, check-in/check-out/iptal, müşteri adına rezervasyon oluşturma

### Zorunlu Tasarım Desenleri
- Singleton: `DatabaseConnection`
- Factory: `RoomFactory`
- Observer: `observer.*`
- State: `state.*` (`Reservation` context)
- Ek desenler: Strategy (`strategy.*`), Builder (`CustomerBuilder`, `Customer.Builder`)
- Abstract sınıflar: `BaseUser`, `Room`

### Notlar
- Şifre alanları `password_hash` kolonu ile tutulur; gerçek hashing için isteğe göre eklenti yapılabilir.
- Servis katmanı UI’dan çağrılır, DAO’lar yalnızca servisler tarafından kullanılır.
