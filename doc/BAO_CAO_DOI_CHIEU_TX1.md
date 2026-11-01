# Báo cáo đối chiếu project với đề TX1

Ngày kiểm tra: 01/11/2026  
Project: `shop-management` — Spring Boot 3.5.7, Java 21, Maven

## 1. Kết luận đối chiếu

| Yêu cầu | Kết quả | Đối chiếu code/cấu hình |
|---|---|---|
| MySQL `shop_management`, JPA/Hibernate | Đạt | MySQL Connector/J và Spring Data JPA có trong `pom.xml`; datasource mặc định trỏ `localhost:3306/shop_management`; Hibernate dùng `ddl-auto=update`, không cấu hình xóa schema. |
| Entity danh mục | Đạt | `Category` ánh xạ bảng `categories`, khóa `Long` tự tăng, `name` bắt buộc, `description`. |
| Entity sản phẩm và quan hệ | Đạt | `Product` ánh xạ bảng `products`; tên bắt buộc, giá `Double` bắt buộc và dương, `stock`, quan hệ `@ManyToOne` với `category_id`. |
| CRUD danh mục | Đạt | Controller có danh sách, form thêm/sửa, lưu và xóa; từ chối xóa danh mục còn sản phẩm và hiện thông báo. |
| CRUD sản phẩm | Đạt | Controller có danh sách, form thêm/sửa, lưu và xóa; bảng hiển thị tên danh mục. |
| Validation | Đạt | Jakarta Validation cho tên, giá và danh mục; lỗi được hiển thị lại trên form. |
| Tìm kiếm/lọc | Đạt | Tìm không phân biệt hoa thường, lọc danh mục và kết hợp cả hai; các nhánh truy vấn được unit test. |
| Thymeleaf | Đạt | Có trang danh sách/form danh mục, danh sách/form sản phẩm, menu điều hướng và thông báo validation. |
| Port local | Đạt | `server.port=8081`; ứng dụng, Docker và Kubernetes cùng dùng container/service port `8081`. |
| Postman | Đã bổ sung hướng dẫn | `doc/POSTMAN_TX1.md` ghi rõ đây là form HTML, cách gửi `x-www-form-urlencoded`, CRUD, tìm/lọc, validation và Actuator. |
| Docker Compose | Đã bổ sung bảo vệ cấu hình | Không còn mật khẩu mặc định; Compose yêu cầu biến trong `.env`. MySQL Compose dùng port host `3307` và volume riêng, không dùng MySQL/XAMPP local. |
| Kubernetes/Minikube | Đã bổ sung | Deployment/Service cho MySQL và ứng dụng, PVC MySQL, Secret tham chiếu, health probes, port `8081` và NodePort `30081`. |
| GitLab CI/CD | Đã bổ sung pipeline | Các stage build, test, package, dockerize và deploy manual có điều kiện; deploy đẩy image theo commit SHA. |
| Prometheus/Grafana | Đã bổ sung cấu hình/hướng dẫn | Actuator xuất health/metrics/Prometheus; có ServiceMonitor cho Prometheus Operator và hướng dẫn Helm/Grafana. |

Đề TX1 có tiêu đề/đoạn giới thiệu nhắc đến “đơn hàng”, nhưng phần schema chi tiết và tiêu chí chấm điểm yêu cầu `categories`, `products` và quan hệ Product–Category, không định nghĩa bảng hay thao tác đơn hàng. Project giữ đúng phạm vi schema/nghiệp vụ chi tiết này; chưa tự thêm entity đơn hàng.

## 2. Phần đã sửa/bổ sung

- [application.properties](../src/main/resources/application.properties): giữ `8081` và MySQL `shop_management`; bỏ fallback mật khẩu khỏi file, chỉ đọc `DB_PASSWORD` từ môi trường; bật health probes, endpoint metrics và nhãn metrics; tắt SQL log mặc định.
- [Dockerfile](../Dockerfile): dùng Java 21 runtime và đóng gói JAR đã build, lắng nghe port `8081`.
- [docker-compose.yml](../docker-compose.yml), [.env.example](../.env.example), [.dockerignore](../.dockerignore): yêu cầu credential do người chạy cung cấp; tạo MySQL riêng với dữ liệu lưu ở volume, tránh đụng MySQL XAMPP.
- [.gitlab-ci.yml](../.gitlab-ci.yml): thêm package, build/push image lên GitLab Registry và deploy Kubernetes manual; không lưu credential trong repository.
- [k8s/app.yaml](../k8s/app.yaml), [k8s/mysql.yaml](../k8s/mysql.yaml), [k8s/prometheus-servicemonitor.yaml](../k8s/prometheus-servicemonitor.yaml): đồng bộ cổng, dùng Kubernetes Secret/PVC/probes và cấu hình scrape Prometheus.
- [doc/POSTMAN_TX1.md](./POSTMAN_TX1.md): bổ sung các request Postman cho form HTML và endpoint Actuator.
- [README.md](../README.md): cập nhật lệnh local, Compose, Minikube, GitLab CI/CD, Prometheus/Grafana và hướng dẫn không đưa mật khẩu vào mã nguồn.
- [PageRenderingTest.java](../src/test/java/com/example/shopmanagement/controller/PageRenderingTest.java), [ProductServiceTest.java](../src/test/java/com/example/shopmanagement/service/ProductServiceTest.java): bổ sung kiểm tra luồng lưu/xóa qua MVC và các nhánh tìm kiếm/lọc.

