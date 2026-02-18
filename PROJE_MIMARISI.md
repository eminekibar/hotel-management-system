# PROJE_MİMARİSİ.md

## Genel Bakış

Bu projenin ana hedefi, otellerin işletimini kolaylaştırmak ve oda ve rezervasyon yönetimi yapmak sağlamaktır. Projede birçok sınıf ve paket bulunuyor, bu nedenle detaylı bir bakış gerekmektedir.

## Teknoloji Yığını

Projemiz Java dili ile yazılmış ve Maven build yönetim sistemi kullanılmıştır. Docker ve Jenkins de kullanılacak yapılandırma dosyaları bulunmaktadır. Ayrıca SQL veritabanı kullanılacaktır.

- **Java**: Yazılımın temel dilidir.
- **Maven**: Proje yönetimi ve derleme aracıdır.
- **Docker**: Uygulamanın dağıtımını kolaylaştıran containerization teknolojisi.
- **Jenkins**: Otomatikleştirilmiş test ve deployment sürecini yönetmek için kullanılır.

## Modüller

Projemiz birçok modül içeriyor ve her biri belirli işlevleri gerçekleştirmektedir. Aşağıda projenin ana modülleri ve onların işlevlerini detaylı bir şekilde açıklamış olacağım:

### Model (model)

Bu paket, uygulamanın veri modelini tanımlar. Burada belirlenen sınıflar, veritabanındaki tabloları temsil ediyor.

- `room`: Oda türlerini ve durumlarını temsil eder.
- `user`: Kullanıcıları temsil eder (Müşteri, Müşteri Temsilcisi, Personel).
- `reservation`: Rezervasyon bilgilerini temsil eder.

### Dao (dao)

Bu paket, veritabanı işlemlerini gerçekleştiren data access object sınıflarını içeriyor. Burası, uygulamanın veritabanına erişimi yönetmek için kullanılır.

- `CustomerDAO`
- `NotificationDAO`
- `ReservationActionDAO`
- `RoomDAO`
- `StaffDAO`

### Service (service)

Bu paket, iş kurallarını ve iş mantığını içeren hizmet sınıflarını içeriyor. Burası, uygulamanın iş kurallarına uygun olarak işlemeleri için kullanılır.

- `AuthService`: Yetkilendirme işlemleri
- `CustomerService`: Müşteri işlemlerini yönetir
- `NotificationQueryService`
- `ReservationService`
- `RoomService`
- `StaffService`

### Observer (observer)

Bu paket, gözlemleme desenine uygun sınıfları içeriyor. Bu desen, bir nesne değiştiğinde onu takip eden diğer nesneleri bilgilendirmek için kullanılır.

- `CustomerNotificationObserver`
- `StaffNotificationObserver`
- `NotificationService`

### State (state)

Bu paket, statelik tasarım desenine uygun sınıfları içeriyor. Bu desen, bir nesnenin durumunu ve onun davranışını değiştirmek için kullanılır.

- `ActiveState`
- `CanceledState`
- `CheckedInState`
- `CompletedState`
- `PendingState`
- `ReservationState`

### Strategy (strategy)

Bu paket, stratejik tasarım desenine uygun sınıfları içeriyor. Bu desen, belirli bir konuda farklı stratejileri uygulamak için kullanılır.

- `DefaultPricingStrategy`
- `PricingStrategy`

### UI (ui)

Bu paket, kullanıcı arayüzü sınıflarını içeriyor. Burası, uygulamanın kullanıcı ile etkileşimini yönetmek için kullanılır.

- `App.java`
- `BookStayPanel.java`
- `CustomerDetailDialog.java`
- `HistoryPanel.java`
- `LoginForm.java`
- `NotificationsPanel.java`
- `ProfilePanel.java`
- `ReservationsPanel.java`
- `StaffCustomersPanel.java`
- `StaffMembersPanel.java`
- `StaffNotificationsPanel.java`
- `StaffPanel.java`
- `StaffReservationsPanel.java`
- `StaffRoomsPanel.java`

