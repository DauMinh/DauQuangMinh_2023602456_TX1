# Shop Management (TX1)

Ứng dụng Spring Boot 3.5.7, Java 21, Maven, MySQL, Spring Data JPA và Thymeleaf để quản lý danh mục và sản phẩm. Cổng HTTP là `8081`; database local là `shop_management`.

## Chạy local với MySQL/XAMPP

1. Khởi động MySQL trong XAMPP và tạo database một lần nếu database chưa tồn tại:

   ```sql
   CREATE DATABASE shop_management
       CHARACTER SET utf8mb4
       COLLATE utf8mb4_unicode_ci;
   ```

2. Cấu hình `DB_USERNAME` và `DB_PASSWORD` trong terminal đang dùng. Không ghi mật khẩu thật vào mã nguồn hoặc Git:

   ```powershell
   $env:DB_USERNAME = "root"
   $password = Read-Host "Nhập mật khẩu MySQL" -AsSecureString
   $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
   try {
       $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
   } finally {
       [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
       Remove-Variable password
   }
   ```

   URL mặc định là `jdbc:mysql://localhost:3306/shop_management`. Có thể override bằng biến `DB_URL`. Tạo bảng/cập nhật schema được Hibernate quản lý bằng `ddl-auto=update`; ứng dụng không xóa database.

3. Chạy ứng dụng:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. Mở `http://localhost:8081`, trang sản phẩm `http://localhost:8081/products` hoặc trang danh mục `http://localhost:8081/categories`.

## Kiểm thử bằng Postman

Ứng dụng cung cấp form HTML Thymeleaf, không phải REST API JSON. Dùng `x-www-form-urlencoded` cho form POST. Hướng dẫn CRUD, tìm kiếm, lọc và kiểm tra validation nằm trong [doc/POSTMAN_TX1.md](doc/POSTMAN_TX1.md).

## Build và test

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Test MVC/validation và tìm kiếm chạy độc lập, không cần xóa hoặc sửa dữ liệu MySQL.

## Docker Compose

Compose dùng MySQL riêng trong named volume `mysql_data`; không kết nối hoặc thay đổi MySQL/XAMPP local. Copy `.env.example` thành `.env`, thay các giá trị mẫu bằng mật khẩu local riêng, rồi:

```powershell
Copy-Item .env.example .env
# Sửa .env bằng editor và thay tất cả giá trị REPLACE_WITH_...
.\mvnw.cmd package
docker compose up --build
```

Ứng dụng chạy tại `http://localhost:8081`; MySQL của Compose được ánh xạ host port `3307` sang container port `3306`. Dữ liệu MySQL Compose được lưu trong volume. `docker compose down` giữ volume; không dùng `docker compose down -v` nếu muốn giữ dữ liệu.

## Kubernetes / Minikube

Ứng dụng dùng port `8081` trong container, Service port `8081` và NodePort `30081`; MySQL dùng port `3306`. Các manifest trong `k8s/` tạo PVC cho MySQL, Deployment và Service. Tạo Secret bằng mật khẩu do bạn chọn, không commit Secret vào Git:

```powershell
$dbPassword = Read-Host "Mật khẩu MySQL cho ứng dụng" -AsSecureString
$rootPassword = Read-Host "Mật khẩu MySQL root" -AsSecureString
$dbBstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPassword)
$rootBstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($rootPassword)
try {
    $dbPlain = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($dbBstr)
    $rootPlain = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($rootBstr)
    kubectl create secret generic shop-db-secret --from-literal="db-password=$dbPlain" --from-literal="root-password=$rootPlain"
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($dbBstr)
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($rootBstr)
    Remove-Variable dbPlain,rootPlain -ErrorAction SilentlyContinue
}
```

MySQL chỉ khởi tạo database/user từ biến môi trường khi PVC còn rỗng. Nếu PVC đã có dữ liệu, thay đổi giá trị Secret không tự đổi thông tin đăng nhập bên trong database. Không xóa PVC để “sửa” lỗi kết nối nếu cần giữ dữ liệu.

Với Minikube, build image trực tiếp trong cluster rồi áp dụng manifest:

```powershell
.\mvnw.cmd package
minikube image build -t shop-management:latest .
kubectl apply -f k8s/mysql.yaml
kubectl rollout status deployment/mysql --timeout=180s
kubectl apply -f k8s/app.yaml
kubectl get pods,svc
minikube service shop-management --url
```

Lệnh trên chỉ chuẩn bị triển khai; trạng thái thực tế phụ thuộc vào Minikube, quyền truy cập cluster, Secret và image trên máy của bạn.

## GitLab CI/CD

`.gitlab-ci.yml` định nghĩa các stage build, test, package, dockerize và deploy. Dockerize yêu cầu GitLab Container Registry bật và Runner hỗ trợ Docker-in-Docker ở chế độ privileged. Deploy là manual trên nhánh mặc định và chỉ xuất hiện khi cấu hình các biến CI/CD `KUBECONFIG` dạng **File** (trỏ tới cluster mà Runner truy cập được), `REGISTRY_DEPLOY_USER` và `REGISTRY_DEPLOY_PASSWORD` (Deploy Token chỉ có quyền `read_registry`, đánh dấu masked/protected).

Trước deploy, tạo Secret `shop-db-secret` trong Kubernetes. Nếu dùng registry riêng, pipeline tạo `registry-credentials` từ thông tin đăng nhập registry có sẵn của GitLab. Minikube chạy trên máy cá nhân thường không truy cập được từ GitLab shared runner; cần self-hosted runner có network/context tới cluster hoặc deploy thủ công theo hướng dẫn Minikube phía trên. Chưa có pipeline nào được chạy bởi GitLab trong lần kiểm tra này.

## Prometheus và Grafana trên Minikube

Ứng dụng bật:

- Health: `http://localhost:8081/actuator/health`
- Metrics: `http://localhost:8081/actuator/metrics`
- Prometheus: `http://localhost:8081/actuator/prometheus`

Sau khi ứng dụng được triển khai, cài Helm nếu máy chưa có, cài kube-prometheus-stack và áp dụng ServiceMonitor:

```powershell
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
kubectl create namespace monitoring
helm install monitoring prometheus-community/kube-prometheus-stack --namespace monitoring
kubectl apply -f k8s/prometheus-servicemonitor.yaml
kubectl get pods --namespace monitoring
```

ServiceMonitor yêu cầu CRD do Prometheus Operator cài đặt và selector của release `monitoring`. Port-forward Grafana:

```powershell
kubectl port-forward svc/monitoring-grafana 3000:80 --namespace monitoring
```

Mở `http://localhost:3000`. Lấy mật khẩu admin từ Secret của Helm release theo hướng dẫn hiển thị sau khi cài chart; không đưa mật khẩu đó vào repository. Import dashboard Spring Boot Micrometer (ví dụ ID `11378`) và chọn Prometheus làm data source. Cấu hình này chưa được triển khai hay xác nhận hoạt động trong lần kiểm tra hiện tại.
