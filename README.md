# 🏨 Hotel Management System (Otel Yönetim Sistemi)

## 👥 Ekip Üyeleri

- **Mukadder Bölükbaşı**  
  GitHub: https://github.com/mukadderbolukbasi

- **Emine Kibar**  
  GitHub: https://github.com/eminekibar

- **Betül Yıldırım**  
  GitHub: https://github.com/betulyldrmm

---

## 📌 Proje Tanımı

Bu proje, bir otel işletmesinin temel operasyonlarını yönetmek amacıyla geliştirilmiş
kapsamlı bir **Java tabanlı masaüstü (Desktop) uygulamasıdır**.  
Uygulama; temiz mimari prensipleri, katmanlı yapı, çeşitli tasarım kalıpları ve katı iş
kuralları temel alınarak tasarlanmıştır.

---

##  Proje Özelliklerine Genel Bakış

**Mimari**  
Katmanlı Mimari (DAO, Service, UI) ve Model-View-Controller (MVC) ayrımı temel alınmıştır.

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

##  Maven Bağımlılıkları (pom.xml)

| Kütüphane | GroupId | Sürüm | Amaç |
|----------|--------|-------|------|
| MySQL Connector | mysql | 8.0.33 | JDBC üzerinden veritabanı bağlantısı |
| SLF4J API | org.slf4j | 2.0.9 | Standart loglama arayüzü |
| SLF4J Simple | org.slf4j | 2.0.9 | Runtime konsol loglama |
| JUnit Jupiter | org.junit.jupiter | 5.10.0 | Birim testleri (test scope) |

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
| State | `state`, `Reservation` | Rezervasyon yaşam döngüsünün yönetimi |
| Strategy | `strategy`, `ReservationService` | Esnek fiyatlandırma |
| Observer | `observer` | Bildirim mekanizması |
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
- MySQL / MariaDB

### Veritabanı Kurulumu

`DatabaseConnection.java` dosyasındaki bağlantı bilgilerini kontrol edin:

```java
private static final String URL =
    "jdbc:mysql://127.0.0.1:3307/hotel_db?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "";


###📋 Proje Diyagramları ve Detaylı Açıklamaları ###

1️⃣ Kullanım Senaryosu Diyagramı (Use Case Diagram)

Bu diyagram, sistemin sunduğu tüm işlevleri ve bu işlevlere erişebilen aktörleri (Customer, Staff ve Admin) görselleştirir.
Ayrıca roller arasındaki hiyerarşiyi (Admin rolünün Staff’tan türemesi) ve kullanıcıların gerçekleştirebileceği temel işlemleri açıkça göstermektedir.

Örnek işlemler:

View Profile

Create Reservation

Check-in / Check-out

View Staff Accounts

![use_case_diagram](https://github.com/user-attachments/assets/e7fa0c37-73e4-429f-8d8d-a82b1d5539f8)


2️⃣ Sıralı İşlem Diyagramı (Sequence Diagram)

Bu diyagram, sistemin en kritik iş akışlarından biri olan rezervasyon oluşturma sürecini detaylı olarak göstermektedir.
Kullanıcı arayüzünden (BookStayPanel) başlayan isteğin, Service katmanları (RoomService, ReservationService) üzerinden nasıl ilerlediği adım adım açıklanır.

Bu süreçte:

Strategy Pattern → fiyat hesaplama

Observer Pattern → bildirim gönderimi

mekanizmalarının hangi sırayla tetiklendiği net biçimde gösterilmektedir.

<img width="2135" height="937" alt="sequence-java" src="https://github.com/user-attachments/assets/83d57bd3-43c5-435e-baca-63a3de34496f" />







3️⃣ Sınıf Diyagramı (UML Class Diagram)

Bu diyagram, projenin nesne yönelimli mimarisini oluşturan temel Java sınıflarını ve aralarındaki ilişkileri göstermektedir.

Öne çıkan noktalar:

Kalıtım (is-a) ilişkileri

İlişkilendirme (has-a) ilişkileri

Builder Pattern’ın Customer sınıfındaki kullanımı

State Pattern’ın Reservation sınıfındaki uygulanışı

![class_diagram](https://github.com/user-attachments/assets/51c3ef35-268c-4053-8655-867bec6736a9)




4️⃣ Varlık–İlişki Diyagramı (ER Diagram) / Veritabanı Şeması

Bu diyagram, MySQL veritabanındaki tabloların yapısını, birincil/yabancı anahtarlarını ve tablolar arası ilişkileri göstermektedir.

Özellikle:

customers, staff, reservations ve reservation_actions tabloları

Denetim (auditing) amacıyla kullanılan reservation_actions tablosu

notifications tablosundaki polimorfik ilişki (müşteri veya personel ile ilişkilendirme)

detaylı olarak vurgulanmıştır.

![database_schema_erd](https://github.com/user-attachments/assets/89a11c02-f7e7-41f5-b94d-5d5bdeab4086)

