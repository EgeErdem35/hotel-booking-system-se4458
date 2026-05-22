# Group 1 Hotel Booking System Roadmap

Bu yol haritası, `docs/SE4458_Final_202526_02_spring.pdf` içindeki Group 1 gereksinimleri ve repo içindeki `PROJECT_DOCUMENTATION.md` dikkate alınarak hazırlanmıştır.

PDF'de Group 1 için istenen proje: Hotels.com benzeri bir Hotel Booking System. Ortak gereksinimlerde ayrıca REST API, API Gateway, versioned API, pagination, cloud database, IAM tabanlı authentication, queue, distributed cache, Dockerfile, deployment ve README/video teslimleri belirtiliyor.

## 1. Proje Hedefi

Group 1 projesi, Hotels.com benzeri servis odaklı bir Hotel Booking System geliştirmeyi hedefliyor.

Sistemin ana akışları:

- Admin otel, oda ve müsaitlik yönetir.
- Kullanıcı destinasyon, tarih ve kişi sayısına göre otel arar.
- Sistem sadece seçilen tarihlerde müsait odaları gösterir.
- Giriş yapan kullanıcıya yüzde 15 indirimli fiyat gösterilir.
- Kullanıcı otel detayından rezervasyon yapar.
- Rezervasyon oda kapasitesini düşürür.
- Rezervasyon sonrası RabbitMQ event yayınlanır.
- Notification Service event tüketir ve bildirim üretir.
- Notification Service her gece kapasite kontrolü yapar.
- Yorumlar ve puanlar DynamoDB'de tutulur.
- Otel detayları Upstash Redis ile cache'lenir.
- AI Agent, proje API'lerini kullanarak arama ve rezervasyon akışına yardımcı olur.
- Frontend tüm backend çağrılarını API Gateway üzerinden yapar.

## PDF Gereksinim Özeti

PDF'de Group 1 için doğrudan geçen ana maddeler:

- Hotel Admin Service authenticated olmalı.
- Adminler başlangıç ve bitiş tarihleri arasında oda availability ekleyip güncelleyebilmeli.
- Image upload zorunlu değil, nice-to-have.
- Kullanıcı destination, tarih ve kişi sayısına göre otel arayabilmeli.
- Sadece admin tarafından boş/müsait olarak işaretlenen odalar ilgili tarih aralığında sonuçlara gelmeli.
- Login olan kullanıcılar yüzde 15 indirimli fiyat görmeli.
- "Haritada göster" özelliği aranan otelleri göstermeli.
- Kullanıcı hotel detail page üzerinden booking yapabilmeli.
- Booking sonrası kapasite seçilen tarihler için düşmeli.
- Payment transaction gerekli değil.
- Comments ekranında yorumlar, ratings ve service bazlı dağılım grafikleri gösterilmeli.
- Notification Service nightly scheduled task yazmalı.
- Notification Service gelecek ay için kapasite yüzde 20 altına düşerse hotel adminlerini bilgilendirmeli.
- Notification Service queue'dan yeni reservation kayıtlarını çekip reservation details mesajı göndermeli.
- AI Agent ana uygulama ekranında chat window olarak yer almalı.
- AI Agent, search ve booking use case'lerini bu projede oluşturulan API'leri kullanarak yapmalı.
- Comments ayrı bir NoSQL DB'de tutulmalı.
- Hotel details Redis gibi ayrı bir distributed cache içinde tutulmalı.
- Tüm business use case'ler REST web service olarak sunulmalı.
- API'ler versionable olmalı ve gerektiğinde pagination desteklemeli.
- Authentication IAM servisinde tutulmalı; local authentication kabul edilmiyor.
- Real-time AI messaging gerekli değil.
- Dockerfile kaynakta bulunmalı, Docker image dosyası commitlenmemeli.
- SQLite yasak; cloud database service kullanılmalı.
- Public GitHub repo, README, deployed URLs, design/assumptions/issues, data models/ER ve max 5 dakikalık video teslim edilmeli.

## 2. Teslimde Gösterilmesi Gerekenler

Final teslimi için en güçlü demo senaryosu şu sırada olmalı:

