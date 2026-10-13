# Kế hoạch nâng cấp giao diện Aura

## Cơ sở đề xuất

Đã xem các template, CSS và JavaScript hiện tại, cùng ảnh người dùng cung cấp về bàn phím 3D trong banner trang chủ; chưa đánh giá toàn bộ giao diện thực tế trên các thiết bị. Website có phong cách nền tối, cyan/tím, hiệu ứng phát sáng, hero canvas/3D, thẻ sản phẩm, bộ lọc giá và danh mục. Đã có giao diện giỏ hàng, thanh toán VietQR, tài khoản và quản trị.

Các ưu tiên từ mã hiện tại: thống nhất icon thay emoji, giảm hiệu ứng cạnh tranh với sản phẩm, đưa CSS viết trực tiếp trong trang về bộ thành phần chung, làm rõ trạng thái thanh toán, bổ sung focus bàn phím và hỗ trợ giảm chuyển động. Các bộ lọc giá/danh mục đã có nên nâng cấp cách trình bày thay vì tạo chức năng trùng.

## Ràng buộc bắt buộc: chỉ nâng cấp giao diện

Theo yêu cầu người dùng, đợt nâng cấp này chỉ thay đổi cách trình bày. Giữ nguyên logic, các hàm/phương thức hiện có, cách gọi và luồng xử lý đang hoạt động trong code.

- Không sửa controller, service, repository, entity, cấu trúc cơ sở dữ liệu, cấu hình nghiệp vụ hoặc tích hợp PayOS.
- Không đổi tên, chữ ký, tham số, giá trị trả về hoặc xóa các hàm/phương thức hiện có; không thay thế thuật toán xử lý.
- Giữ nguyên URL, HTTP method, API, form action/method, tên trường nhập, dữ liệu gửi/nhận và các ràng buộc kiểm tra hiện tại.
- Giữ nguyên liên kết dữ liệu Thymeleaf, ID/data-attribute và các điểm liên kết với JavaScript hiện có. Bố cục mới phải tiếp tục gọi đúng các phương thức đang dùng.
- Không thay đổi tính giá, giảm giá, tồn kho, giỏ hàng, đăng nhập/phân quyền, tạo/hủy đơn hoặc xác nhận thanh toán.
- Giữ nguyên các phương thức thanh toán đang được hỗ trợ, điều kiện tạo QR, đối soát giao dịch, chu kỳ kiểm tra và các trạng thái hiện tại.
- Phạm vi chỉnh sửa chính: HTML/template ở phần trình bày, CSS, icon, ảnh/banner; với mô hình 3D chỉ chỉnh thông số hình ảnh như màu, vật liệu, kích thước, góc nhìn và ánh sáng, giữ nguyên hàm dựng hình và các tương tác hiện có.
- Không thêm chức năng nghiệp vụ, nút không hoạt động hoặc số liệu giả. Hạng mục cần thay đổi logic/phương thức hiện có được loại khỏi đợt này và ghi thành đề xuất riêng.

Khi triển khai cần đối chiếu thay đổi với phiên bản trước để xác nhận không có sửa đổi nghiệp vụ hoặc giao diện gọi hàm/API.

## Ba hướng thiết kế

| Hướng | Đặc điểm | Phù hợp |
| --- | --- | --- |
| Dark Premium — đề xuất | Nền đen/xanh than, chữ rõ, cyan làm điểm nhấn chính, hình sản phẩm lớn; tím chỉ dùng hạn chế | Cửa hàng gaming giữ nhận diện Aura và tăng sự đồng bộ |
| Light Commerce | Nền trắng/xám nhạt, điểm nhấn xanh, mật độ sản phẩm cao hơn, thông tin mua hàng dễ quét | Cửa hàng bán nhiều loại thiết bị điện tử |
| Gaming Showcase | Hero đậm cá tính, hình setup lớn, nhiều chuyển động hơn và neon mạnh hơn | Trang giới thiệu bộ sưu tập hoặc chiến dịch gaming |

Chọn Dark Premium làm hướng mặc định cho bản kế hoạch. Đây là đề xuất, chưa phải lựa chọn đã được người dùng chốt.

## Phạm vi làm lại toàn bộ

### 1. Bộ giao diện chung