Không xóa database, bảng hoặc dữ liệu. Các test/build Maven không kết nối database. Cấu hình JPA hiện tại vẫn là `ddl-auto=update`, không phải `create`/`create-drop`.

## 3. Kết quả kiểm tra thực tế

| Kiểm tra | Kết quả |
|---|---|
| `.\mvnw.cmd test` | Thành công: 12 tests, 0 failures, 0 errors. |
| `.\mvnw.cmd package` | `BUILD SUCCESS`; tạo `target/shop-management-0.0.1-SNAPSHOT.jar`; test vẫn chạy, không bị bỏ qua. |
| Cấu hình Docker Compose | `docker compose config -q` thành công với biến kiểm tra tạm thời, không ghi vào file. |
| Cú pháp YAML | Offline parser đọc thành công `.gitlab-ci.yml`, `docker-compose.yml` và toàn bộ manifest trong `k8s/`; xác nhận stage CI, port Service `8081` và selector ServiceMonitor. |
| Port/config | Cấu hình source vẫn là port `8081`, database `shop_management`. |
| Ứng dụng đang chạy | Tiến trình Java có vẻ là `shop-management` đã chiếm port `8081` từ 07:50. GET hiện tại tới `/actuator/health`, `/products`, `/actuator/prometheus` trả HTTP 200; health endpoint báo `UP`. |
| Runtime của cấu hình mới | **Chưa xác nhận.** Tiến trình hiện có đã chạy trước khi cấu hình password thành environment-only được cập nhật; tôi không dừng hoặc thay tiến trình đó và không khởi chạy instance thứ hai trên port đang bị chiếm. |
| Docker image/Compose runtime | **Chưa chạy.** Docker daemon không kết nối được (`dockerDesktopLinuxEngine` không sẵn sàng). |
| Kubernetes/Minikube | **Chưa triển khai.** `kubectl` có cài nhưng context mặc định không có cluster trả lời tại `localhost:8080`; `minikube` và `helm` không có trong PATH. |
| GitLab CI/CD | **Chưa chạy trên GitLab.** Chưa có bằng chứng Runner, Registry, biến CI/CD hay cluster deploy được cấu hình. |
| Prometheus/Grafana | **Chưa triển khai/kiểm chứng.** Mới kiểm tra được endpoint Prometheus HTTP 200 trên tiến trình ứng dụng đang tồn tại; chưa cài stack hoặc xác nhận scrape/dashboard. |
| Postman | **Chưa chạy Postman.** Đã cung cấp hướng dẫn request; chưa có kiểm thử Postman thực tế. |

## 4. Việc cần làm thủ công

1. **Khởi động lại ứng dụng với cấu hình mới:** tiến trình hiện tại cần được chủ máy dừng/restart có chủ ý vì đang chiếm port `8081`. Trong terminal PowerShell chạy ứng dụng, đặt `DB_USERNAME` và `DB_PASSWORD` rồi chạy `.\mvnw.cmd spring-boot:run`. Mật khẩu không còn có giá trị mặc định trong source. Sau đó kiểm tra `/actuator/health`, `/products` và `/actuator/prometheus`.
2. **Postman:** làm theo [doc/POSTMAN_TX1.md](./POSTMAN_TX1.md); thêm danh mục trước khi thêm sản phẩm.
3. **Docker:** Docker Desktop phải hoạt động. Copy `.env.example` thành `.env`, thay các giá trị `REPLACE_WITH_...` bằng credential local, chạy `.\mvnw.cmd package`, rồi `docker compose up --build`. Dùng `docker compose down` để dừng; không dùng `down -v` nếu cần giữ volume dữ liệu.
4. **Minikube:** cài/chạy Minikube, tạo Secret `shop-db-secret`, build image bằng `minikube image build`, rồi apply `k8s/mysql.yaml`, chờ `kubectl rollout status deployment/mysql`, sau đó apply `k8s/app.yaml`. Xem lệnh cụ thể trong README. Secret MySQL và PVC cần tồn tại trước khi ứng dụng kết nối được.
5. **GitLab:** bật Container Registry và Runner Docker-in-Docker privileged. Tạo CI/CD variable `KUBECONFIG` dạng File trỏ tới cluster Runner truy cập được, cùng `REGISTRY_DEPLOY_USER`/`REGISTRY_DEPLOY_PASSWORD` là Deploy Token chỉ có `read_registry` (masked/protected). Tạo trước Kubernetes Secret `shop-db-secret`; deploy chỉ xuất hiện manual trên default branch khi cấu hình đủ biến. GitLab shared runner thường không truy cập Minikube chạy trên máy cá nhân.
6. **Prometheus/Grafana:** cài Helm và kube-prometheus-stack release tên `monitoring`, sau đó apply `k8s/prometheus-servicemonitor.yaml`. CRD Prometheus Operator phải tồn tại; tiếp đó port-forward Grafana và import dashboard Spring Boot Micrometer theo README.

Việc xác minh các mục cần Docker daemon, Minikube/cluster, GitLab Runner hoặc Postman phải được thực hiện trên máy/môi trường tương ứng; hiện không có bằng chứng để tuyên bố các hệ thống đó đã chạy thành công.
