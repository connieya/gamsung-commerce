# 시퀀스 다이어그램

## 상품 

### 상품 목록 

```mermaid
sequenceDiagram
    participant U as 사용자 (User)
    participant S as 상품 목록 화면 (UI)
    participant M as 상품 관리자 (Manager)
    participant D as 데이터 저장소 (Database)

    U->>+S: 상품 목록 페이지 방문
   

    S->>+M: 상품 목록을 보여주세요. (기본: 최신순)
    

    M->>+D: 최신순으로 정렬된 상품들의 <br> 요약 정보를 조회합니다.
   

    D-->>-M: 상품 정보 목록 반환
   

    M-->>-S: 화면에 표시할 형태로 <br> 가공된 상품 정보 전달
   

    S-->>-U: 최신 상품 목록을 화면에 표시
    
 
```

### 상품 상세


```mermaid
sequenceDiagram
    participant U as 사용자 (User)
    participant S as 상품 상세 화면 (UI)
    participant M as 상품 관리자 (Manager)
    participant D as 데이터 저장소 (Database)

    U->>+S: 상품 목록에서 특정 상품 클릭
  

    S->>+M: 이 상품의 상세 정보를 보여주세요.
    

    M->>+D: 선택된 상품의 모든 상세 정보를 조회합니다.
   

    D-->>-M: 상품의 상세 정보 반환
    

    M-->>-S: 화면에 표시할 형태로 <br> 가공된 상세 정보 전달
    

    S-->>-U: 상품의 상세 정보를 화면에 표시
    
```

## 브랜드

### 브랜드 정보 조회 

```mermaid
sequenceDiagram
    participant U as 사용자 (User)
    participant S as 브랜드 페이지 (UI)
    participant M as 상품 관리자 (Manager)
    participant D as 데이터 저장소 (Database)

    U->>+S: 상품 상세 화면에서 브랜드 이름 클릭
    

    S->>+M: 이 브랜드의 정보와 상품 목록을 보여주세요.
   

    M->>+D: 1. 선택된 브랜드의 상세 정보를 조회합니다.
  

    D-->>-M: 브랜드 상세 정보 반환

    M->>+D: 2. 이 브랜드에 속한 상품들의 <br> 목록을 조회합니다.
    

    D-->>-M: 해당 브랜드의 상품 목록 반환

    M-->>-S: 화면에 표시할 브랜드 정보와 <br> 상품 목록을 함께 전달
   

    S-->>-U: 브랜드 정보와 해당 상품 목록을 <br> 화면에 표시
   
    
```

## 좋아요 

### 좋아요 등록
 
```mermaid
sequenceDiagram
    participant U as 사용자 (User)
    participant S as 상품 화면 (UI)
    participant M as 좋아요 관리자 (Manager)
    participant D as 데이터 저장소 (Database)

    

    U->>+S: '좋아요' 하기 (비활성 버튼 클릭)

    S->>+M: '좋아요' 등록을 요청합니다.
    

    M->>+D: '좋아요' 정보를 저장합니다.

    D-->>-M: 저장 성공

    M-->>-S: 등록 처리 성공 응답

    S-->>-U: '좋아요' 버튼을 활성화 상태로 변경
   
```

### 좋아요 취소 

```mermaid
sequenceDiagram
    participant U as 사용자 (User)
    participant S as 상품 화면 (UI)
    participant M as 좋아요 관리자 (Manager)
    participant D as 데이터 저장소 (Database)


    U->>+S: '좋아요' 취소하기 (활성 버튼 클릭)

    S->>+M: '좋아요' 취소를 요청합니다.
    

    M->>+D: '좋아요' 정보를 삭제합니다.

    D-->>-M: 삭제 성공

    M-->>-S: 취소 처리 성공 응답

    S-->>-U: '좋아요' 버튼을 비활성화 상태로 변경
    
```

### 좋아요 목록

