# Thanh toán chuyển khoản / VietQR qua PayOS

## Sử dụng

1. Thêm sản phẩm vào giỏ, chọn **Chuyển khoản Ngân hàng**, xác nhận đặt hàng.
2. Trang thanh toán hiển thị QR riêng của đơn hàng cùng tài khoản nhận, số tiền và nội dung chuyển khoản do PayOS cung cấp.
3. Quét bằng ứng dụng ngân hàng hoặc chuyển khoản đúng thông tin hiển thị. Có thể mở trang PayOS khi mua hàng trên điện thoại.
4. Trạng thái tự cập nhật sau khi webhook có chữ ký hợp lệ hoặc kiểm tra trực tiếp với PayOS xác nhận thanh toán. Mở trang thanh toán sẽ kiểm tra lại mỗi 15 giây; kiểm tra nội bộ mỗi 5 giây.

Không dùng `?paid=true` hoặc API giả lập để ghi nhận đã thanh toán. Không chuyển tiền theo các ảnh QR cũ trong thư mục ảnh.

## Cấu hình

Cấu hình PayOS hiện tại đã chuyển sang `config/payment-local.properties`, được loại khỏi Git. Chạy ứng dụng từ thư mục gốc dự án để đọc tệp này. Khi triển khai, cung cấp các biến môi trường:

- `PAYOS_CLIENT_ID`
- `PAYOS_API_KEY`
- `PAYOS_CHECKSUM_KEY`
- `PAYOS_BASE_URL`: địa chỉ gốc của website, ví dụ `https://shop.example.com`. Mặc định khi chạy máy cá nhân là `http://localhost:8080`.

Trong kênh thanh toán trên PayOS, đăng ký webhook:

```
https://<ten-mien-website>/api/payment/payos-webhook
```

Webhook cần địa chỉ HTTPS công khai. Khi chạy localhost, trang thanh toán vẫn có thể kiểm tra giao dịch trực tiếp với PayOS nếu máy kết nối Internet; cập nhật tự động khi khách đóng trang cần webhook công khai.

Thông tin người nhận trên website lấy từ kênh thanh toán PayOS, không dùng tài khoản ghi cứng. Cần kiểm tra kênh PayOS đang liên kết đúng ngân hàng của cửa hàng trước khi thu tiền thật. Các khóa từng nằm trong mã nguồn nên được thay mới trên PayOS nếu mã nguồn đã được chia sẻ.

JPA cập nhật các cột thông tin ngân hàng và phiên bản đơn hàng khi khởi động với cấu hình hiện tại `ddl-auto=update`. Với môi trường dùng migration riêng, cần thêm các cột này trước khi chạy phiên bản mới.

Chỉ COD và chuyển khoản/VietQR được cho chọn ở bước đặt hàng. Chưa có tích hợp cổng MoMo, VNPAY riêng hoặc thẻ quốc tế. Giá trị `VNPAY` của các đơn cũ tiếp tục được xử lý qua PayOS.

## Kiểm thử

`PaymentFlowTest` kiểm tra số tiền, chữ ký giả, đối chiếu mã link, webhook lặp, thanh toán sau hủy đơn, xác nhận trực tiếp, giới hạn tần suất, phân quyền khách mua, CSRF, QR giải mã và các trạng thái giao diện. Các kiểm thử không chuyển tiền và không sửa dữ liệu mua hàng hiện có.

Tài liệu chính thức: [Java SDK](https://payos.vn/docs/sdks/back-end/java/), [Webhook thanh toán](https://payos.vn/docs/du-lieu-tra-ve/webhook/).

Trước khi đưa vào sử dụng thật, thực hiện một đơn giá trị nhỏ từ tài khoản ngân hàng của bạn, kiểm tra tiền về đúng tài khoản và đơn chuyển sang **Đã thanh toán**. Quét QR hoặc mở trang thanh toán đơn thuần chưa đồng nghĩa với đã thanh toán.