1. Admin giriş yapar veya admin token ile istek atılır.
2. Admin otel oluşturur.
3. Admin oda oluşturur.
4. Admin belirli tarih aralığı için müsaitlik ekler.
5. Kullanıcı otel arar.
6. Arama sonucu sadece müsait otelleri gösterir.
7. Kullanıcı otel detayına girer.
8. Kullanıcı rezervasyon yapar.
9. Rezervasyon sonrası kapasite düşer.
10. RabbitMQ `reservation.created` event'i oluşur.
11. Notification Service event'i tüketir.
12. Kullanıcı veya admin bildirimi listeler.
13. Yorum/rating ekranı gösterilir.
14. AI chat üzerinden otel arama gösterilir.

Bu demo, projenin dağıtık sistem, servis ayrımı, queue, NoSQL, cache, auth, scheduled job ve frontend gereksinimlerini tek akışta görünür yapar.

## 3. Önceliklendirme Stratejisi

Önce çalışır MVP, sonra ekstra puan getiren parçalar.

P0 - Mutlaka çalışmalı:

- Supabase PostgreSQL şema ve seed data
- API Gateway routing
- Hotel Admin Service
- Hotel Search Service
- Booking Service
- RabbitMQ event publishing
- Notification Service RabbitMQ consumer
- Frontend arama, detay ve rezervasyon akışı
- Swagger/OpenAPI ve health endpoints

P1 - Final projesi için güçlü gereksinimler:

- Supabase Auth / JWT doğrulama
- Giriş yapan kullanıcıya yüzde 15 indirim
- DynamoDB Comments Service
- Rating summary, yıldız dağılımı ve service bazlı distribution graph
- Upstash Redis hotel detail cache
- Nightly capacity scheduled job
- Dockerfile'lar

P2 - Demo kalitesini artırır:

- AI Agent chat flow
- Map view
- AWS Amplify frontend deployment
- Elastic Beanstalk backend deployment
- CloudAMQP geçişi
- README, video script ve mimari diyagramlar

## 4. Fazlara Bölünmüş Yol Haritası

### Faz 0 - Repo Temizliği ve Temel Sağlık

Amaç: Geliştirmeye başlamadan önce proje çalışır ve anlaşılır halde olsun.

Yapılacaklar:

- `target/` klasörlerinin git'e girmediğini kontrol et.
- `.gitignore` içine frontend `.next/`, `node_modules/`, backend `target/` kurallarını doğrula.
- Frontend shadcn/Tailwind uyumluluğunu sabitle.
- Her serviste `mvn -q -DskipTests compile` çalıştığını doğrula.
- Root README'ye servis portlarını ekle.

Çıktı:

- Temiz çalışan monorepo scaffold
- Build komutları bilinen ve belgelenmiş repo

### Faz 1 - Database Tasarımı ve Seed Data

Amaç: Tüm ana servislerin kullanacağı ilişkisel veri modelini netleştirmek.

Dosyalar:

- `docs/database/001_schema.sql`
- `docs/database/002_seed.sql`
- `docs/database/README.md`

Tablolar:

- `hotels`
- `rooms`
- `room_availability`
- `bookings`
- `hotel_admins`
- `notifications`

Önemli kurallar:

- UUID primary key kullan.
- `created_at` ve `updated_at` alanları ekle.
- Destination search için index ekle.
- `room_id + date` için unique/index ekle.
- `available_count >= 0`, `price_per_night > 0`, `guest_count > 0` constraint'leri ekle.
- Demo için Istanbul, Antalya, Izmir seed data ekle.

Kabul kriteri:

- Supabase SQL editor'da schema ve seed çalışır.
- Search ve booking için yeterli örnek veri oluşur.

### Faz 2 - API Gateway

Amaç: Frontend'in tek giriş noktası olacak gateway'i ayağa kaldırmak.

Servis:

- `api-gateway-service`

Routes:

- `/api/v1/admin/**` -> `hotel-admin-service`
- `/api/v1/hotels/**` -> `hotel-search-service`
- `/api/v1/bookings/**` -> `booking-service`
- `/api/v1/comments/**` -> `comments-service`
- `/api/v1/notifications/**` -> `notification-service`
- `/api/v1/ai/**` -> `ai-agent-service`

Yapılacaklar:

- Environment variable ile servis URL'leri al.
- Authorization header forward et.
- CORS ayarını frontend için aç.
- Actuator health açık olsun.
- Swagger route bilgilerini README'ye yaz.

Kabul kriteri:

- Gateway üzerinden her servis health endpoint'ine erişilebilir.
- Frontend sadece gateway URL'sini bilir.

