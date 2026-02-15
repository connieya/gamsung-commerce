# Docker 인프라 사용법

다른 PC에서 프로젝트를 실행할 때 참고할 수 있도록, Docker Compose 기반 인프라 기동·종료·MySQL 접속 방법을 정리합니다.

---

## 1. 인프라 기동

Compose 파일이 있는 경로에서 실행합니다.

**방법 A: 프로젝트 루트에서 (권장)**

```bash
# 프로젝트 루트(gamsung-commerce)에서
docker-compose -f ./docker/infra-compose.yml up -d
```

**방법 B: docker 디렉터리로 이동 후**

```bash
cd docker
docker-compose -f infra-compose.yml up -d
```

- `-d`: 백그라운드 실행
- MySQL, Redis, Kafka, Kafka UI 등이 기동됩니다.

---

## 2. 컨테이너 확인

```bash
docker ps
```

실행 중인 컨테이너 목록과 **컨테이너 이름(NAMES)**을 확인할 수 있습니다. MySQL 접속 시 이 이름을 사용합니다.

---

## 3. 인프라 종료

**컨테이너만 종료 (볼륨·데이터 유지)**

```bash
docker-compose -f ./docker/infra-compose.yml down
```

**컨테이너 + 볼륨까지 삭제 (DB 데이터 등 모두 삭제)**

```bash
docker-compose -f ./docker/infra-compose.yml down -v
```

- `-v`: 볼륨 삭제. MySQL 데이터가 모두 지워지므로 필요할 때만 사용하세요.

---

## 4. MySQL 접속 (데이터 확인·쿼리)

### 4.1 컨테이너 이름 확인

컨테이너 이름은 실행한 디렉터리(프로젝트명)에 따라 달라집니다.

```bash
docker ps
```

예: `gamsung-commerce_mysql_1`, `docker_mysql_1` 등. 아래 예시는 `docker-mysql-1`로 적었습니다. **실제로는 `docker ps`에 나온 이름**을 사용하세요.

### 4.2 컨테이너 셸 접속 후 MySQL 실행

```bash
# 컨테이너 접속
docker exec -it docker-mysql-1 bash

# 컨테이너 안에서 MySQL 클라이언트 실행
mysql -u root -p
# 비밀번호 입력: root

# DB 선택
USE loopers;
```

### 4.3 한 줄로 MySQL 접속 (DB 지정)

```bash
docker exec -it docker-mysql-1 mysql -u root -proot loopers
```

바로 `loopers` DB가 선택된 상태로 접속됩니다.

### 4.4 접속 정보 요약

| 항목 | 값 |
|------|-----|
| 호스트 | localhost (또는 컨테이너 내부: mysql) |
| 포트 | 3306 |
| root 비밀번호 | root |
| DB 이름 (로컬·웹용) | loopers |
| DB 이름 (부하 테스트·QA용) | loopers_qa |
| 애플리케이션 계정 | application / application |

### 4.5 자주 쓰는 쿼리

```sql
-- 테이블 목록
SHOW TABLES;

-- 데이터 확인 (테이블명은 단수형)
SELECT * FROM users;
SELECT * FROM brand;
SELECT * FROM product;
SELECT * FROM product_like;
```

> **참고**: 이 프로젝트의 상품 테이블 이름은 **`product`** (단수)입니다. `products`가 아닙니다.

---

## 4.6 부하 테스트용 DB (loopers_qa)

- **loopers**: 로컬 웹 화면·시드 데이터용 (프로필 `local`, `DataLocalLoader`로 시드 삽입)
- **loopers_qa**: 부하 테스트·k6·대량 샘플 데이터용 (프로필 `qa`, 시드 자동 실행 없음)

**최초 1회 설정**

- **Docker를 막 올린 경우**: `docker/mysql-init/02-create-loopers-qa.sql`이 DB만 생성함. 테이블이 없으므로 `qa/init-schema-loopers-qa.sh`로 `loopers` 스키마를 복사하세요.
- **이미 MySQL 볼륨을 쓰고 있는 경우**: `qa/init-schema-loopers-qa.sh` 한 번 실행 (DB 생성 + 스키마 복사).

```bash
# 프로젝트 루트에서 (root 비밀번호 기본값: root)
MYSQL_ROOT_PASSWORD=root ./qa/init-schema-loopers-qa.sh
```

**부하 테스트 시**

1. `loopers_qa` 스키마 준비 (위 스크립트로 1회)
2. `qa/sample/insert-all-data.sh` 등으로 대량 데이터 삽입 (기본 DB가 `loopers_qa`로 설정됨)
3. API는 **qa 프로필**로 기동: `SPRING_PROFILES_ACTIVE=qa ./gradlew :apps:commerce-api:bootRun`
4. k6 실행: `qa/k6/scripts/` 아래 스크립트 사용

---

## 5. 다른 PC에서 프로젝트 실행 시 체크리스트

1. **Docker, Docker Compose** 설치
2. **인프라 기동**: `docker-compose -f ./docker/infra-compose.yml up -d`
3. **컨테이너 확인**: `docker ps`로 MySQL 등 정상 기동 확인
4. **애플리케이션 실행**: `./gradlew :apps:commerce-api:bootRun` (local 프로필, 시드 데이터 자동 삽입)
5. **MySQL 접속이 필요하면**: `docker ps`로 컨테이너 이름 확인 후 `docker exec -it <컨테이너이름> mysql -u root -proot loopers`

---

## 6. 관련 파일

- Compose 파일: `docker/infra-compose.yml`
- 모니터링(선택): `docker-compose -f ./docker/monitoring-compose.yml up` → Grafana http://localhost:3000 (admin/admin)
