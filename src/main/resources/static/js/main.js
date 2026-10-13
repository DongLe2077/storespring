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
    // 6. DARK PREMIUM HEADSET SHOWCASE (THREE.JS)
    // Keep the existing initializer name and DOM hooks for compatibility.
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
            camera.position.set(0, 0.4, 6.8);
            camera.lookAt(0, 0.1, 0);

            var renderer = new THREE.WebGLRenderer({
                canvas: canvas,
                alpha: true,
                antialias: true
            });
            renderer.setSize(width, height);
            renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

            // Lighting
            var ambientLight = new THREE.AmbientLight(0xb9c9da, 0.65);
            scene.add(ambientLight);

            // Left Neon Cyan Point Light
            var cyanLight = new THREE.PointLight(0x65dce8, 0.9, 20);
            cyanLight.position.set(-4, 3, 3);
            scene.add(cyanLight);

            // Right Neon Purple Point Light
            var purpleLight = new THREE.PointLight(0xb5c6e2, 0.75, 20);
            purpleLight.position.set(4, 3, 3);
            scene.add(purpleLight);

            // Top directional light
            var topLight = new THREE.DirectionalLight(0xffffff, 1.1);
            topLight.position.set(0, 6, 4);
            scene.add(topLight);

            // The existing group and animation hooks now display a headset.
            var keyboard = new THREE.Group();
            scene.add(keyboard);

            // Graphite headband, leather padding and brushed metal rails.
            var caseGeo = new THREE.TorusGeometry(1.25, 0.105, 18, 96, Math.PI);
            var caseMat = new THREE.MeshStandardMaterial({
                color: 0x26313e,
                roughness: 0.48,
                metalness: 0.45
            });
            var caseMesh = new THREE.Mesh(caseGeo, caseMat);
            caseMesh.position.y = 0.22;
            keyboard.add(caseMesh);

            var rimGeo = new THREE.TorusGeometry(1.29, 0.022, 12, 96, Math.PI);
            var rimMat = new THREE.MeshStandardMaterial({
                color: 0x9bacbd,
                roughness: 0.28,
                metalness: 0.85
            });
            var rimMesh = new THREE.Mesh(rimGeo, rimMat);
            rimMesh.position.set(0, 0.22, 0.085);
            keyboard.add(rimMesh);

            var padding = new THREE.Mesh(
                new THREE.TorusGeometry(1.13, 0.09, 18, 96, Math.PI),
                new THREE.MeshStandardMaterial({ color: 0x101820, roughness: 0.95, metalness: 0.02 })
            );
            padding.position.set(0, 0.22, 0);
            keyboard.add(padding);

            // Retain the existing hover collection for the headset controls.
            var keycaps = [];
            var keyMatDark = new THREE.MeshStandardMaterial({
                color: 0x253443,
                roughness: 0.43,
                metalness: 0.4
            });
            var keyMatCyan = new THREE.MeshStandardMaterial({
                color: 0x6ad9e3,
                roughness: 0.32,
                metalness: 0.5,
                emissive: 0x195a65,
                emissiveIntensity: 0.3
            });
            var keyMatPurple = new THREE.MeshStandardMaterial({
                color: 0x101820,
                roughness: 0.93,
                metalness: 0.02
            });

            [-1, 1].forEach(function (side) {
                var cup = new THREE.Group();
                cup.position.set(side * 1.13, -0.55, 0);
                cup.rotation.y = side * -0.23;
                cup.rotation.z = side * 0.09;
                keyboard.add(cup);

                var cushion = new THREE.Mesh(new THREE.SphereGeometry(1, 40, 32), keyMatPurple);
                cushion.scale.set(0.53, 0.73, 0.3);
                cushion.position.z = -0.11;
                cup.add(cushion);

                var shell = new THREE.Mesh(new THREE.SphereGeometry(1, 40, 32), keyMatDark);
                shell.scale.set(0.5, 0.69, 0.25);
                shell.position.z = 0.09;
                cup.add(shell);

                var trim = new THREE.Mesh(new THREE.TorusGeometry(0.49, 0.014, 10, 80), keyMatCyan);
                trim.scale.y = 1.34;
                trim.position.z = 0.24;
                cup.add(trim);

                var panel = new THREE.Mesh(new THREE.SphereGeometry(1, 32, 24), caseMat);
                panel.scale.set(0.4, 0.55, 0.08);
                panel.position.z = 0.3;
                cup.add(panel);

                var yoke = new THREE.Mesh(new THREE.CylinderGeometry(0.045, 0.045, 0.44, 16), rimMat);
                yoke.position.set(side * 0.12, 0.62, 0);
                cup.add(yoke);

                // Small cyan emblem, rather than oversized luminous surfaces.
                var emblem = new THREE.Mesh(new THREE.TorusGeometry(0.105, 0.012, 8, 36), keyMatCyan);
                emblem.position.set(0, 0.02, 0.39);
                cup.add(emblem);

                var keyMesh = new THREE.Mesh(new THREE.SphereGeometry(0.045, 16, 12), keyMatCyan);
                keyMesh.position.set(side * 1.13, -0.96, 0.37);
                keyMesh.userData = { originalY: -0.96 };
                keyboard.add(keyMesh);
                keycaps.push(keyMesh);
            });

            // Slim boom microphone, using the same scene and renderer.
            var micCurve = new THREE.CatmullRomCurve3([
                new THREE.Vector3(-1.4, -0.91, 0.12),
                new THREE.Vector3(-1.48, -1.18, 0.43),
                new THREE.Vector3(-1.04, -1.45, 0.72),
                new THREE.Vector3(-0.62, -1.43, 0.8)
            ]);
            keyboard.add(new THREE.Mesh(new THREE.TubeGeometry(micCurve, 32, 0.035, 10, false), caseMat));
            var micTip = new THREE.Mesh(new THREE.SphereGeometry(1, 24, 16), keyMatPurple);
            micTip.scale.set(0.16, 0.085, 0.085);
            micTip.position.set(-0.62, -1.43, 0.8);
            keyboard.add(micTip);

            // Rotary Volume Knob
            var knobGeo = new THREE.CylinderGeometry(0.065, 0.065, 0.045, 24);
            var knobMat = new THREE.MeshStandardMaterial({
                color: 0xa4b2bf,
                metalness: 0.85,
                roughness: 0.3
            });
            var knob = new THREE.Mesh(knobGeo, knobMat);
            knob.position.set(1.48, -0.91, 0.05);
            knob.rotation.z = Math.PI / 2;
            keyboard.add(knob);

            // Initial sport angle
            keyboard.rotation.x = 0.12;
            keyboard.rotation.y = -0.3;
            keyboard.rotation.z = -0.07;

            var targetRotX = 0.12;
            var targetRotY = -0.3;

            var raycaster = new THREE.Raycaster();
            var mousePos = new THREE.Vector2();

            container.addEventListener('mousemove', function (e) {
                var rect = container.getBoundingClientRect();
                var nx = (e.clientX - rect.left) / rect.width * 2 - 1;
                var ny = -((e.clientY - rect.top) / rect.height * 2 - 1);

                targetRotY = -0.3 + nx * 0.4;
                targetRotX = 0.12 - ny * 0.25;

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
                targetRotX = 0.12;
                targetRotY = -0.3;
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

                cyanLight.intensity = 0.85 + Math.sin(elapsedTime * 3) * 0.08;
                purpleLight.intensity = 0.65 + Math.cos(elapsedTime * 2.5) * 0.06;

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