- Chuẩn hóa màu, chữ, khoảng cách, bo góc, button, trường nhập, badge, thông báo và thẻ sản phẩm.
- Chọn một hệ icon đồng nhất; nút chính có mức nổi bật nhất quán.
- Có trạng thái hover, focus, disabled, đang xử lý, lỗi và thành công.
- Header gọn, ưu tiên tìm kiếm; menu điện thoại mở/đóng rõ ràng.
- Footer có liên hệ và chính sách thực tế của cửa hàng.

### 2. Trang chủ

- Hero một thông điệp rõ, một sản phẩm hoặc bộ sưu tập chủ đạo, nút mua sắm nổi bật.
- Thứ tự nội dung: hero → danh mục → sản phẩm nổi bật → sản phẩm mới → chính sách cửa hàng.
- Ảnh sản phẩm thống nhất nền và tỷ lệ; giảm glitch và hiệu ứng phát sáng liên tục.
- Các số liệu 500+ sản phẩm, 10k+ khách và tuyên bố bảo hành cần dùng dữ liệu/chính sách thật hoặc bỏ.

#### Nâng cấp phần bàn phím 3D trong ảnh người dùng cung cấp

- Phần cần thay đổi là khối bàn phím nổi bên phải banner, gồm canvas `keyboard-3d-canvas`, nền và vòng sáng bên dưới.
- Đề xuất theo Dark Premium: bàn phím có hình dáng rõ hơn, kích thước phím cân đối, màu graphite/xám làm nền và một số phím cyan làm điểm nhấn; giảm màu tím/hồng quá sáng.
- Điều chỉnh vật liệu, ánh sáng và góc nhìn để sản phẩm có chiều sâu, giảm cảm giác các phím là những khối vuông phát sáng. Chi tiết nhãn phím nếu làm được bằng tài nguyên hiển thị hiện có sẽ được bổ sung; nếu cần viết lại logic dựng hình thì đưa ra đề xuất riêng.
- Thu gọn vòng sáng, giảm độ chói và mật độ họa tiết nền để bàn phím nổi bật và đồng bộ với nội dung banner.
- Cân lại tỷ lệ, khoảng trống và vị trí bàn phím ở máy tính/điện thoại; không cắt mất sản phẩm hoặc chồng lên chữ/nút mua sắm.
- Giữ nguyên cách khởi tạo mô hình, các phương thức render, tương tác đang có và cơ chế ảnh thay thế khi không tải được 3D.
- Bản mẫu đầu tiên phải thể hiện rõ thay đổi của khối này, thay vì chỉ đổi chữ/màu của trang chủ.

### 3. Danh mục và tìm kiếm

- Làm lại thẻ sản phẩm: ảnh, tên hai dòng, giá bán, giá cũ và tình trạng hàng rõ ràng.
- Hiển thị bộ lọc đang áp dụng, nút xóa bộ lọc, số kết quả và sắp xếp.
- Bộ lọc bên cạnh trên máy tính; trên điện thoại bố trí gọn và dễ thao tác bằng cách trình bày các bộ lọc hiện có. Bảng trượt chỉ áp dụng nếu tái sử dụng được cách mở/đóng hiện có mà không thay đổi phương thức hoặc logic.
- Trạng thái không tìm thấy hàng có hướng dẫn điều chỉnh bộ lọc.

### 4. Chi tiết sản phẩm

- Ảnh lớn, thông tin mua hàng gọn và giá nổi bật.
- Trình bày thông số, mô tả, chính sách thành các vùng dễ đọc.
- Nút mua trên điện thoại luôn dễ tiếp cận, không che nội dung.
- Gallery nhiều ảnh, biến thể, đánh giá và so sánh không thuộc đợt nâng cấp giao diện này nếu chưa có sẵn; ghi thành đề xuất riêng.

### 5. Giỏ hàng và thanh toán

