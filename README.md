#  Hotel Management System (Otel Yönetim Sistemi)

##  Ekip Üyeleri

- **Mukadder Bölükbaşı**  
  GitHub: https://github.com/mukadderbolukbasi

- **Emine Kibar**  
  GitHub: https://github.com/eminekibar

- **Betül Yıldırım**  
  GitHub: https://github.com/betulyldrmm

---
---

##  Proje Tanımı

Bu proje, bir otel işletmesinin temel operasyonlarını yönetmek amacıyla geliştirilmiş
kapsamlı bir **Java tabanlı masaüstü (Desktop) uygulamasıdır**.  
Uygulama; temiz mimari prensipleri, katmanlı yapı, çeşitli tasarım kalıpları ve katı iş
kuralları temel alınarak tasarlanmıştır.

---

## Klasor Dallanmasi
```text
└── src
    └── main
        ├── java
        │   ├── builder
        │   ├── dao
        │   ├── database
        │   ├── factory
        │   ├── model
        │   │   ├── reservation
        │   │   ├── room
        │   │   └── user
        │   ├── observer
        │   ├── service
        │   ├── state
        │   ├── strategy
        │   ├── ui
        │   └── util
        └── resources
            └── schema.sql
```

---

##  Proje Özelliklerine Genel Bakış

**Mimari**  
Katmanlı Mimari (DAO, Service, UI) kullanılmıştır.

**Tasarım Kalıpları**  
Strategy, State, Observer, Singleton, Factory ve Builder tasarım kalıpları etkin şekilde
kullanılmıştır.

**Güvenlik**  
Kullanıcı parolaları SHA-256 algoritması ile hashlenerek saklanmaktadır.

**Veritabanı**  
MySQL kullanılarak JDBC üzerinden güçlü veri bütünlüğü ve ilişkisel yapı
sağlanmıştır.

**Kullanıcı Arayüzü**  
Java Swing ile geliştirilmiş kullanıcı dostu masaüstü (GUI) arayüzü sunulmaktadır.

---

##  Teknolojiler ve Bağımlılıklar

Bu proje, Apache Maven kullanılarak derlenen bir Java masaüstü uygulamasıdır.

### 🔹 Temel Teknolojiler

- **Java Sürümü:** Java SE 17  
- **Yapılandırma Yönetimi:** Apache Maven 4.0.0  
- **Veritabanı:** MySQL 
- **Kullanıcı Arayüzü:** Java Swing  

---

##  Mimari ve Tasarım Kalıpları

Proje, yüksek modülerlik, test edilebilirlik ve sürdürülebilirlik hedeflenerek güçlü bir
**Katmanlı Mimari** üzerine inşa edilmiştir.

### Katmanlı Mimari

- **Data Access Layer (DAO)**  
  `dao` paketi. Tüm CRUD işlemleri ve SQL sorguları bu katmanda yer alır.

- **Business Logic Layer (Service)**  
  `service` paketi. İş kuralları, validasyonlar, fiyat hesaplama ve durum geçişleri bu
  katmanda yönetilir.

- **Presentation Layer (UI)**  
  `ui` paketi. Java Swing tabanlı kullanıcı arayüzü bu katmanda yer alır.

### Kullanılan Tasarım Kalıpları

| Kalıp | Uygulama Yeri | Amaç |
|-----|--------------|------|
| State | `ReservationState` | Rezervasyon yaşam döngüsünün yönetimi |
| Strategy | `PricingStrategy` | Esnek fiyatlandırma |
| Observer | `NotificationObserver` | Bildirim mekanizması |
| Singleton | `DatabaseConnection` | Tek veritabanı bağlantısı |
| Factory | `RoomFactory` | Oda tiplerinin merkezi oluşturulması |
| Builder | `CustomerBuilder` | Karmaşık nesne oluşturma |

---

## İş Mantığı ve Veri Bütünlüğü Özellikleri

### Güvenlik ve Kimlik Yönetimi

- **Parola Hashleme:**  
  Parolalar SHA-256 algoritması ile hashlenerek saklanır.

- **Gelişmiş Validasyon:**  
  Kullanıcı adı, e-posta, ulusal kimlik numarası ve parola alanları Regex tabanlı
  kontrollerden geçirilir.

- **Çoklu Kimlik Doğrulama:**  
  Kullanıcılar; kullanıcı adı, e-posta veya ulusal kimlik numarası ile giriş yapabilir.

### Veri Bütünlüğü ve Denetlenebilirlik

- **Soft Delete:**  
  Kullanıcı hesapları fiziksel olarak silinmez, `is_active` alanı güncellenir.

- **Eylem Kayıtları (Auditing):**  
  Check-in, check-out ve iptal işlemleri `reservation_actions` tablosunda personel ID’si ile
  birlikte kayıt altına alınır.

### Rezervasyon Yönetimi

- **Oda Çakışma Kontrolü:**  
  Seçilen tarih aralığında başka aktif rezervasyon olup olmadığı SQL sorguları ile
  denetlenir.

- **Esnek Fiyatlandırma:**  
  Strategy Pattern sayesinde dinamik fiyat hesaplama yapılır.

