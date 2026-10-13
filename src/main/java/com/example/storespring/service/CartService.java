package com.example.storespring.service;

import com.example.storespring.dto.CartItemDTO;
import com.example.storespring.entity.CartItem;
import com.example.storespring.entity.Product;
import com.example.storespring.entity.User;
import com.example.storespring.repository.CartItemRepository;
import com.example.storespring.repository.ProductRepository;
import com.example.storespring.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CartService {

    public static final String SESSION_CART_KEY = "CART";

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    @Autowired
    public CartService(ProductRepository productRepository,
                       CartItemRepository cartItemRepository,
                       UserRepository userRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(HttpSession session) {
        if (session == null) return null;
        Object userObj = session.getAttribute("currentUser");
        if (userObj instanceof User u) {
            return u;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Map<Integer, CartItemDTO> getCartMap(HttpSession session) {
        Map<Integer, CartItemDTO> cart = (Map<Integer, CartItemDTO>) session.getAttribute(SESSION_CART_KEY);
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute(SESSION_CART_KEY, cart);
        }
        return cart;
    }

    public List<CartItemDTO> getCartItems(HttpSession session) {
        return new ArrayList<>(getCartMap(session).values());
    }

    @Transactional
    public boolean addToCart(HttpSession session, Integer productId, int quantity) {
        if (productId == null || quantity <= 0) {
            return false;
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return false;
        }

        Product product = productOpt.get();
        if (product.getQuantity() <= 0) {
            return false; // Hết hàng
        }

        Map<Integer, CartItemDTO> cart = getCartMap(session);
        CartItemDTO item = cart.get(productId);

        int newQuantity = quantity;
        if (item != null) {
            newQuantity = item.getQuantity() + quantity;
        }

        // Không cho vượt quá số lượng tồn kho
        if (newQuantity > product.getQuantity()) {
            newQuantity = product.getQuantity();
        }

        if (item == null) {
            item = new CartItemDTO(
                    product.getId(),
                    product.getTitle(),
                    product.getPrice(),
                    product.getDiscount(),
                    product.getDiscountedPrice(),
                    product.getThumbnail(),
                    newQuantity
            );
            cart.put(productId, item);
        } else {
            item.setQuantity(newQuantity);
        }

        session.setAttribute(SESSION_CART_KEY, cart);
        session.setAttribute("CART_COUNT", getTotalQuantity(session));

        // Lưu vào cơ sở dữ liệu nếu người dùng đã đăng nhập
        User currentUser = getCurrentUser(session);
        if (currentUser != null && currentUser.getId() != null) {
            Optional<CartItem> dbItemOpt = cartItemRepository.findByUserIdAndProductId(currentUser.getId(), productId);
            if (dbItemOpt.isPresent()) {
                CartItem dbItem = dbItemOpt.get();
                dbItem.setQuantity(newQuantity);
                cartItemRepository.save(dbItem);
            } else {
                User userRef = userRepository.findById(currentUser.getId()).orElse(currentUser);
                CartItem newCartItem = new CartItem(userRef, product, newQuantity);
                cartItemRepository.save(newCartItem);
            }
        }

        return true;
    }

    @Transactional
    public boolean updateQuantity(HttpSession session, Integer productId, int quantity) {
        Map<Integer, CartItemDTO> cart = getCartMap(session);
        if (!cart.containsKey(productId)) {
            return false;
        }

        User currentUser = getCurrentUser(session);

        if (quantity <= 0) {
            cart.remove(productId);
            if (currentUser != null && currentUser.getId() != null) {
                cartItemRepository.deleteByUserIdAndProductId(currentUser.getId(), productId);
            }
        } else {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                int stock = productOpt.get().getQuantity();
                int finalQty = Math.min(quantity, stock);
                cart.get(productId).setQuantity(finalQty);

                if (currentUser != null && currentUser.getId() != null) {
                    Optional<CartItem> dbItemOpt = cartItemRepository.findByUserIdAndProductId(currentUser.getId(), productId);
                    if (dbItemOpt.isPresent()) {
                        CartItem dbItem = dbItemOpt.get();
                        dbItem.setQuantity(finalQty);
                        cartItemRepository.save(dbItem);
                    }
                }
            } else {
                cart.remove(productId);
                if (currentUser != null && currentUser.getId() != null) {
                    cartItemRepository.deleteByUserIdAndProductId(currentUser.getId(), productId);
                }
            }
        }

        session.setAttribute(SESSION_CART_KEY, cart);
        session.setAttribute("CART_COUNT", getTotalQuantity(session));
        return true;
    }

    @Transactional
    public void removeItem(HttpSession session, Integer productId) {
        Map<Integer, CartItemDTO> cart = getCartMap(session);
        cart.remove(productId);
        session.setAttribute(SESSION_CART_KEY, cart);
        session.setAttribute("CART_COUNT", getTotalQuantity(session));

        User currentUser = getCurrentUser(session);
        if (currentUser != null && currentUser.getId() != null) {
            cartItemRepository.deleteByUserIdAndProductId(currentUser.getId(), productId);
        }
    }

    @Transactional
    public void clearCart(HttpSession session) {
        session.removeAttribute(SESSION_CART_KEY);
        session.removeAttribute("CART_COUNT");

        User currentUser = getCurrentUser(session);
        if (currentUser != null && currentUser.getId() != null) {
            cartItemRepository.deleteByUserId(currentUser.getId());
        }
    }

    // Đồng bộ giỏ hàng khi người dùng đăng nhập (nạp từ DB và gộp giỏ hàng khách)
    @Transactional
    public void syncUserCartOnLogin(HttpSession session, User user) {
        if (user == null || user.getId() == null) return;

        Map<Integer, CartItemDTO> sessionCart = getCartMap(session);
        // 1. Nếu khách đã thêm hàng trước khi đăng nhập, gộp vào database
        if (!sessionCart.isEmpty()) {
            for (CartItemDTO guestItem : sessionCart.values()) {
                Optional<CartItem> dbItemOpt = cartItemRepository.findByUserIdAndProductId(user.getId(), guestItem.getProductId());
                if (dbItemOpt.isPresent()) {
                    CartItem dbItem = dbItemOpt.get();
                    int mergedQty = Math.max(dbItem.getQuantity(), guestItem.getQuantity());
                    dbItem.setQuantity(mergedQty);
                    cartItemRepository.save(dbItem);
                } else {
                    Optional<Product> pOpt = productRepository.findById(guestItem.getProductId());
                    if (pOpt.isPresent()) {
                        User userRef = userRepository.findById(user.getId()).orElse(user);
                        cartItemRepository.save(new CartItem(userRef, pOpt.get(), guestItem.getQuantity()));
                    }
                }
            }
        }

        // 2. Tải toàn bộ giỏ hàng từ database lên Session cho người dùng
        List<CartItem> userDbItems = cartItemRepository.findByUserId(user.getId());
        Map<Integer, CartItemDTO> newCart = new LinkedHashMap<>();
        for (CartItem ci : userDbItems) {
            Product p = ci.getProduct();
            if (p != null && p.getQuantity() > 0) {
                int actualQty = Math.min(ci.getQuantity(), p.getQuantity());
                CartItemDTO dto = new CartItemDTO(
                        p.getId(),
                        p.getTitle(),
                        p.getPrice(),
                        p.getDiscount(),
                        p.getDiscountedPrice(),
                        p.getThumbnail(),
                        actualQty
                );
                newCart.put(p.getId(), dto);
            }
        }

        session.setAttribute(SESSION_CART_KEY, newCart);
        session.setAttribute("CART_COUNT", newCart.values().stream().mapToInt(CartItemDTO::getQuantity).sum());
    }

    // Xóa giỏ hàng khỏi session khi đăng xuất để khách vãng lai không thấy giỏ hàng của user
    public void clearGuestCartOnLogout(HttpSession session) {
        if (session != null) {
            session.removeAttribute(SESSION_CART_KEY);
            session.removeAttribute("CART_COUNT");
            session.removeAttribute("currentUser");
        }
    }

    public Double getTotalAmount(HttpSession session) {
        return getCartItems(session).stream()
                .mapToDouble(CartItemDTO::getSubtotal)
                .sum();
    }

    public int getTotalQuantity(HttpSession session) {
        return getCartItems(session).stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();
    }
}
