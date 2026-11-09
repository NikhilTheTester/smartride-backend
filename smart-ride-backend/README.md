# Smart Ride - Backend (Spring Boot, MVC, REST API)

## Package structure (com.smartride)
| Package | Purpose |
|---------|---------|
| `entity` | JPA entities (User, Cab, Location, Booking, SosAlert) and enums (Role, CabStatus, BookingStatus) |
| `dao` | Data Access Objects: all database queries (UserDao, CabDao, LocationDao, BookingDao, SosAlertDao) |
| `service` | Business logic (AuthService, BookingService, DriverService, AdminService, SosService, RouteOptimizerService) |
| `resource` | REST endpoints, one class per feature: AuthResource, BookingResource, CabResource, UserResource, LocationResource, SosResource, DashboardResource |
| `dto` | Request / response records |
| `config` | AuthInterceptor (token + role check), WebConfig (CORS), DataInitializer (demo data) |
| `exception`, `util` | Error handling, BCrypt password util, GPS distance util |

Passwords are hashed with **BCrypt** (spring-security-crypto).

## Run in Eclipse
1. Install JDK 17 or newer and MySQL (or use the H2 option below).
2. Eclipse -> File -> Import -> Maven -> Existing Maven Projects -> select this folder -> Finish.
3. Edit `src/main/resources/application.properties` and put your MySQL username/password.
4. Right click `SmartRideApplication.java` -> Run As -> Java Application.
5. Backend starts at http://localhost:8080 and creates the demo data on the first start.

### IMPORTANT if you ran the older version
Old users were stored with a different hash. Drop the old database once so it is recreated:
`DROP DATABASE smartride_db;` (or run `smart_ride_database.sql`) and start the app again.

### No MySQL? Use the in-memory H2 database
Run -> Run Configurations -> your app -> Arguments -> VM arguments: `-Dspring.profiles.active=h2`

## Demo logins
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@smartride.com | admin123 |
| Driver | driver1@smartride.com (also driver2, driver3) | driver123 |
| Employee | emp1@smartride.com (also emp2..emp5) | emp123 |