### Faz 3 - Hotel Admin Service

Amaç: Admin'in otel, oda ve müsaitlik verisini yönetmesi.

Servis:

- `hotel-admin-service`

Endpointler:

- `POST /api/v1/admin/hotels`
- `PUT /api/v1/admin/hotels/{hotelId}`
- `POST /api/v1/admin/hotels/{hotelId}/rooms`
- `PUT /api/v1/admin/rooms/{roomId}`
- `POST /api/v1/admin/rooms/{roomId}/availability`

Yapılacaklar:

- DTO + validation ekle.
- Supabase PostgreSQL bağlantısı kur.
- Admin auth kontrolünü ilk aşamada basit JWT doğrulama veya admin header kontrolüyle başlat, sonra Supabase JWT'ye sıkılaştır.
- Availability endpoint'i start/end date aralığını günlük kayıtlara açsın.
- Swagger açıklamalarını ekle.

Kabul kriteri:

- Admin otel oluşturabilir.
- Admin oda oluşturabilir.
- Admin 3-5 günlük availability ekleyebilir.
- Search servisinin kullanacağı veri oluşur.

### Faz 4 - Hotel Search Service

Amaç: Kullanıcının ana arama deneyimini çalıştırmak.

Servis:

- `hotel-search-service`

Endpointler:

- `GET /api/v1/hotels/search`
- `GET /api/v1/hotels/{hotelId}`
- `GET /api/v1/hotels/{hotelId}/map`

Search parametreleri:

- `destination`
- `checkIn`
- `checkOut`
- `guests`
- `page`
- `size`

Yapılacaklar:

- Seçilen tarih aralığındaki her gün için `available_count > 0` kontrol et.
- Oda kapasitesi `guests` sayısını karşılamalı.
- Pagination döndür.
- Auth header varsa yüzde 15 indirimli fiyat hesapla.
- Hotel detail response içinde oda seçenekleri ve map bilgisi dön.
- Upstash Redis cache'i önce sadece hotel detail için ekle.

Kabul kriteri:

- Müsait olmayan otel aramada çıkmaz.
- Login olmayan normal fiyat görür.
- Login olan indirimli fiyat görür.
- Otel detayları Redis cache'den okunabilir.

### Faz 5 - Booking Service

Amaç: Rezervasyonun atomik şekilde oluşturulması ve kapasitenin düşmesi.

Servis:

- `booking-service`

Endpointler:

- `POST /api/v1/bookings`
- `GET /api/v1/bookings/{bookingId}`
- `GET /api/v1/bookings/user/{userId}`

Yapılacaklar:

- Booking request validation ekle.
- Seçilen tarih aralığı için müsaitlik kontrol et.
- Transaction içinde availability düş ve booking oluştur.
- Payment ekleme; gerek yok.
- Başarılı booking sonrası RabbitMQ'ya `reservation.created` mesajı publish et.

Kabul kriteri:

- Aynı odada kapasite dolduktan sonra yeni booking engellenir.
- Booking sonrası ilgili günlerde `available_count` azalır.
- RabbitMQ management panelinde event akışı görülebilir.

### Faz 6 - Notification Service

Amaç: Queue ve scheduled job gereksinimlerini karşılamak.

Servis:

- `notification-service`

Endpointler:

- `GET /api/v1/notifications/admin/{adminId}`
- `GET /api/v1/notifications/user/{userId}`
- `POST /api/v1/notifications/test-nightly-job`

Yapılacaklar:

- RabbitMQ `reservation.created` queue consumer ekle.
- Reservation event sonrası kullanıcı bildirimi oluştur.
- `@Scheduled(cron = "0 0 2 * * *")` nightly job ekle.
- Nightly job, gelecek 30 gün kapasitesini kontrol etsin.
- Kapasite yüzde 20 altındaysa admin notification oluştursun.
- Manual test endpoint aynı job logic'ini tetiklesin.

Kabul kriteri:

- Booking sonrası notification oluşur.
- Manual nightly job endpoint'i demo için çalışır.
- Düşük kapasite admin bildirimi üretir.

### Faz 7 - Comments Service ve Rating Graph

Amaç: NoSQL gereksinimini ve rating grafiklerini tamamlamak.

Servis:

- `comments-service`

Endpointler:

