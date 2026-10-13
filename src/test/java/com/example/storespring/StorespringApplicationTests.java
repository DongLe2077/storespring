package com.example.storespring;

import com.example.storespring.entity.User;
import com.example.storespring.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StorespringApplicationTests {

	@Autowired
	private UserService userService;

	@Test
	void contextLoads() {
	}

	@Autowired
	private com.example.storespring.service.CartService cartService;

	@Autowired
	private com.example.storespring.repository.ProductRepository productRepository;

	@Autowired
	private com.example.storespring.repository.CartItemRepository cartItemRepository;

	@Test
	void testDong1UserCanLogin() {
		User user = userService.login("dong1", "123456");
		Assertions.assertNotNull(user);
		Assertions.assertEquals("dong1", user.getUsername());
		Assertions.assertEquals("USER", user.getRole());
	}

	@Test
	void testCartPersistenceAcrossLogoutAndLogin() {
		User user = userService.login("dong1", "123456");
		cartItemRepository.deleteByUserId(user.getId());
		var products = productRepository.findAll();
		Assertions.assertFalse(products.isEmpty());
		var product = products.get(0);

		// Session 1: User is logged in and adds item
		org.springframework.mock.web.MockHttpSession session1 = new org.springframework.mock.web.MockHttpSession();
		session1.setAttribute("currentUser", user);
		cartService.addToCart(session1, product.getId(), 2);

		Assertions.assertEquals(2, cartService.getTotalQuantity(session1));
		Assertions.assertFalse(cartItemRepository.findByUserId(user.getId()).isEmpty());

		// User logs out: Guest session cart must be cleared
		cartService.clearGuestCartOnLogout(session1);
		Assertions.assertEquals(0, cartService.getTotalQuantity(session1));
		Assertions.assertNull(session1.getAttribute("currentUser"));

		// Session 2: User logs in again on a new session (e.g. after server restart)
		org.springframework.mock.web.MockHttpSession session2 = new org.springframework.mock.web.MockHttpSession();
		session2.setAttribute("currentUser", user);
		cartService.syncUserCartOnLogin(session2, user);

		// Cart must be restored from DB!
		Assertions.assertEquals(2, cartService.getTotalQuantity(session2));
		Assertions.assertEquals(1, cartService.getCartItems(session2).size());
		Assertions.assertEquals(product.getId(), cartService.getCartItems(session2).get(0).getProductId());

		// Cleanup after test
		cartItemRepository.deleteByUserId(user.getId());
	}
}