### Util (util)

Bu paket, yardımcı sınıfları içeriyor. Burası, uygulamanın genel işlevleri için kullanılır.

- `HashUtil.java`

## Proje Yapısı

Projenin dosya ve klasör yapısı aşağıdaki gibidir:

```text
.
├── Dockerfile
├── Jenkinsfile
├── pom.xml
├── project_tree.txt
├── README.md
└── src
    └── main
        ├── java
        │   ├── builder
        │   │   └── CustomerBuilder.java
        │   ├── dao
        │   │   ├── CustomerDAO.java
        │   │   ├── NotificationDAO.java
        │   │   ├── ReservationActionDAO.java
        │   │   ├── ReservationDAO.java
        │   │   ├── RoomDAO.java
        │   │   └── StaffDAO.java
        │   ├── database
        │   │   └── DatabaseConnection.java
        │   ├── factory
        │   │   └── RoomFactory.java
        │   ├── model
        │   │   ├── Notification.java
        │   │   ├── reservation
        │   │   │   └── Reservation.java
        │   │   ├── room
        │   │   │   ├── FamilyRoom.java
        │   │   │   ├── RoomAvailabilityInfo.java
        │   │   │   ├── Room.java
        │   │   │   ├── StandardRoom.java
        │   │   │   └── SuiteRoom.java
        │   │   └── user
        │   │       ├── BaseUser.java
        │   │       ├── Customer.java
        │   │       └── Staff.java
        │   ├── observer
        │   │   ├── CustomerNotificationObserver.java
        │   │   ├── NotificationObserver.java
        │   │   ├── NotificationService.java
        │   │   └── StaffNotificationObserver.java
        │   ├── service
        │   │   ├── AuthService.java
        │   │   ├── CustomerService.java
        │   │   ├── NotificationQueryService.java
        │   │   ├── ReservationService.java
        │   │   ├── RoomService.java
        │   │   └── StaffService.java
        │   ├── state
        │   │   ├── ActiveState.java
        │   │   ├── CanceledState.java
        │   │   ├── CheckedInState.java
        │   │   ├── CompletedState.java
        │   │   ├── PendingState.java
        │   │   └── ReservationState.java
        │   ├── strategy
        │   │   ├── DefaultPricingStrategy.java
        │   │   └── PricingStrategy.java
        │   ├── ui
        │   │   ├── App.java
        │   │   ├── BookStayPanel.java
        │   │   ├── CustomerDetailDialog.java
        │   │   ├── CustomerListRenderers.java
        │   │   ├── CustomerPanel.java
        │   │   ├── CustomerRegistrationDialog.java
        │   │   ├── HistoryPanel.java
        │   │   ├── LoginForm.java
        │   │   ├── NotificationsPanel.java
        │   │   ├── ProfilePanel.java
        │   │   ├── ReservationsPanel.java
        │   │   ├── StaffCustomersPanel.java
        │   │   ├── StaffListRenderers.java
        │   │   ├── StaffMembersPanel.java
        │   │   ├── StaffNotificationsPanel.java
        │   │   ├── StaffPanel.java
        │   │   ├── StaffReservationsPanel.java
        │   │   └── StaffRoomsPanel.java
        │   └── util
        │       └── HashUtil.java
        └── resources
            └── schema.sql

19 directories, 62 files
```

Bu projenin yapıları ve işlevleri, geliştiricilerin uygulamanın nasıl çalıştığını anlamalarına yardımcı olacak. Her bir modülün işlevselliği ayrıntılı olarak açıklanmıştır, bu nedenle yeni katılanlar kolayca proje yapısına ve iş mantığına uyum sağlayabilirler.

Bu dökümanı kullanarak projeye katılacak herkesin proje yapısını ve iş mantığını anlaması sağlanacaktır. Bu, uygulamanın geliştirilme sürecinde etkili iletişim kurmanıza yardımcı olur.