- **Rezervasyon Durumları:**  
  - `pending`  
  - `active`  
  - `checked_in`  
  - `completed`  
  - `canceled`

---

## 💾 Veritabanı Yapısı (`schema.sql`)

| Tablo Adı | Açıklama | Kritik Sütunlar |
|----------|----------|----------------|
| `customers` | Müşteri hesapları | `username`, `email`, `national_id`, `is_active` |
| `staff` | Personel hesapları | `username`, `email`, `national_id`, `role`, `is_active` |
| `rooms` | Oda bilgileri | `room_number`, `status` |
| `reservations` | Rezervasyon kayıtları | `customer_id`, `room_id`, `status` |
| `reservation_actions` | Denetim kayıtları | `reservation_id`, `staff_id` |
| `notifications` | Sistem bildirimleri | `user_type`, `user_id`, `is_read` |

---

##  Kurulum ve Çalıştırma

### Ön Gereksinimler
- Java Development Kit (JDK) 17+
- Apache Maven
- MySQL 

### Veritabanı Kurulumu

Uygulama, MySQL uyumlu bir veritabanı kullanmaktadır.

Proje içerisinde yer alan `DatabaseConnection.java` dosyasında;
- veritabanı adresi,
- port numarası,
- kullanıcı adı
- ve şifre bilgileri

kontrol edilmelidir.

Ardından proje kök dizininde bulunan `schema.sql` dosyası çalıştırılarak
gerekli tablolar oluşturulmalıdır.

---

### Projeyi Çalıştırma

Veritabanı bağlantısı doğru şekilde yapılandırıldıktan sonra proje, Apache Maven
kullanılarak derlenip çalıştırılabilir.
Aşağıdaki adımlar, projenin derlenmesini ve ana sınıfın (ui.App) başlatılmasını sağlar.

İzlenecek Adımlar

1. Proje dizinine girin

Terminal üzerinden, pom.xml dosyasının bulunduğu proje kök dizinine geçin:

```bash
cd hotel-management-system
```

2. Projeyi derleyin ve bağımlılıkları indirin

Projeyi ilk kez çalıştırırken veya bağımlılıkları güncellediğinizde aşağıdaki komutu çalıştırın:

```bash
mvn clean install
```

3. Uygulamayı başlatın

Ana sınıf (ui.App) Maven aracılığıyla çalıştırılır:

```bash
mvn exec:java
```

Uygulama başarıyla başlatıldığında, Java Swing tabanlı giriş ekranı açılacaktır.

## 📋 Proje Diyagramları ve Detaylı Açıklamaları

### 1️-Kullanım Senaryosu Diyagramı (Use Case Diagram)

Bu diyagram, sistemde yer alan aktörleri (Customer, Staff ve Admin)
ve bu aktörlerin gerçekleştirebileceği temel işlemleri göstermektedir.

Ayrıca roller arasındaki hiyerarşik yapı
(Admin rolünün Staff rolünden türemesi) açık bir şekilde ifade edilmektedir.

Gerçekleştirilebilen başlıca işlemler:
- Profil görüntüleme
- Rezervasyon oluşturma
- Check-in / Check-out işlemleri
- Personel hesaplarını görüntüleme

![use_case_diagram](https://github.com/user-attachments/assets/e7fa0c37-73e4-429f-8d8d-a82b1d5539f8)

---

### 2️- Sıralı İşlem Diyagramı (Sequence Diagram)

Bu diyagram, rezervasyon oluşturma sürecinde sistem bileşenleri arasında
gerçekleşen etkileşimi adım adım göstermektedir.

Kullanıcı arayüzünden başlayan istek,
Service katmanları üzerinden ilerleyerek
iş kurallarının uygulanmasını sağlar.

Bu süreçte:
- Fiyat hesaplama işlemleri
- Bildirim mekanizmaları

belirli bir sıra dahilinde çalışmaktadır.

<img src="https://github.com/user-attachments/assets/83d57bd3-43c5-435e-baca-63a3de34496f" />

---

### 3️- Sınıf Diyagramı (UML Class Diagram)

Bu diyagram, sistemde yer alan temel Java sınıflarını
ve sınıflar arasındaki ilişkileri göstermektedir.

Diyagramda özellikle:
- Kalıtım (is-a) ilişkileri
- Nesneler arası ilişkilendirmeler (has-a)
- Tasarım kalıplarının sınıflara yansıması

vurgulanmaktadır.

![class_diagram](https://github.com/user-attachments/assets/51c3ef35-268c-4053-8655-867bec6736a9)

---

### 4️- Varlık–İlişki Diyagramı (ER Diagram) / Veritabanı Şeması

Bu diyagram, veritabanı tablolarının yapısını
ve tablolar arasındaki ilişkileri göstermektedir.

Öne çıkan noktalar:
- Kullanıcı ve personel tabloları
- Rezervasyon kayıtları
- İşlem geçmişini tutan denetim yapıları
- Bildirim sistemi için kullanılan ilişkisel yapı

![database_schema_erd](https://github.com/user-attachments/assets/89a11c02-f7e7-41f5-b94d-5d5bdeab4086)

