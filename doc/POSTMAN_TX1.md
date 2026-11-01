# Hướng dẫn kiểm thử TX1 bằng Postman

Ứng dụng hiện cung cấp giao diện web Thymeleaf, không phải REST API JSON. Trong Postman, gửi form dưới dạng `x-www-form-urlencoded`; các thao tác lưu/xóa thành công trả về chuyển hướng HTTP `302`.

## Chuẩn bị

1. Tạo database MySQL `shop_management`, cấu hình tài khoản qua biến môi trường `DB_USERNAME` và `DB_PASSWORD`, rồi khởi động ứng dụng:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

2. Tạo một Environment trong Postman với biến:

   | Variable | Initial value |
   |---|---|
   | `baseUrl` | `http://localhost:8081` |
   | `categoryId` | ID danh mục đã tạo |
   | `productId` | ID sản phẩm đã tạo |

3. Với request GET, chọn method `GET`. Với form lưu, chọn method `POST`, tab **Body → x-www-form-urlencoded**. Postman thường tự theo chuyển hướng; nếu tắt tùy chọn follow redirects, kiểm tra status `302` và header `Location`.

## Danh mục

| Thao tác | Method và URL | Body `x-www-form-urlencoded` | Kết quả mong đợi |
|---|---|---|---|
| Xem danh sách | `GET {{baseUrl}}/categories` | — | `200`, HTML danh sách danh mục |
| Mở form thêm | `GET {{baseUrl}}/categories/new` | — | `200`, form thêm danh mục |
| Thêm danh mục | `POST {{baseUrl}}/categories/save` | `name=Đồ uống`, `description=Danh mục kiểm thử` | `302`, chuyển tới `/categories` |
| Mở form sửa | `GET {{baseUrl}}/categories/{{categoryId}}/edit` | — | `200`, form có dữ liệu hiện tại |
| Cập nhật danh mục | `POST {{baseUrl}}/categories/save` | `id={{categoryId}}`, `name=Đồ uống cập nhật`, `description=Mô tả mới` | `302`, chuyển tới `/categories` |
| Kiểm tra validation tên | `POST {{baseUrl}}/categories/save` | `name= ` | `200`, HTML form có thông báo tên không được để trống |
| Xóa danh mục trống | `POST {{baseUrl}}/categories/{{categoryId}}/delete` | — | `302`, chuyển tới `/categories` |

Danh mục đang được sản phẩm tham chiếu không thể xóa. Thử xóa trường hợp đó để xác nhận ứng dụng giữ nguyên quan hệ và hiển thị thông báo lỗi.

## Sản phẩm

| Thao tác | Method và URL | Body `x-www-form-urlencoded` | Kết quả mong đợi |
|---|---|---|---|
| Xem danh sách | `GET {{baseUrl}}/products` | — | `200`, bảng sản phẩm có tên danh mục |
| Tìm theo tên | `GET {{baseUrl}}/products?name=ca%20phe` | — | `200`, danh sách khớp tên (không phân biệt hoa/thường) |
| Lọc theo danh mục | `GET {{baseUrl}}/products?categoryId={{categoryId}}` | — | `200`, chỉ sản phẩm thuộc danh mục |
| Kết hợp tìm và lọc | `GET {{baseUrl}}/products?name=ca%20phe&categoryId={{categoryId}}` | — | `200`, áp dụng đồng thời hai điều kiện |
| Mở form thêm | `GET {{baseUrl}}/products/new` | — | `200`, form có danh sách chọn danh mục |
| Thêm sản phẩm | `POST {{baseUrl}}/products/save` | `name=Cà phê`, `price=25000`, `stock=10`, `categoryId={{categoryId}}` | `302`, chuyển tới `/products` |
| Mở form sửa | `GET {{baseUrl}}/products/{{productId}}/edit` | — | `200`, form có dữ liệu hiện tại |
| Cập nhật sản phẩm | `POST {{baseUrl}}/products/save` | `id={{productId}}`, `name=Cà phê cập nhật`, `price=27000`, `stock=12`, `categoryId={{categoryId}}` | `302`, chuyển tới `/products` |
| Kiểm tra validation | `POST {{baseUrl}}/products/save` | `name= `, `price=0`, `stock=10`, `categoryId={{categoryId}}` | `200`, HTML form hiển thị lỗi tên và giá |
| Xóa sản phẩm | `POST {{baseUrl}}/products/{{productId}}/delete` | — | `302`, chuyển tới `/products` |

Các request POST xóa có thể bị trình duyệt hỏi xác nhận, nhưng Postman gửi trực tiếp request và không cần bước xác nhận JavaScript. Chỉ dùng dữ liệu thử nghiệm bạn được phép thay đổi.

## Actuator / Prometheus

| Mục đích | Request |
|---|---|
| Health | `GET {{baseUrl}}/actuator/health` |
| Metrics | `GET {{baseUrl}}/actuator/metrics` |
| Prometheus | `GET {{baseUrl}}/actuator/prometheus` |

`/actuator/prometheus` trả về văn bản metrics theo định dạng Prometheus khi ứng dụng kết nối database và khởi động thành công.
