/* ========================================================
   AURA - MAIN JAVASCRIPT
   AJAX Cart, Toast Notifications, Animations
   ======================================================== */

document.addEventListener('DOMContentLoaded', function () {

    // === Header Scroll Effect (Glassmorphism tăng cường khi cuộn) ===
    const header = document.querySelector('.header');
    if (header) {
        window.addEventListener('scroll', function () {
            if (window.scrollY > 30) {
                header.classList.add('scrolled');
            } else {
                header.classList.remove('scrolled');
            }
        });
    }

    // === Toast Notification System ===
    window.showToast = function (message, type) {
        type = type || 'success';
        let container = document.querySelector('.toast-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        const iconMap = {
            success: '✓',
            error: '✕',
            info: 'ℹ'
        };

        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.innerHTML =
            '<span class="toast-icon">' + (iconMap[type] || '✓') + '</span>' +
            '<span>' + message + '</span>';

        container.appendChild(toast);

        // Tự động ẩn sau 3 giây
        setTimeout(function () {
            toast.classList.add('toast-out');
            setTimeout(function () {
                toast.remove();
            }, 300);
        }, 3000);
    };

    // === Cart Badge Bounce Animation ===
    window.bounceCartBadge = function () {
        var badge = document.querySelector('.cart-badge');
        if (badge) {
            badge.classList.remove('bounce');
            // Trigger reflow
            void badge.offsetWidth;
            badge.classList.add('bounce');
        }
    };

    // === AJAX Add to Cart ===
    document.addEventListener('click', function (e) {
        var addBtn = e.target.closest('.btn-add-cart, .btn-add-cart-ajax');
        if (!addBtn) return;

        e.preventDefault();
        var productId = addBtn.getAttribute('data-product-id');
        var quantity = 1;

        // Nếu có input quantity (trang chi tiết SP)
        var qtyInput = document.querySelector('.qty-input');
        if (qtyInput) {
            quantity = parseInt(qtyInput.value) || 1;
        }

        addBtn.disabled = true;
        var originalHTML = addBtn.innerHTML;
        addBtn.innerHTML = '⟳ Đang thêm...';

        fetch('/cart/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'productId=' + encodeURIComponent(productId) + '&quantity=' + encodeURIComponent(quantity)
        })
            .then(function (response) { return response.json(); })
            .then(function (data) {
                if (data.success) {
                    showToast(data.message || 'Đã thêm vào giỏ hàng!', 'success');
                    // Cập nhật badge số lượng
                    var badge = document.querySelector('.cart-badge');
                    if (badge && data.totalItems !== undefined) {
                        badge.textContent = data.totalItems;
                        bounceCartBadge();
                    }
                } else {
                    showToast(data.message || 'Không thể thêm vào giỏ hàng!', 'error');
                }
            })
            .catch(function () {
                showToast('Có lỗi xảy ra, vui lòng thử lại!', 'error');
            })
            .finally(function () {
                addBtn.disabled = false;
                addBtn.innerHTML = originalHTML;
            });
    });

    // === Buy Now (Mua ngay) Handler ===
    document.addEventListener('click', function (e) {
        var buyBtn = e.target.closest('.btn-buy-now');
        if (!buyBtn) return;

        e.preventDefault();
        var productId = buyBtn.getAttribute('data-product-id');
        var quantity = 1;

        var qtyInput = document.querySelector('.qty-input');
        if (qtyInput) {
            quantity = parseInt(qtyInput.value) || 1;
        }

        buyBtn.disabled = true;
        var originalHTML = buyBtn.innerHTML;
        buyBtn.innerHTML = '⟳ Đang chuyển hướng...';

        fetch('/cart/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'productId=' + encodeURIComponent(productId) + '&quantity=' + encodeURIComponent(quantity)
        })
            .then(function (response) { return response.json(); })
            .then(function (data) {
                if (data.success) {
                    window.location.href = '/checkout';
                } else {
                    showToast(data.message || 'Không thể tiến hành mua ngay!', 'error');
                    buyBtn.disabled = false;
                    buyBtn.innerHTML = originalHTML;
                }
            })
            .catch(function () {
                showToast('Có lỗi xảy ra, vui lòng thử lại!', 'error');
                buyBtn.disabled = false;
                buyBtn.innerHTML = originalHTML;
            });
    });

    // === Checkout Payment Method Card Selection ===
    document.addEventListener('change', function (e) {
        if (e.target.matches('input[name="paymentMethod"]')) {
            var selectedVal = e.target.value;
            // Update active styling on all payment cards
            document.querySelectorAll('.payment-card').forEach(function (card) {
                var radio = card.querySelector('input[name="paymentMethod"]');
                if (radio && radio.checked) {
                    card.classList.add('active');
                } else {
                    card.classList.remove('active');
                }
            });

            // Toggle corresponding detail panels
            document.querySelectorAll('.payment-subpanel').forEach(function (panel) {
                if (panel.getAttribute('data-for') === selectedVal) {
                    panel.style.display = 'block';
                } else {
                    panel.style.display = 'none';
                }
            });
        }
    });

    // === Quantity Control (+/-) ===
    document.addEventListener('click', function (e) {
        if (e.target.classList.contains('qty-btn')) {
            var action = e.target.getAttribute('data-action');
            var input = e.target.closest('.qty-control').querySelector('.qty-input');
            var currentVal = parseInt(input.value) || 1;
            var max = parseInt(input.getAttribute('max')) || 999;

            if (action === 'increase' && currentVal < max) {
                input.value = currentVal + 1;
            } else if (action === 'decrease' && currentVal > 1) {
                input.value = currentVal - 1;
            }

            // Trigger change event for cart page auto-update
            input.dispatchEvent(new Event('change'));
        }
    });

    // === Cart Page: Auto update quantity via AJAX ===
    document.addEventListener('change', function (e) {
        if (e.target.classList.contains('cart-qty-input')) {
            var productId = e.target.getAttribute('data-product-id');
            var newQty = parseInt(e.target.value) || 1;

            fetch('/cart/update', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'productId=' + encodeURIComponent(productId) + '&quantity=' + encodeURIComponent(newQty)
            })
                .then(function (response) { return response.json(); })
                .then(function (data) {
                    if (data.success) {
                        // Reload trang giỏ hàng để cập nhật tổng tiền
                        window.location.reload();
                    }
                })
                .catch(function () {
                    showToast('Lỗi cập nhật số lượng!', 'error');
                });
        }
    });

    // === Intersection Observer: Animate elements on scroll ===
    var observerOptions = {
        root: null,
        rootMargin: '0px',
        threshold: 0.1
    };

    var observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-fade-in');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);

    document.querySelectorAll('.product-card, .category-chip, .filter-card').forEach(function (el) {
        observer.observe(el);
    });

    // === Format price display (VND) ===
    window.formatPrice = function (price) {
        return new Intl.NumberFormat('vi-VN').format(price) + ' ₫';
    };

    // ========================================================
    // 1. HERO PARTICLE CANVAS (Mạng lưới hạt công nghệ)
    // ========================================================
    function initHeroCanvas() {
        var canvas = document.getElementById('hero-canvas');
        if (!canvas) return;

        var ctx = canvas.getContext('2d');
        var hero = document.getElementById('hero-section') || canvas.parentElement;
        var width, height;
        var particles = [];
        var numParticles = 48;
        var mouse = { x: -1000, y: -1000, radius: 140 };
        var animationFrameId = null;
        var isCanvasActive = true;

        function resize() {
            width = canvas.width = hero.offsetWidth;
            height = canvas.height = hero.offsetHeight;
        }

        window.addEventListener('resize', resize);
        resize();

        function Particle() {
            this.x = Math.random() * width;
            this.y = Math.random() * height;
            this.vx = (Math.random() - 0.5) * 0.7;
            this.vy = (Math.random() - 0.5) * 0.7;
            this.size = Math.random() * 2 + 1;
            this.color = Math.random() > 0.35 ? 'rgba(0, 212, 255, ' : 'rgba(168, 85, 247, ';
            this.baseAlpha = Math.random() * 0.45 + 0.25;
        }

        Particle.prototype.update = function () {
            this.x += this.vx;
            this.y += this.vy;

            if (this.x < 0 || this.x > width) this.vx *= -1;
            if (this.y < 0 || this.y > height) this.vy *= -1;

            var dx = mouse.x - this.x;
            var dy = mouse.y - this.y;
            var dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < mouse.radius) {
                var force = (mouse.radius - dist) / mouse.radius;
                this.x -= (dx / dist) * force * 2.5;
                this.y -= (dy / dist) * force * 2.5;
            }
        };

        Particle.prototype.draw = function () {
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
            ctx.fillStyle = this.color + this.baseAlpha + ')';
            ctx.fill();
        };

        for (var i = 0; i < numParticles; i++) {
            particles.push(new Particle());
        }

        hero.addEventListener('mousemove', function (e) {
            var rect = hero.getBoundingClientRect();
            mouse.x = e.clientX - rect.left;
            mouse.y = e.clientY - rect.top;
        });

        hero.addEventListener('mouseleave', function () {
            mouse.x = -1000;
            mouse.y = -1000;
        });

        function animate() {
            if (!isCanvasActive) return;
            ctx.clearRect(0, 0, width, height);

            for (var a = 0; a < particles.length; a++) {
                for (var b = a + 1; b < particles.length; b++) {
                    var dx = particles[a].x - particles[b].x;
                    var dy = particles[a].y - particles[b].y;
                    var dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist < 115) {
                        var alpha = (1 - dist / 115) * 0.22;
                        ctx.beginPath();
                        ctx.moveTo(particles[a].x, particles[a].y);
                        ctx.lineTo(particles[b].x, particles[b].y);
                        ctx.strokeStyle = 'rgba(0, 212, 255, ' + alpha + ')';
                        ctx.lineWidth = 1;
                        ctx.stroke();
                    }
                }
            }

            for (var j = 0; j < particles.length; j++) {
                particles[j].update();
                particles[j].draw();
            }

            animationFrameId = requestAnimationFrame(animate);
        }

        // Tạm dừng vẽ canvas khi cuộn khỏi màn hình để tiết kiệm tài nguyên
        var heroObserver = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    if (!isCanvasActive) {
                        isCanvasActive = true;
                        animate();
                    }
                } else {
                    isCanvasActive = false;
                    if (animationFrameId) cancelAnimationFrame(animationFrameId);
                }
            });
        }, { threshold: 0.05 });

        heroObserver.observe(hero);
        animate();
    }

    // ========================================================
    // 2. TYPEWRITER EFFECT (Tagline tự động gõ)
    // ========================================================
    function initTypewriter() {
        var el = document.getElementById('hero-typed-text');
        if (!el) return;

        var words = [
            'Bàn Phím Cơ Custom',
            'Chuột Gaming 8000Hz',
            'Tai Nghe Studio 7.1',
            'Lót Chuột Artisan Precision'
        ];
        var wordIdx = 0;
        var charIdx = 0;
        var isDeleting = false;
        var typeSpeed = 90;

        function type() {
            var currentWord = words[wordIdx];

            if (isDeleting) {
                el.textContent = currentWord.substring(0, charIdx - 1);
                charIdx--;
                typeSpeed = 40;
            } else {
                el.textContent = currentWord.substring(0, charIdx + 1);
                charIdx++;
                typeSpeed = 90;
            }

            if (!isDeleting && charIdx === currentWord.length) {
                typeSpeed = 2200;
                isDeleting = true;
            } else if (isDeleting && charIdx === 0) {
                isDeleting = false;
                wordIdx = (wordIdx + 1) % words.length;
                typeSpeed = 450;
            }

            setTimeout(type, typeSpeed);
        }

        type();
    }

    // ========================================================
    // 3. CYBER GLITCH CONTROLLER
    // ========================================================
    function initCyberGlitch() {
        var heroTitle = document.querySelector('.hero-title');
        if (!heroTitle) return;

        setInterval(function () {
            heroTitle.classList.add('glitch-active');
            setTimeout(function () {
                heroTitle.classList.remove('glitch-active');
            }, 320);
        }, 4500);
    }

    // ========================================================
    // 4. 3D HERO PRODUCT MOUSE PARALLAX
    // ========================================================
    function initHeroParallax() {
        var card = document.getElementById('hero-floating-card');
        var hero = document.getElementById('hero-section');
        if (!card || !hero) return;

        if (!window.matchMedia('(hover: hover)').matches) return;

        var targetRotX = 0;
        var targetRotY = 0;
        var currentRotX = 0;
        var currentRotY = 0;

        hero.addEventListener('mousemove', function (e) {
            var rect = hero.getBoundingClientRect();
            var x = (e.clientX - rect.left) / rect.width - 0.5;
            var y = (e.clientY - rect.top) / rect.height - 0.5;

            targetRotY = x * 20;
            targetRotX = -y * 20;
        });

        hero.addEventListener('mouseleave', function () {
            targetRotX = 0;
            targetRotY = 0;
        });

        function updateParallax() {
            currentRotX += (targetRotX - currentRotX) * 0.08;
            currentRotY += (targetRotY - currentRotY) * 0.08;

            card.style.transform = 'rotateX(' + currentRotX.toFixed(2) + 'deg) rotateY(' + currentRotY.toFixed(2) + 'deg)';
            requestAnimationFrame(updateParallax);
        }

        updateParallax();
    }

    // ========================================================
    // 5. PRODUCT CARD 3D TILT EFFECT
    // ========================================================
    function initProductCardTilt() {
        if (!window.matchMedia('(hover: hover)').matches) return;

        var cards = document.querySelectorAll('.product-card');
        cards.forEach(function (card) {
            card.addEventListener('mousemove', function (e) {
                var rect = card.getBoundingClientRect();
                var x = e.clientX - rect.left;
                var y = e.clientY - rect.top;

                var centerX = rect.width / 2;
                var centerY = rect.height / 2;

                var rotX = ((y - centerY) / centerY) * -7;
                var rotY = ((x - centerX) / centerX) * 7;

                card.style.transform = 'perspective(900px) rotateX(' + rotX.toFixed(1) + 'deg) rotateY(' + rotY.toFixed(1) + 'deg) translateY(-8px)';
            });

            card.addEventListener('mouseleave', function () {
                card.style.transform = '';
            });
        });
    }

    // ========================================================
    // 6. REAL INTERACTIVE 3D MECHANICAL KEYBOARD (THREE.JS)
    // ========================================================
    function init3DKeyboard() {
        var canvas = document.getElementById('keyboard-3d-canvas');
        var fallbackImg = document.getElementById('hero-fallback-img');
        var container = document.getElementById('hero-product-frame');
        if (!canvas || !container) return;

        // Nếu Three.js chưa sẵn sàng, giữ nguyên ảnh 3D fallback
        if (typeof THREE === 'undefined') {
            if (fallbackImg) fallbackImg.style.display = 'block';
            return;
        }

        try {
            var width = container.offsetWidth || 440;
            var height = container.offsetHeight || 440;

            var scene = new THREE.Scene();
            var camera = new THREE.PerspectiveCamera(38, width / height, 0.1, 1000);
            camera.position.set(0, 4.4, 7.8);
            camera.lookAt(0, 0, 0);

            var renderer = new THREE.WebGLRenderer({
                canvas: canvas,
                alpha: true,
                antialias: true
            });
            renderer.setSize(width, height);
            renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

            // Lighting
            var ambientLight = new THREE.AmbientLight(0xffffff, 0.8);
            scene.add(ambientLight);

            // Left Neon Cyan Point Light
            var cyanLight = new THREE.PointLight(0x00d4ff, 2.8, 20);
            cyanLight.position.set(-4, 3, 3);
            scene.add(cyanLight);

            // Right Neon Purple Point Light
            var purpleLight = new THREE.PointLight(0xa855f7, 2.8, 20);
            purpleLight.position.set(4, 3, 3);
            scene.add(purpleLight);

            // Top directional light
            var topLight = new THREE.DirectionalLight(0xffffff, 0.9);
            topLight.position.set(0, 6, 4);
            scene.add(topLight);

            // Keyboard Group
            var keyboard = new THREE.Group();
            scene.add(keyboard);

            // Case (Chassis)
            var caseGeo = new THREE.BoxGeometry(5.8, 0.42, 2.4);
            var caseMat = new THREE.MeshStandardMaterial({
                color: 0x0f172a,
                roughness: 0.35,
                metalness: 0.8
            });
            var caseMesh = new THREE.Mesh(caseGeo, caseMat);
            caseMesh.position.y = -0.21;
            keyboard.add(caseMesh);

            // Bevel rim / chamfer
            var rimGeo = new THREE.BoxGeometry(5.95, 0.1, 2.55);
            var rimMat = new THREE.MeshStandardMaterial({
                color: 0x00d4ff,
                roughness: 0.2,
                metalness: 0.9,
                emissive: 0x003344,
                emissiveIntensity: 0.6
            });
            var rimMesh = new THREE.Mesh(rimGeo, rimMat);
            rimMesh.position.y = -0.38;
            keyboard.add(rimMesh);

            // Keycaps Grid (60% Layout, 5 rows)
            var keycaps = [];
            var rows = [
                { count: 14, w: 0.32, gap: 0.07, z: -0.8 },
                { count: 14, w: 0.32, gap: 0.07, z: -0.4 },
                { count: 13, w: 0.34, gap: 0.08, z: 0.0 },
                { count: 12, w: 0.37, gap: 0.08, z: 0.4 },
                { count: 7,  w: 0.55, gap: 0.12, z: 0.8 } // Spacebar row
            ];

            var keyMatDark = new THREE.MeshStandardMaterial({
                color: 0x1e293b,
                roughness: 0.4,
                metalness: 0.25
            });
            var keyMatCyan = new THREE.MeshStandardMaterial({
                color: 0x00d4ff,
                roughness: 0.2,
                metalness: 0.35,
                emissive: 0x00d4ff,
                emissiveIntensity: 0.4
            });
            var keyMatPurple = new THREE.MeshStandardMaterial({
                color: 0xa855f7,
                roughness: 0.2,
                metalness: 0.35,
                emissive: 0xa855f7,
                emissiveIntensity: 0.4
            });

            rows.forEach(function (row, rIdx) {
                var totalW = row.count * (row.w + row.gap);
                var startX = -totalW / 2 + row.w / 2;

                for (var i = 0; i < row.count; i++) {
                    var curGeo = (rIdx === 4 && i === 3)
                        ? new THREE.BoxGeometry(1.8, 0.22, 0.3)
                        : new THREE.BoxGeometry(row.w, 0.22, 0.3);

                    var mat = keyMatDark;
                    if (rIdx === 0 && (i === 0 || i === 13)) mat = keyMatPurple;
                    else if (rIdx === 2 && (i === 1 || i === 2 || i === 3)) mat = keyMatCyan;
                    else if (rIdx === 1 && i === 2) mat = keyMatCyan;
                    else if (rIdx === 4 && i === 3) mat = keyMatCyan;
                    else if (Math.random() > 0.75) mat = keyMatPurple;

                    var keyMesh = new THREE.Mesh(curGeo, mat);
                    keyMesh.position.set(startX + i * (row.w + row.gap), 0.11, row.z);
                    keyMesh.userData = { originalY: 0.11 };
                    keyboard.add(keyMesh);
                    keycaps.push(keyMesh);
                }
            });

            // Rotary Volume Knob
            var knobGeo = new THREE.CylinderGeometry(0.18, 0.18, 0.24, 24);
            var knobMat = new THREE.MeshStandardMaterial({
                color: 0x00d4ff,
                metalness: 0.9,
                roughness: 0.15
            });
            var knob = new THREE.Mesh(knobGeo, knobMat);
            knob.position.set(2.4, 0.16, -0.85);
            keyboard.add(knob);

            // Initial sport angle
            keyboard.rotation.x = 0.55;
            keyboard.rotation.y = -0.45;
            keyboard.rotation.z = 0.15;

            var targetRotX = 0.55;
            var targetRotY = -0.45;

            var raycaster = new THREE.Raycaster();
            var mousePos = new THREE.Vector2();

            container.addEventListener('mousemove', function (e) {
                var rect = container.getBoundingClientRect();
                var nx = (e.clientX - rect.left) / rect.width * 2 - 1;
                var ny = -((e.clientY - rect.top) / rect.height * 2 - 1);

                targetRotY = -0.45 + nx * 0.75;
                targetRotX = 0.55 - ny * 0.55;

                mousePos.x = nx;
                mousePos.y = ny;

                raycaster.setFromCamera(mousePos, camera);
                var intersects = raycaster.intersectObjects(keycaps);
                keycaps.forEach(function (k) {
                    k.position.y = k.userData.originalY;
                });
                if (intersects.length > 0) {
                    intersects[0].object.position.y = intersects[0].object.userData.originalY - 0.08;
                }
            });

            container.addEventListener('mouseleave', function () {
                targetRotX = 0.55;
                targetRotY = -0.45;
                keycaps.forEach(function (k) {
                    k.position.y = k.userData.originalY;
                });
            });

            // Render loop with idle floating
            var clock = new THREE.Clock();
            function animate3D() {
                requestAnimationFrame(animate3D);
                var elapsedTime = clock.getElapsedTime();

                keyboard.rotation.x += (targetRotX - keyboard.rotation.x) * 0.08;
                keyboard.rotation.y += (targetRotY - keyboard.rotation.y) * 0.08;
                keyboard.position.y = Math.sin(elapsedTime * 1.6) * 0.12;

                knob.rotation.y = elapsedTime * 0.5;

                cyanLight.intensity = 2.4 + Math.sin(elapsedTime * 3) * 0.5;
                purpleLight.intensity = 2.4 + Math.cos(elapsedTime * 2.5) * 0.5;

                renderer.render(scene, camera);
            }

            animate3D();

            // Hiển thị canvas 3D và ẩn ảnh tĩnh fallback
            canvas.style.display = 'block';
            if (fallbackImg) fallbackImg.style.display = 'none';

        } catch (err) {
            console.warn('Three.js initialization failed, using 3D image fallback:', err);
            canvas.style.display = 'none';
            if (fallbackImg) fallbackImg.style.display = 'block';
        }
    }

    // Khởi chạy toàn bộ hiệu ứng
    initHeroCanvas();
    initTypewriter();
    initCyberGlitch();
    initHeroParallax();
    initProductCardTilt();
    init3DKeyboard();

});