- `POST /api/v1/comments`
- `GET /api/v1/comments/hotel/{hotelId}?page=0&size=10`
- `GET /api/v1/comments/hotel/{hotelId}/summary`
- `GET /api/v1/comments/hotel/{hotelId}/distribution`

DynamoDB:

- Table: `hotel_comments`
- Partition key: `hotelId`
- Sort key: `createdAt`

Yapılacaklar:

- Overall rating ve service ratings sakla.
- Pagination için DynamoDB query kullan.
- Summary response: average rating, total comment count.
- Distribution response: 1-5 yıldız sayıları ve service bazlı rating dağılımı.
- Frontend'de yorumlar için basit bar chart ve service bazlı graph göster.

Kabul kriteri:

- Yorumlar DynamoDB'de tutulur.
- Otel detayında yorum listesi, rating distribution ve service bazlı distribution görünür.

### Faz 8 - Frontend Uygulama Akışı

Amaç: Kullanıcının projeyi uçtan uca deneyimleyebilmesi.

Frontend:

- `frontend/`

Sayfalar:

- Home/search form
- Search results
- Hotel detail
- Booking confirmation
- Login/register
- Admin dashboard
- Comments/rating section
- AI chat panel
- Map view

Yapılacaklar:

- `NEXT_PUBLIC_API_BASE_URL` ile gateway'e bağlan.
- Supabase Auth client ekle.
- Logged-in kullanıcı token'ını Authorization Bearer olarak gönder.
- Search form gerçek query params ile results sayfasına gitsin.
- Results sayfası pagination desteklesin.
- Hotel detail booking form içersin.
- Admin dashboard sade ama kullanılabilir olsun.
- Comments ve rating distribution görseli ekle.

Kabul kriteri:

- Frontend üzerinden search -> detail -> booking yapılır.
- Admin frontend üzerinden otel/oda/availability ekleyebilir.
- UI basit, responsive ve demo için anlaşılır olur.

### Faz 9 - AI Agent Service

Amaç: AI gereksinimini sadece metin üretimi değil, proje API'lerini kullanarak karşılamak.

Servis:

- `ai-agent-service`

Endpoint:

- `POST /api/v1/ai/chat`

Yapılacaklar:

- Request: `sessionId`, `message`
- Mesajdan destination, checkIn, checkOut, guests çıkar.
- Eksik bilgi varsa follow-up question dön.
- Bilgi tamamsa Hotel Search Service'i çağır.
- Kullanıcı oteli onaylarsa Booking Service'i çağır.
- Session state'i ilk aşamada memory map ile tutulabilir.
- OpenAI API key env variable'dan gelsin.

Kabul kriteri:

- AI chat "I want a hotel in Istanbul..." mesajını aramaya dönüştürür.
- AI gerçekten search API çağırır.
- Onay sonrası booking API çağrılır.

### Faz 10 - Docker, Deployment ve Final Paket

Amaç: Projeyi teslim edilebilir hale getirmek.

Yapılacaklar:

- Her backend service için Dockerfile ekle.
- Frontend Dockerfile ekle.
- Root `docker-compose.yml` RabbitMQ'yu korusun.
- CloudAMQP config dokümante edilsin.
- AWS Amplify frontend deploy.
- Elastic Beanstalk backend deploy planı.
- README'ye deployed URL'ler, env vars ve demo akışı ekle.
- Mimari diyagram ve ER diagram ekle.
- En fazla 5 dakikalık video script hazırla.

Kabul kriteri:

- Repo GitHub'da anlaşılır.
- README tek başına projeyi anlatır.
- Demo videosu akış hatası olmadan izlenir.

## 5. Servis Bağımlılık Sırası

En doğru geliştirme sırası:

1. Database schema
2. Hotel Admin Service
3. Hotel Search Service
4. Booking Service
5. RabbitMQ publisher
6. Notification Service consumer
7. API Gateway route bağlantıları
8. Frontend search/detail/booking
9. Auth ve discount
10. Comments/DynamoDB
11. Redis cache
12. Scheduled job
13. AI Agent
14. Map view
15. Docker/deployment/final docs

Not: API Gateway daha erken de yapılabilir, fakat business endpointleri oluşmadan gateway sadece routing kalır. Pratik geliştirme için servis endpointlerini önce lokal portlarda çalıştırmak, sonra gateway'e bağlamak daha hızlıdır.

## 6. Önerilen Sprint Planı

### Sprint 1 - Temel Veri ve Admin

