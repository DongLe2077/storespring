package com.example.storespring;

import com.example.storespring.entity.*;
import com.example.storespring.repository.CategoryRepository;
import com.example.storespring.repository.OrderRepository;
import com.example.storespring.repository.ProductRepository;
import com.example.storespring.repository.UserRepository;
import com.example.storespring.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class StorespringApplication {

	public static void main(String[] args) {
		SpringApplication.run(StorespringApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(UserService userService,
									  CategoryRepository categoryRepository,
									  ProductRepository productRepository,
									  OrderRepository orderRepository,
									  UserRepository userRepository) {
		return args -> {
			// 1. Khởi tạo tài khoản mặc định: admin / admin123 và dong1 / 123456
			userService.initDefaultAdmin();

			// 2. Khởi tạo danh mục nếu chưa có
			if (!categoryRepository.existsByName("Bàn phím cơ")) categoryRepository.save(new Category("Bàn phím cơ", "keyboard.png"));
			if (!categoryRepository.existsByName("Chuột Gaming")) categoryRepository.save(new Category("Chuột Gaming", "mouse.png"));
			if (!categoryRepository.existsByName("Tai nghe Gaming")) categoryRepository.save(new Category("Tai nghe Gaming", "headphone.png"));
			if (!categoryRepository.existsByName("Lót chuột & Phụ kiện")) categoryRepository.save(new Category("Lót chuột & Phụ kiện", "pad.png"));
			if (!categoryRepository.existsByName("Màn hình Gaming")) categoryRepository.save(new Category("Màn hình Gaming", "monitor.png"));

			var categories = categoryRepository.findAll();
			Category catKey = categories.stream().filter(c -> c.getName().contains("phím")).findFirst().orElse(null);
			Category catMouse = categories.stream().filter(c -> c.getName().contains("Chuột")).findFirst().orElse(null);
			Category catHead = categories.stream().filter(c -> c.getName().contains("Tai nghe")).findFirst().orElse(null);
			Category catPad = categories.stream().filter(c -> c.getName().contains("Phụ kiện") || c.getName().contains("Lót")).findFirst().orElse(null);
			Category catMonitor = categories.stream().filter(c -> c.getName().contains("Màn hình")).findFirst().orElse(null);

			// 3. Khởi tạo danh mục 40 sản phẩm thực tế với ảnh trong /uploads/
			if (catKey != null) {
				saveOrUpdateProduct(productRepository, "Bàn phím cơ AKKO 3098B Plus Multi-Modes Cyberpunk", 1890000, 15, 50, catKey,
						"/uploads/akko-3098b.jpg",
						"Bàn phím cơ AKKO 3098B Plus Cyberpunk kết nối 3 chế độ (Type-C, Bluetooth 5.0, 2.4GHz). Switch Akko CS Jelly Pink gõ cực êm, keycap PBT Double-shot độ bền cao, LED RGB tùy biến.");

				saveOrUpdateProduct(productRepository, "Bàn phím Custom Keychron Q1 Pro Wireless QMK/VIA", 4250000, 10, 30, catKey,
						"/uploads/keychron-q1.jpg",
						"Khung nhôm nguyên khối CNC cao cấp, kết nối không dây Bluetooth 5.1 và cáp Type-C. Hỗ trợ tùy biến switch hot-swap, keycap OSA PBT profile, mạch xuôi LED RGB từng phím chuẩn pro.");

				saveOrUpdateProduct(productRepository, "Bàn phím cơ không dây FL-Esports CMK75 Gradient Cyan", 2490000, 20, 40, catKey,
						"/uploads/flesports-cmk75.jpg",
						"Bàn phím CMK75 trang bị màn hình TFT hiển thị thông tin thời gian thực, núm xoay kim loại, cấu trúc Gasket Mount êm ái, switch TTC Iron cao cấp đã lube sẵn mượt mà.");

				saveOrUpdateProduct(productRepository, "Bàn phím Corsair K70 RGB PRO Mechanical Gaming", 3190000, 0, 25, catKey,
						"/uploads/corsair-k70.jpg",
						"Tốc độ xử lý siêu nhanh với công nghệ AXON 8000Hz, switch Cherry MX Red chính hãng từ Đức, khung nhôm phay xước chuẩn eSports đẳng cấp hàng đầu thế giới.");

				saveOrUpdateProduct(productRepository, "Bàn phím cơ Logitech G Pro X TKL Lightspeed Wireless", 4690000, 10, 35, catKey,
						"/uploads/logitech-g-pro-x-tkl.jpg",
						"Phiên bản TKL không dây đỉnh cao thiết kế cùng các tuyển thủ eSports số 1 thế giới. Switch GX Brown cơ học xúc giác, kết nối LIGHTSPEED độ trễ 1ms, túi đựng du lịch cao cấp đi kèm.");

				saveOrUpdateProduct(productRepository, "Bàn phím cơ MonsGeek M1W V3 SP RGB Tri-Mode", 2190000, 15, 45, catKey,
						"/uploads/monsgeek-m1w.jpg",
						"Bàn phím nhôm nguyên khối 75% giá tốt nhất phân khúc, hỗ trợ 3 chế độ kết nối, mạch xuôi hotswap 5 pin, lót full foam Poron triệt tiêu âm vang hoàn hảo.");

				saveOrUpdateProduct(productRepository, "Bàn phím cơ Gaming ASUS ROG Azoth 75% OLED", 5990000, 8, 20, catKey,
						"/uploads/asus-rog-azoth.jpg",
						"Siêu phẩm phím cơ custom gaming với màn hình OLED 2 inch hiển thị thông số CPU/GPU, núm xoay đa năng 3 hướng, switch ROG NX Red lube sẵn, bộ dụng cụ lube DIY đi kèm.");

				saveOrUpdateProduct(productRepository, "Bàn phím cơ Wooting 60HE Hall Effect Analog", 4890000, 0, 20, catKey,
						"/uploads/wooting-60he.jpg",
						"Đỉnh cao bàn phím cơ Esports với switch từ tính Lekker, tính năng Rapid Trigger tùy chỉnh điểm nhận phím từ 0.1mm đến 4.0mm không độ trễ, chế độ Tachyon mode siêu tốc.");

				saveOrUpdateProduct(productRepository, "Bàn phím cơ Razer Huntsman V3 Pro TKL Optical", 5290000, 10, 15, catKey,
						"/uploads/razer-huntsman-v3.jpg",
						"Switch quang học Analog Gen-2 thế hệ mới nhất của Razer với vòng xoay Digital Dial đa chức năng, đệm kê tay nam châm da cao cấp, kết cấu nhôm 5052 chịu lực chuẩn quân đội.");
			}

			if (catMouse != null) {
				saveOrUpdateProduct(productRepository, "Chuột Gaming Razer Viper V2 Pro Ultra-Lightweight", 3290000, 18, 35, catMouse,
						"/uploads/razer-viper-v2.jpg",
						"Trọng lượng siêu nhẹ chỉ 58g, mắt đọc Focus Pro 30K Optical Sensor đỉnh cao, switch quang học Gen 3 không lo double click, thời lượng pin 80 giờ liên tục.");

				saveOrUpdateProduct(productRepository, "Chuột Logitech G Pro X Superlight Wireless 2", 3490000, 12, 60, catMouse,
						"/uploads/logitech-g-pro-x.jpg",
						"Huyền thoại chuột gaming bắn súng thế hệ mới với cảm biến HERO 2 32000 DPI, switch cơ lai quang LIGHTFORCE, kết nối không dây LIGHTSPEED siêu ổn định.");

				saveOrUpdateProduct(productRepository, "Chuột gaming Pulsar X2V2 Wireless Neon Edition", 2150000, 25, 20, catMouse,
						"/uploads/pulsar-x2v2.jpg",
						"Thiết kế đối xứng hoàn hảo cho kiểu cầm Claw/Fingertip grip, trọng lượng siêu nhẹ 53g, hỗ trợ polling rate lên đến 4K Hz khi dùng dongle, cuộn chuột TTC Gold siêu bền.");

				saveOrUpdateProduct(productRepository, "Chuột Gaming BenQ Zowie EC2-CW Wireless eSports", 3890000, 5, 25, catMouse,
						"/uploads/zowie-ec2-cw.jpg",
						"Form công thái học huyền thoại chuẩn giải đấu CS2 / Valorant, trạm thu phát tín hiệu độc lập Enhanced Receiver chống nhiễu sóng tối đa, cắm là chạy không cần phần mềm.");

				saveOrUpdateProduct(productRepository, "Chuột Gaming ASUS ROG Harpe Ace Aim Lab Edition", 2990000, 20, 30, catMouse,
						"/uploads/asus-rog-harpe-ace.jpg",
						"Hợp tác phát triển cùng Aim Lab tối ưu kỹ năng vẩy đạn FPS, cảm biến ROG AimPoint 36.000 DPI, trọng lượng 54g cấu trúc nylon sinh học siêu bền bỉ.");

				saveOrUpdateProduct(productRepository, "Chuột Gaming Lamzu Maya Wireless 4K Charcoal", 2390000, 12, 40, catMouse,
						"/uploads/lamzu-maya-4k.jpg",
						"Form chuột đa dụng cho mọi cỡ tay, trọng lượng lông vũ 45g, cảm biến PixArt PAW3395 hàng đầu, switch quang Huano Blue Shell Pink Dot siêu nảy.");

				saveOrUpdateProduct(productRepository, "Chuột Gaming Finalmouse UltralightX Carbon Lion", 5890000, 0, 10, catMouse,
						"/uploads/finalmouse-ultralightx.jpg",
						"Vỏ sợi carbon composite độc quyền với trọng lượng không tưởng chỉ 31 gram, cảm biến quang học thế hệ mới hỗ trợ polling rate không dây lên đến 8000Hz đỉnh cao thế giới.");

				saveOrUpdateProduct(productRepository, "Chuột Gaming eSports Vaxee XE Wireless 4K Orange", 3650000, 5, 25, catMouse,
						"/uploads/vaxee-xe-wireless.jpg",
						"Thiết kế chuẩn giải đấu CS2 / Valorant từ những nhà sáng lập Zowie huyền thoại, kết nối 4K không dây độc lập chống nhiễu sóng, cảm biến PAW3395 căn chỉnh tối ưu chuẩn xác.");
			}

			if (catHead != null) {
				saveOrUpdateProduct(productRepository, "Tai nghe HyperX Cloud III Wireless Black-Red", 3690000, 15, 25, catHead,
						"/uploads/hyperx-cloud-3.jpg",
						"Thời lượng pin vô đối lên đến 120 giờ chỉ với một lần sạc, âm thanh vòm DTS Headphone:X Spatial Audio định vị tiếng bước chân cực chuẩn xác trong game FPS.");

				saveOrUpdateProduct(productRepository, "Tai nghe Gaming SteelSeries Arctis Nova Pro Wireless", 7990000, 10, 15, catHead,
						"/uploads/steelseries-nova-pro.jpg",
						"Đỉnh cao tai nghe gaming với bộ giải mã GameDAC Gen 2 âm thanh Hi-Res, tính năng chống ồn chủ động ANC 4 micro, hệ thống 2 pin tráo đổi liên tục không lo hết pin.");

				saveOrUpdateProduct(productRepository, "Tai nghe Razer BlackShark V2 Pro 2023 Edition", 4490000, 20, 30, catHead,
						"/uploads/razer-blackshark-v2.jpg",
						"Micro băng thông siêu rộng Razer HyperClear 9.9mm cho giọng nói trong trẻo như phát thanh viên, màng loa TriForce Titanium 50mm tái tạo âm trầm mạnh mẽ.");

				saveOrUpdateProduct(productRepository, "Tai nghe Logitech G PRO X 2 LIGHTSPEED Wireless", 5690000, 10, 25, catHead,
						"/uploads/logitech-g-pro-x-2.jpg",
						"Màng loa Graphene 50mm đột phá cho âm thanh chi tiết đến từng hơi thở, kết nối đa nền tảng LIGHTSPEED, Bluetooth và 3.5mm, đệm tai mút hoạt tính bọc da êm ái.");

				saveOrUpdateProduct(productRepository, "Tai nghe không dây Corsair HS80 MAX Wireless Spatial", 3890000, 15, 30, catHead,
						"/uploads/corsair-hs80-max.jpg",
						"Công nghệ Dolby Atmos chân thực đa hướng, màng loa Neodymium 50mm tùy biến, micro thu âm đa hướng chuẩn phát sóng, thời lượng pin 65 giờ liên tục.");

				saveOrUpdateProduct(productRepository, "Tai nghe kiểm âm Gaming Audio-Technica ATH-GDL3 Open-Back", 3290000, 0, 20, catHead,
						"/uploads/audio-technica-gdl3.jpg",
						"Thiết kế Open-Back mở thoáng âm trường siêu rộng như đứng giữa sân khấu, driver 45mm tái tạo âm thanh chuẩn phòng thu Nhật Bản, trọng lượng siêu nhẹ 220g.");

				saveOrUpdateProduct(productRepository, "Tai nghe Studio Gaming Sennheiser HD 660S2", 13990000, 8, 8, catHead,
						"/uploads/sennheiser-hd660s2.jpg",
						"Huyền thoại âm thanh phòng thu audiophile đến từ Đức với thiết kế Open-Back màng loa 38mm, tái tạo dải trầm sâu lắng và không gian âm trường 3D sống động chân thực.");

				saveOrUpdateProduct(productRepository, "Tai nghe Gaming Beyerdynamic DT 990 Pro 250 Ohm", 4390000, 0, 15, catHead,
						"/uploads/beyerdynamic-dt990pro.jpg",
						"Tai nghe kiểm âm mở kinh điển của các game thủ và streamer hàng đầu thế giới, đệm tai nhung Velour êm ái thoáng khí, dải âm tần siêu rộng 5Hz - 35.000Hz định vị cực nhạy.");
			}

			if (catPad != null) {
				saveOrUpdateProduct(productRepository, "Lót chuột Artisan Ninja FX Zero XSoft (Japan)", 1350000, 0, 40, catPad,
						"/uploads/artisan-ninja-fx.jpg",
						"Pad chuột số 1 thế giới đến từ Nhật Bản được các tuyển thủ chuyên nghiệp tin dùng, bề mặt dệt đặc biệt cho độ kiểm soát và dừng chuột chính xác tuyệt đối.");

				saveOrUpdateProduct(productRepository, "Bàn di chuột LED RGB Asus ROG Balteus Qi Wireless", 2190000, 30, 20, catPad,
						"/uploads/asus-rog-balteus.jpg",
						"Tích hợp sạc không dây chuẩn Qi ngay trên góc bàn di, 15 vùng đèn LED Aura Sync RGB có thể đồng bộ hóa ánh sáng với toàn bộ dàn máy gaming.");

				saveOrUpdateProduct(productRepository, "Bàn di chuột eSports BenQ ZOWIE G-SR-SE Rouge Edition", 990000, 0, 60, catPad,
						"/uploads/zowie-gsr-se.jpg",
						"Bề mặt dệt mịn tăng tốc độ lướt nhưng vẫn giữ độ ghìm chính xác cao, đế cao su chống trượt độc quyền không bị lún đệm, chuẩn thi đấu CS2 quốc tế.");

				saveOrUpdateProduct(productRepository, "Cáp bàn phím Custom Coiled Cable GX16 Aviator Type-C", 390000, 25, 80, catPad,
						"/uploads/custom-coiled-cable.jpg",
						"Dây xoắn bọc dù 2 lớp Techflex chống gập gãy, đầu nối kim loại phi công GX16 4-pin mạ vàng sang trọng, hỗ trợ sạc nhanh và truyền dữ liệu tốc độ cao.");

				saveOrUpdateProduct(productRepository, "Bộ Keycap PBT Double-Shot Matcha 134 phím XDA Profile", 550000, 20, 50, catPad,
						"/uploads/keycap-matcha-pbt.jpg",
						"Chất liệu nhựa PBT siêu dày dặn 1.5mm không lo bóng mòn sau nhiều năm sử dụng, công nghệ in nhiệt Dye-sub sắc nét tông màu trà xanh Matcha thanh lịch.");

				saveOrUpdateProduct(productRepository, "Giá treo tai nghe Corsair ST100 RGB Sound Card", 1890000, 15, 30, catPad,
						"/uploads/stand-headphone-rgb.jpg",
						"Khung nhôm nguyên khối vững chắc, tích hợp 9 vùng LED Dynamic RGB tùy biến qua iCUE, cổng âm thanh 3.5mm giả lập âm thanh vòm 7.1 cao cấp và 2 cổng USB 3.1 sạc nhanh.");

				saveOrUpdateProduct(productRepository, "Kê tay bàn phím cơ Gỗ Walnut Tự Nhiên Cao Cấp", 390000, 20, 50, catPad,
						"/uploads/wrist-rest-walnut.jpg",
						"Chất liệu gỗ óc chó Bắc Mỹ nguyên khối tuyển chọn, vát công thái học chuẩn chống mỏi cổ tay khi gõ phím thời gian dài, bề mặt phủ sáp ong tự nhiên chống nước và mối mọt.");
			}

			if (catMonitor != null) {
				saveOrUpdateProduct(productRepository, "Màn hình Gaming ASUS ROG Swift PG27AQDM OLED", 24990000, 8, 15, catMonitor,
						"/uploads/monitor-asus-rog-pg27.jpg",
						"Màn hình 27 inch 2K QHD tấm nền OLED 240Hz thời gian phản hồi siêu tốc 0.03ms, độ bao phủ màu 99% DCI-P3, tản nhiệt tùy chỉnh độc quyền chống hiện tượng burn-in.");

				saveOrUpdateProduct(productRepository, "Màn hình Siêu Cong Samsung Odyssey G9 Neo 49\"", 32990000, 10, 10, catMonitor,
						"/uploads/monitor-samsung-odyssey-g9.jpg",
						"Độ cong đỉnh cao 1000R góc nhìn siêu rộng tương đương 2 màn 27 inch, công nghệ Quantum Matrix Mini-LED HDR2000, tần số quét 240Hz 1ms cho không gian giải trí vô tận.");

				saveOrUpdateProduct(productRepository, "Màn hình Gaming LG UltraGear 27GR95QE OLED", 19990000, 12, 20, catMonitor,
						"/uploads/monitor-lg-ultragear-oled.jpg",
						"Tấm nền OLED cao cấp tốc độ làm tươi 240Hz, độ tương phản 1.500.000:1, tương thích NVIDIA G-Sync và AMD FreeSync Premium mang đến trải nghiệm đồ họa mượt mà không bóng mờ.");

				saveOrUpdateProduct(productRepository, "Màn hình Đồ họa Gaming Dell Alienware AW3423DW", 27500000, 5, 12, catMonitor,
						"/uploads/monitor-dell-alienware-curved.jpg",
						"Công nghệ Quantum Dot OLED đột phá, độ cong 1800R, chuẩn HDR True Black 400, ánh xạ màu sắc chuyên nghiệp chuẩn rạp chiếu phim kết hợp cùng dải đèn AlienFX RGB.");

				saveOrUpdateProduct(productRepository, "Màn hình Gaming MSI MAG 274QRF-QD Rapid IPS", 8990000, 15, 25, catMonitor,
						"/uploads/monitor-msi-optix-mag.jpg",
						"Tấm nền Rapid IPS 2K 165Hz với lớp chấm lượng tử Quantum Dot cho màu sắc sống động, góc nhìn siêu rộng 178 độ, chân đế công thái học nâng hạ xoay 90 độ linh hoạt.");

				saveOrUpdateProduct(productRepository, "Màn hình Gaming eSports GIGABYTE Aorus FI27Q-X", 12490000, 10, 18, catMonitor,
						"/uploads/monitor-gigabyte-aorus-2k.jpg",
						"Chuẩn thi đấu Esports với tần số quét 240Hz độ phân giải 2K QHD, bảng điều khiển Super Speed IPS màu 10-bit chuẩn điện ảnh, hệ thống khử ồn chủ động ANC tích hợp.");

				saveOrUpdateProduct(productRepository, "Màn hình Gaming AOC 27B2H Viền Siêu Mỏng", 2890000, 0, 40, catMonitor,
						"/uploads/monitor-aoc-gaming-27.jpg",
						"Thiết kế tràn viền 3 cạnh siêu mỏng hiện đại, tấm nền IPS Full HD 100Hz hiển thị sắc nét, công nghệ Low Blue Mode và Flicker-Free bảo vệ mắt tối đa khi làm việc ban đêm.");

				saveOrUpdateProduct(productRepository, "Màn hình Gaming ViewSonic Omni VX2728-2K", 5490000, 18, 30, catMonitor,
						"/uploads/monitor-viewsonic-omni.jpg",
						"Tần số quét 180Hz Fast IPS 2K sắc nét, thời gian phản hồi 0.5ms MPRT chống xé hình tuyệt đối, tích hợp loa kép tiện dụng và hỗ trợ treo tường VESA chuẩn.");
			}

			// 4. Khởi tạo đơn hàng mẫu nếu chưa có đơn hàng nào
			if (orderRepository.count() == 0) {
				var dong1Opt = userRepository.findByUsername("dong1");
				if (dong1Opt.isPresent()) {
					User dong1 = dong1Opt.get();
					var products = productRepository.findAll();
					if (products.size() >= 4) {
						// Đơn 1: Đã hoàn thành (3 ngày trước)
						Order order1 = new Order();
						order1.setCode("ORD-202609-1001");
						order1.setUser(dong1);
						order1.setOrderDate(LocalDateTime.now().minusDays(3));
						order1.setStatus(OrderStatus.COMPLETED);
						order1.setPaymentMethod("COD");
						order1.setFullname("Đồng");
						order1.setPhone("0987654321");
						order1.setAddress("Số 123 Đường Cầu Giấy, Hà Nội");
						order1.setNote("Giao giờ hành chính, gọi trước khi đến");

						Product p1 = products.get(0);
						Product p2 = products.get(1);
						double sub1 = p1.getDiscountedPrice() * 1.0;
						double sub2 = p2.getDiscountedPrice() * 1.0;
						order1.setTotalPrice(sub1 + sub2);
						order1.addOrderItem(new OrderItem(order1, p1, 1, (double) p1.getDiscountedPrice()));
						order1.addOrderItem(new OrderItem(order1, p2, 1, (double) p2.getDiscountedPrice()));
						orderRepository.save(order1);

						// Đơn 2: Đang giao hàng (1 ngày trước)
						Order order2 = new Order();
						order2.setCode("ORD-202609-1002");
						order2.setUser(dong1);
						order2.setOrderDate(LocalDateTime.now().minusDays(1));
						order2.setStatus(OrderStatus.SHIPPING);
						order2.setPaymentMethod("BANK_TRANSFER");
						order2.setFullname("Đồng");
						order2.setPhone("0987654321");
						order2.setAddress("Số 123 Đường Cầu Giấy, Hà Nội");
						order2.setNote("Để ở sảnh lễ tân nếu không nghe máy");

						Product p3 = products.get(2);
						double sub3 = p3.getDiscountedPrice() * 1.0;
						order2.setTotalPrice(sub3);
						order2.addOrderItem(new OrderItem(order2, p3, 1, (double) p3.getDiscountedPrice()));
						orderRepository.save(order2);

						// Đơn 3: Đã xác nhận (Hôm qua)
						Order order3 = new Order();
						order3.setCode("ORD-202609-1003");
						order3.setUser(dong1);
						order3.setOrderDate(LocalDateTime.now().minusHours(18));
						order3.setStatus(OrderStatus.CONFIRMED);
						order3.setPaymentMethod("VNPAY");
						order3.setFullname("Đồng");
						order3.setPhone("0987654321");
						order3.setAddress("Số 123 Đường Cầu Giấy, Hà Nội");
						order3.setNote("Đóng gói cẩn thận giúp mình nhé shop");

						Product p4 = products.get(3);
						double sub4 = p4.getDiscountedPrice() * 1.0;
						order3.setTotalPrice(sub4);
						order3.addOrderItem(new OrderItem(order3, p4, 1, (double) p4.getDiscountedPrice()));
						orderRepository.save(order3);

						// Đơn 4: Chờ xử lý (Hôm nay)
						Order order4 = new Order();
						order4.setCode("ORD-202609-1004");
						order4.setUser(dong1);
						order4.setOrderDate(LocalDateTime.now().minusHours(2));
						order4.setStatus(OrderStatus.PENDING);
						order4.setPaymentMethod("MOMO");
						order4.setFullname("Đồng");
						order4.setPhone("0987654321");
						order4.setAddress("Số 123 Đường Cầu Giấy, Hà Nội");
						order4.setNote("Giao hỏa tốc");

						double sub5 = p1.getDiscountedPrice() * 1.0;
						order4.setTotalPrice(sub5);
						order4.addOrderItem(new OrderItem(order4, p1, 1, (double) p1.getDiscountedPrice()));
						orderRepository.save(order4);
					}
				}
			}
		};
	}

	private void saveOrUpdateProduct(ProductRepository repo,
									 String title, int price, int discount, int quantity,
									 Category category, String thumbnail, String description) {
		Product p = repo.findByTitle(title).orElse(new Product());
		p.setTitle(title);
		p.setPrice(price);
		p.setDiscount(discount);
		p.setQuantity(quantity);
		p.setCategory(category);
		p.setThumbnail(thumbnail);
		p.setDescription(description);
		repo.save(p);
	}

	@org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
	public void openBrowserAfterStartup() {
		// Bỏ qua không mở trình duyệt khi đang chạy automated test
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			if (ste.getClassName().contains("junit") || ste.getClassName().contains("Test")) {
				return;
			}
		}

		String url = "http://localhost:8080";
		String os = System.getProperty("os.name").toLowerCase();
		try {
			if (os.contains("win")) {
				new ProcessBuilder("cmd", "/c", "start", url).start();
			} else if (os.contains("mac")) {
				new ProcessBuilder("open", url).start();
			} else if (os.contains("nix") || os.contains("nux")) {
				new ProcessBuilder("xdg-open", url).start();
			}
		} catch (Exception ignored) {
		}
	}
}
