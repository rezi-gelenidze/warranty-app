# Warranty Vault

A Spring Boot web app for keeping receipts and warranty documents for things you buy.
It lists purchases, shows which warranties end soon, and stores uploaded files in S3-compatible storage.

## Run

Requires Docker.

```bash
docker compose up --build
```

Open http://localhost:8080. Demo data is loaded on first start.

| Service  | URL                   | Notes                                   |
|----------|-----------------------|-----------------------------------------|
| App      | http://localhost:8080 |                                         |
| Postgres | localhost:5432        | user / password / db: `warranty`        |
| RustFS   | http://localhost:9001 | storage console, login `rustfsadmin`    |

To run the app from an IDE instead, start only the dependencies with `docker compose up postgres rustfs`
and run `./mvnw spring-boot:run`. Tests use Testcontainers: `./mvnw test`.

## Configuration

All settings come from environment variables with local defaults. See `.env.example`.

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | `localhost`, `5432`, `warranty`... | PostgreSQL connection |
| `S3_ENDPOINT` | `http://localhost:9000` | S3 API endpoint |
| `S3_REGION` | `us-east-1` | Region (`auto` for R2) |
| `S3_BUCKET` | `warranty-documents` | Bucket for uploaded files |
| `S3_ACCESS_KEY`, `S3_SECRET_KEY` | `rustfsadmin` | Storage credentials |
| `S3_CREATE_BUCKET` | `false` (`true` in Compose) | Create the bucket on startup |

### Production storage (Cloudflare R2)

The app uses the S3 API, so R2 needs no code changes:

```
S3_ENDPOINT=https://<ACCOUNT_ID>.r2.cloudflarestorage.com
S3_REGION=auto
S3_ACCESS_KEY=<R2 token access key>
S3_SECRET_KEY=<R2 token secret>
S3_CREATE_BUCKET=false
```

Files are streamed through the app, so the bucket can stay private.

## Structure

```
src/main/java/com/warrantyvault
├── config       S3 client and storage properties
├── controller   MVC controllers (Thymeleaf views)
├── dto          form and view objects
├── entity       JPA entities
├── exception    exceptions and the global handler
├── repository   Spring Data repositories
└── service      business logic and transactions
src/main/resources
├── db/migration Flyway schema and demo data
├── templates    Thymeleaf pages
└── static/css   stylesheet
```

### Data model

```
Store 1 ──── * Purchase * ──── * Tag
                  1
                  │
                  *
               Document  (file metadata, bytes in object storage)
```

## Assignment checklist

| Requirement | Where |
|---|---|
| Layered packages | `controller`, `service`, `repository`, `entity` |
| Stereotype annotations | `@Controller`, `@Service`, `@Repository`, `@Configuration`, `@ControllerAdvice` |
| Constructor injection only | `final` fields with Lombok `@RequiredArgsConstructor` in every service and controller, no `@Autowired` anywhere |
| 3+ entities | `Store`, `Purchase`, `Tag`, `Document` |
| `@ManyToOne` | `Purchase.store`, `Document.purchase` |
| `@ManyToMany` | `Purchase.tags` (join table `purchase_tag`) |
| Method-name queries | `StoreRepository.findAllByOrderByNameAsc`, `TagRepository.findAllByOrderByNameAsc`, `DocumentRepository.findByPurchaseIdOrderByUploadedAtDesc`, `DocumentRepository.findByIdAndPurchaseId` |
| `@Query` queries | `PurchaseRepository.search`, `PurchaseRepository.findWarrantiesEndingBetween` |
| `@Transactional` | `PurchaseService`, `DocumentService` (write methods; read-only by default) |
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` | `PurchaseController`, `DocumentController` (HTML forms send PUT/DELETE through the hidden `_method` field) |
| `@PathVariable` | `/purchases/{id}`, `/purchases/{purchaseId}/documents/{documentId}` |
| `@RequestParam` | list filters (`q`, `storeId`, `tagId`) and the upload (`file`, `type`) |
| `@ModelAttribute` + `@Valid` | `PurchaseController.create` and `update` with `PurchaseForm` |
| JSR-303 validation | `PurchaseForm`: `@NotBlank`, `@Size`, `@NotNull`, `@DecimalMin`, `@Digits`, `@PastOrPresent`, `@Min`, `@Max` |
| File upload | `DocumentController.upload`, `MultipartFile` from a `multipart/form-data` form; file stored in the bucket, key and name saved in `document` |
| Pagination and sorting | `GET /purchases?page=0&size=10&sort=price,desc` with `Pageable` and `Sort` |