- Supabase schema
- Seed data
- Hotel Admin create/update hotel
- Room create/update
- Availability create/update
- Swagger ve health kontrolleri

### Sprint 2 - Search ve Booking MVP

- Search by destination/date/guests
- Hotel detail
- Booking create
- Capacity decrease
- RabbitMQ publish
- Notification consumer

### Sprint 3 - Frontend MVP

- Search form
- Search results
- Hotel detail
- Booking confirmation
- Basic admin dashboard
- API Gateway üzerinden bağlantı

### Sprint 4 - Required Integrations

- Supabase Auth
- Logged-in discount
- DynamoDB comments
- Rating summary/distribution
- Redis hotel detail cache
- Nightly scheduled job

### Sprint 5 - AI, Map, Deployment

- AI chat search flow
- AI booking confirmation flow
- Map view
- Dockerfiles
- AWS deploy notes
- README, diagrams, video script

## 7. Riskler ve Basit Çözümler

- Risk: Mikroservis sayısı fazla.
  Çözüm: Her serviste küçük, net endpointler; ortak library oluşturma.

- Risk: Auth fazla zaman alabilir.
  Çözüm: Önce Authorization header var/yok mantığıyla discount demo yapılır, sonra Supabase JWT doğrulama sıkılaştırılır.

- Risk: Deployment zaman alabilir.
  Çözüm: Lokal çalışan sistem + net deployment dokümantasyonu önce tamamlanır.

- Risk: AI Agent karmaşıklaşabilir.
  Çözüm: İlk sürümde sadece structured intent extraction + API call; uzun konuşma hafızası gerekmez.

- Risk: Booking race condition.
  Çözüm: Database transaction ve `available_count > 0` guard ile update yapılır.

- Risk: DynamoDB pagination zorlaşabilir.
  Çözüm: Demo için `limit` ve `lastEvaluatedKey` basit tutulur.

## 8. Her Task İçin Codex Çalışma Kuralı

Her task şu formatta ilerlemeli:

1. İlgili servis dosyalarını oku.
2. Sadece o task'ın dosyalarını değiştir.
3. Endpoint contract'ını önce yaz.
4. DTO + validation ekle.
5. Service logic ekle.
6. Controller ekle.
7. Swagger/README notu ekle.
8. Build/test çalıştır.
9. Değişen dosyaları ve test komutunu raporla.

Tek seferde tüm projeyi yaptırma. En güvenli Codex prompt sırası:

1. "Create Supabase schema and seed files under docs/database."
2. "Implement Hotel Admin Service endpoints only."
3. "Implement Hotel Search Service search and detail endpoints only."
4. "Implement Booking Service with capacity decrease."
5. "Add RabbitMQ publish and Notification consumer."
6. "Connect frontend search/detail/booking pages to API Gateway."
7. "Add Supabase Auth and logged-in discount."
8. "Add DynamoDB Comments Service."
9. "Add Redis hotel detail cache."
10. "Add AI Agent service API-calling flow."

## 9. Minimum Demo Senaryosu

Eğer zaman daralırsa sadece şu senaryoyu kusursuz yap:

1. Seed data ile Istanbul'da iki otel hazır.
2. Kullanıcı Istanbul, tarih ve 2 kişi ile arama yapar.
3. Sadece müsait otel listelenir.
4. Kullanıcı detay sayfasına gider.
5. Kullanıcı rezervasyon yapar.
6. Kapasite düşer.
7. RabbitMQ event oluşur.
8. Notification Service bildirimi üretir.
9. Swagger ekranları ve README ile servisler gösterilir.

Bu MVP, projenin ana mimari değerini kanıtlar.

## 10. Son Kontrol Listesi

- [ ] Tüm API'ler `/api/v1` altında.
- [ ] Tüm backend servislerinde Swagger var.
- [ ] Tüm backend servislerinde `/actuator/health` var.
- [ ] Main DB Supabase PostgreSQL.
- [ ] Comments DB DynamoDB.
- [ ] Hotel detail cache Upstash Redis.
- [ ] Queue RabbitMQ / CloudAMQP.
- [ ] Notification scheduled job var.
- [ ] Frontend API Gateway dışında backend çağırmıyor.
- [ ] Secrets env variable ile geliyor.
- [ ] Dockerfile'lar var.
- [ ] README final demo akışını anlatıyor.
- [ ] Video 5 dakikayı geçmiyor.
