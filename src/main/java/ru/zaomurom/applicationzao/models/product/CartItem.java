    package ru.zaomurom.applicationzao.models.product;
    import jakarta.persistence.*;

    @Entity
    public class CartItem {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "cart_id")
        private Cart cart;

        @ManyToOne
        @JoinColumn(name = "product_id")
        private Product product;

        private int quantity;

        @ManyToOne
        @JoinColumn(name = "sum_id")
        private Sum sum;

        public CartItem() {}

        public CartItem(Cart cart, Product product, int quantity, Sum sum) {
            this.cart = cart;
            this.product = product;
            this.quantity = quantity;
            this.sum = sum;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Cart getCart() {
            return cart;
        }

        public void setCart(Cart cart) {
            this.cart = cart;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public Sum getSum() {
            return sum;
        }

        public void setSum(Sum sum) {
            this.sum = sum;
        }
    }
