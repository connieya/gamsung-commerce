# 클래스 다이어그램

## 상품 & 브랜드

```mermaid
classDiagram
    direction LR


    class Product {
        -Long id
        -String name
        -Long price
        -Long stockQuantity
        -Brand brand
    }

    class Brand {
        -Long id
        -String name
        -String description
    }

    class ProductLike {
        -Long id
        -User user
        -Product product
    }

 
    class ProductService {
        +getProductDetail(productId, userId) ProductDetailDto 
        +getProducts(query: ProductQuery, userId) List~ProductDetailDto~ 
        +getBrandDetail(brandId) Brand 
    }

    Product "N" -- "1" Brand : 소속
    Product "1" -- "N" ProductLike : 참조

    ProductService ..> Product : 상품 정보 조회
    ProductService ..> Brand : 브랜드 정보 조회
    ProductService ..> ProductLike : 좋아요 수 조회/사용자 좋아요 여부 확인
  

```

## 좋아요 

```mermaid
classDiagram
    direction LR

    class User {
        -Long id
        -String userId
        -String email
        -BirtDate birthDate
        -Gender gender
    }

    class Product {
        -Long id
        -String name
        -Long price
        -Long stockQuantity
        -Brand brand
    }

    class ProductLike {
        -Long id
        -User user
        -Product product
    }

    class ProductLikeService {
        +addLike(userId, productId) ProductLike 
        +removeLike(userId, productId) void 
        +getLikesByUser(userId) List~ProductLike~ 
    }

    User "1" -- "N" ProductLike : 참조
    Product "1" -- "N" ProductLike : 참조

 
    ProductLikeService ..> ProductLike : 좋아요 정보 생성/삭제/조회
```

## 주문

```mermaid
classDiagram
    direction TB

    class User {
        -Long id
        -String userId
        -String email
        -BirtDate birthDate
        -Gender gender
        
    }
    
    class Point {
        -Long id
        -User user
        -Long amount 
        +deduct(Long amount) void
        +add(Long amount) void 
    }

    class Product {
        -Long id
        -String name
        -Long price
        -Long stockQuantity
        -Brand brand
        +decreaseStock(Long quantity) void
    }

    class Order {
        -Long id
        -String orderNumber
        -User user
        -OrderStatus status 
        -int totalPaymentAmount 
        
    }

    class OrderItem {
        -Long id
        -Order order
        -Product product
        -int quantity 
        -Long orderPrice 
    }

    class OrderService {
        +order(userId, List~OrderCommand~) Order
        +getOrdersByUser(userId) List~Order~ 
        +getOrderDetail(orderId, userId) Order 
    }

    enum OrderStatus 
        PENDING_PAYMENT
        PAYMENT_COMPLETED
        SHIPPING
        DELIVERED
        CANCELED

    User "1"<--"1" Point : 참조
    User "1" -- "N" Order : 주문하다
    Order "1" -- "N" OrderItem : 참조
    OrderItem "N" -- "1" Product : 참조
    OrderService ..> Order : 생성/조회
    OrderService ..> OrderItem : 생성/조회
    OrderService ..> Point : 포인트 차감/조회
    OrderService ..> Product : 재고 차감/조회
    Order -- OrderStatus : 소속
```