```mermaid
sequenceDiagram
    participant U as 사용자 (User)
    participant S as 좋아요 목록 화면 (UI)
    participant M as 상품 관리자 (Manager)
    participant D as 데이터 저장소 (Database)

    U->>S: '좋아요 목록' 페이지 방문

    S->>+M: 내가 '좋아요'한 상품 목록 요청

    M->>+D: 이 사용자가 '좋아요'한 상품들 조회

    D-->>-M: '좋아요'한 상품 정보 목록 반환

    M-->>-S: 화면에 표시할 상품 정보 전달

    S-->>U: '좋아요'한 상품 목록을 화면에 표시

```

## 주문 

### 주문 생성

시나리오 1 : 주문 성공 

```mermaid
sequenceDiagram
    participant U as 사용자
    participant O as 주문 서비스
    participant P as 포인트 서비스
    participant B as 메시지 브로커
    participant S as 재고 서비스

    U->>O: 상품 주문 요청
    O->>O: 주문 데이터 생성 (상태: PENDING)
    O->>P: 포인트 차감 요청
    P->>P: 사용자 포인트 확인
    Note right of P: 포인트 충분함!
    P->>P: 사용자 포인트 차감
    P->>B: "포인트 차감 완료(PointsDeducted)" 이벤트 발행

    B-->>S: "포인트 차감 완료" 이벤트 전달 (비동기)
    S->>S: 이벤트 수신 및 재고 차감 시도
    S->>S: 실제 재고 차감 성공
    S->>B: "재고 차감 성공(StockDecreased)" 이벤트 발행

    B-->>O: "재고 차감 성공" 이벤트 전달 (비동기)
    O->>O: 주문 상태를 "완료(COMPLETED)"로 변경
    O->>U: "주문이 성공적으로 완료되었습니다" 알림
```

시나리오 2 : 재고 부족으로 인한 보상

포인트 차감 완료 이벤트 발행까지는 이전과 동일

```mermaid
sequenceDiagram
    participant U as 사용자
    participant O as 주문 서비스
    participant P as 포인트 서비스
    participant B as 메시지 브로커
    participant S as 재고 서비스

    
    P->>B: "포인트 차감 완료(PointsDeducted)" 이벤트 발행

    B-->>S: "포인트 차감 완료" 이벤트 전달 (비동기)
    S->>S: 이벤트 수신 및 재고 차감 시도
    Note right of S: 재고가 부족하다.
    S->>B: "재고 차감 실패(StockDecreaseFailed)" 이벤트 발행

    B-->>O: "재고 차감 실패" 이벤트 전달 (비동기)
    O->>O: 주문 상태를 "실패(FAILED)"로 변경
    O->>U: "죄송합니다. 주문하신 상품의 재고가 부족합니다." 알림

    B-->>P: "재고 차감 실패" 이벤트 전달 (비동기)
    P->>P: 이벤트 수신 및 보상 트랜잭션 시작
    P->>P: 이전에 차감했던 포인트를 다시 '복구(충전)' 처리
    Note right of P: 이전에 성공했던 포인트 차감을 <br> 다시 원상복구 시킨다. (보상)
    P->>B: "포인트 복구 완료(PointsRestored)" 이벤트 발행
```

### 주문 목록

```mermaid
sequenceDiagram
    participant U as 사용자
    participant S as 주문 내역 화면 (UI)
    participant O as 주문 서비스 (Order Service)

    U->>S: '주문 내역' 페이지 방문

    S->>+O: 나의 모든 주문 내역을 조회합니다.
   

    O->>O: 데이터베이스에서 해당 사용자의 <br> 모든 주문 목록을 최신순으로 조회

    O-->>-S: 화면에 표시할 주문 목록 데이터 전달

    S-->>U: 주문 목록을 화면에 표시
```


### 주문 상세

```mermaid
sequenceDiagram
    participant U as 사용자
    participant S as 주문 상세 화면 (UI)
    participant O as 주문 서비스 (Order Service)

    U->>S: 주문 내역 목록에서 특정 주문 클릭

    S->>+O: 이 주문의 상세 내역을 조회합니다.
    
    O->>O: 데이터베이스에서 선택된 주문의 <br> 상세 정보를 조회
   
    O-->>-S: 화면에 표시할 주문 상세 데이터 전달

    S-->>U: 선택한 주문의 상세 내역을 화면에 표시
```