- Giỏ hàng dạng thẻ trên điện thoại, tổng tiền và nút tiếp tục rõ ràng.
- Thanh toán theo bố cục thông tin nhận hàng → phương thức → tổng tiền.
- Trang VietQR có QR nổi bật, tài khoản, số tiền và nội dung dễ sao chép.
- Phân biệt đơn đã tạo, đang chờ xác nhận, đã thanh toán và lỗi kết nối. Lỗi kết nối không hiển thị như thanh toán thất bại.
- Trình bày lại các thông báo lỗi hiện có cho dễ nhận biết. Chỉ thiết kế trạng thái đang xử lý nếu trạng thái này đã có trong luồng hiện tại; không bổ sung hoặc thay đổi logic submit.

### 6. Tài khoản và đơn hàng

- Đồng bộ đăng nhập/đăng ký với nhận diện mới.
- Thẻ đơn hàng gọn; trạng thái giao hàng và thanh toán được phân biệt.
- Dòng tiến trình đơn chỉ biểu diễn các trạng thái và thời điểm hệ thống thực sự lưu.

### 7. Quản trị

- Điều hướng bên trái rõ mục đang mở, bảng dễ đọc, form sản phẩm chia nhóm.
- Tách đơn chưa thanh toán, đã thanh toán và đã hủy bằng màu/badge nhất quán.
- Chỉ trình bày lại số liệu và bộ lọc quản trị hiện có. Biểu đồ doanh thu hoặc chức năng tìm kiếm/lọc mới cần thêm xử lý được đưa ra ngoài phạm vi đợt này.

## Thứ tự triển khai

1. **Chốt phong cách và bản mẫu:** trang chủ có khối bàn phím 3D nâng cấp + một thẻ sản phẩm + trang thanh toán ở bản máy tính và điện thoại; ghi nhận các hàm/API/form hiện có làm mốc đối chiếu.
2. **Nền tảng và khám phá sản phẩm:** bộ giao diện chung, header/footer, trang chủ, tìm kiếm/danh mục.
3. **Luồng mua hàng:** chi tiết sản phẩm, giỏ hàng, checkout, QR và các trạng thái thanh toán.
4. **Hoàn tất cửa hàng:** tài khoản, lịch sử đơn hàng, liên hệ và các trạng thái trống/lỗi.
5. **Quản trị và nghiệm thu:** đồng bộ trang quản trị, kiểm tra thiết bị và các luồng hoạt động.

Nếu chỉ nâng cấp một phần, ưu tiên bộ giao diện chung + trang chủ + thẻ sản phẩm + checkout/QR trước.

## Tiêu chí nghiệm thu

- Kiểm tra ở chiều rộng 360, 390, 768 và 1440px; không tràn ngang, menu/bộ lọc/nút mua sử dụng được.
- Kiểm tra bằng chuột và bàn phím; focus luôn nhìn thấy, trường nhập có nhãn, thông báo không chỉ dựa vào màu.
- Giảm hiệu ứng trang trí bằng CSS theo tùy chọn người dùng; không sửa phương thức render hoặc vòng lặp canvas hiện có trong đợt nâng cấp giao diện.
- Ảnh có kích thước ổn định, ảnh ngoài màn hình tải sau; ảnh lỗi có thay thế hợp lý.
- Kiểm tra tìm kiếm/lọc, thêm và cập nhật giỏ, đăng nhập, đặt hàng COD/VietQR, QR đúng dữ liệu và trạng thái thanh toán được xác minh.
- Kiểm tra màn hình sản phẩm hết hàng, giỏ trống, không có kết quả, lỗi kết nối thanh toán và đơn đã hủy.
- Chụp và xem lại giao diện từng trang trọng điểm ở bản máy tính và điện thoại trước khi hoàn tất.
- Kiểm tra riêng bàn phím 3D: bố cục, độ chói, tỷ lệ, ảnh thay thế và tương tác hiện có trên máy tính/điện thoại.
- Đối chiếu thay đổi: không sửa logic nghiệp vụ, các hàm/phương thức, URL/API, form và liên kết dữ liệu hiện tại. Các luồng trước và sau nâng cấp phải cho cùng kết quả với cùng dữ liệu đầu vào.

## Các lựa chọn cần chốt trước khi triển khai

- Dark Premium hay nền sáng.
- Nâng cấp cửa hàng trước, hay làm cả quản trị trong cùng đợt.
- Giữ hình sản phẩm hiện có hay chuẩn bị thêm ảnh/banner mới.

Chưa có thay đổi giao diện được thực hiện trong bước lập kế hoạch này.
