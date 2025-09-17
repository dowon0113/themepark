# 🎡 Fantasy Factory: 통합 테마파크 서비스

## 프로젝트 소개
테마파크 이용객들이 웹을 통해 놀이기구 대기줄을 예약하고 입장권을 구매하며,<br>
해시태그를 활용한 콘텐츠 추천 등의 다양한 기능을 편리하게 이용할 수 있도록 구성된 통합 웹 애플리케이션입니다.

## 목표

**MSA 설계**

- 대규모 시스템 개발을 위해 핵심 도메인별로 분리하여 설계했습니다.

**모니터링도구 사용**

- Prometheus를 사용하여 서비스의 메트릭을 수집하고 모니터링할 수 있도록 설정했습니다.
- Grafana를 연동하여 Prometheus에서 수집한 데이터를 시각화하고, 실시간으로 서비스 상태를 모니터링할 수 있는 대시보드를 생성했습니다.
- Grafana에서 CPU 사용량이 **50% 이상**일 때 자동으로 Slack에 알림이 전송되도록 설정했습니다.

**대규모 트래픽 대응**

- Jmeter를 사용하여 대규모 트래픽 시에도 원활하게 실행되는지 부하테스트를 진행했습니다.
- 트래픽이 몰릴경우 동기화문제가 발생하는것을 해결하기위해 Redis 의 메모리 캐싱기법 사용 및 분산락 도입했습니다.

**Swagger API 문서화**

- 작성한 API를 문서화하여 확인이 용이하도록 했습니다.

<br><br>


## 팀원 소개

| [김도원](https://github.com/dowon0113) | [문고은](https://github.com/moongzz) | [신다은](https://github.com/devdaeun) | [김대중](https://github.com/djmachine) |
| --- | --- | --- | --- |
| <a href="https://github.com/dowon0113"><img height="150px" width="130px" src="https://github.com/user-attachments/assets/6d3e3b18-3e88-4f19-889b-2ec0ce2cfcb1"/></a> | <a href="https://github.com/moongzz"><img height="150px" width="130px" src="https://github.com/user-attachments/assets/14b98138-b68b-44e7-85b2-1b8ac2640e81"/></a> | <a href="https://github.com/devdaeun"><img height="150px" width="130px" src="https://github.com/user-attachments/assets/4b8ef41c-ebfe-4e3b-81ff-e908e1f46b28"/></a> | <a href="https://github.com/djmachine"><img height="150px" width="130px" src="https://github.com/user-attachments/assets/7ef09772-7d4d-46f9-b657-6d3e0bdc2430"/></a> | 
| 상품, 티켓팅 | 인증/인가, 유저, 슬랙 | 테마파크, 대기열, 해시태그 | 주문, 결제 | 

<br><br>

## 프로젝트 일정 및 진행

### 4.3 ~ 4.4
- 주제 및 목표 선정
- 기능 정리
- API 명세서, 인프라 설계도, 테이블 명세서, ERD, 애그리거트 구조도 작성

### 4.7 ~ 4.17
- MVP 개발

### 4.18 ~ 4.21
- 통합 테스트
- 트러블 슈팅

### 4.22 ~ 4.25
- 1차 성능 테스트
- 고도화 (Redis, Kafka 적용)
- 2차 성능 테스트

### 4.26 ~ 4.29
- 리팩톤

### 4.30
- 2차 통합 테스트


<br><br>

## 개발 환경

본 프로젝트는 Java와 Spring Boot 기반으로 개발되었으며, MSA 구조에서 각 서비스 간 유기적인 연동을 위해 Eureka 기반의 서비스 레지스트리와 FeignClient를 활용한 REST 통신을 v1에 구성했습니다. 서비스 확장성을 고려하여 v2에는 Redis 캐싱을 도입하였으며, v3에는 Kafka 기반 비동기 메세지 처리를 구현하였습니다. 인증과 인가는 Spring Security와 JWT를 통해 stateless 기반의 보안 처리를 구현했으며, 데이터 처리에는 Spring Data JPA와 QueryDSL을 적용하여 복잡한 조건의 데이터 조회도 유연하게 처리할 수 있도록 했습니다.
각 도메인 별 데이터 독립성을 고려하여 서비스별 DB 분리를 지향했으나, 학습 환경의 제약으로 인해 하나의 데이터베이스 내에서 스키마를 분리하는 방식으로 논리적인 격리를 구현하였습니다.

로컬에서도 전체 MSA 환경을 손쉽게 구동할 수 있도록 Docker와 Docker Compose를 활용하여 서비스 실행 환경을 통합 구성했습니다. 이를 통해 개발자 간 환경 차이를 줄이고, 테스트 및 배포 전 과정을 효율적으로 수행할 수 있도록 했습니다.

### Stacks
<div>
<img src="https://github.com/user-attachments/assets/30dd8d49-88e3-4e47-9526-532bcd2fa3af" width="700">
</div>

<br><br>

## 프로젝트 실행 방법
### 1. 로컬 실행
```
# 1. Git 저장소 클론 및 이동

# 2. 의존성 설치 및 빌드
./gradlew clean build -x test

# 3. 서비스 실행 (순서대로 실행)
# Eureka (서비스 디스커버리)
cd eureka
java -jar build/libs/eureka.jar

# 4. Gateway 실행
cd ../gateway
java -jar build/libs/gateway.jar

# 5. 나머지 서비스 실행 (예: auth, user 등)
cd ../auth
java -jar build/libs/auth.jar
# ... (필요한 다른 서비스도 동일하게 실행)

```

### 2. Docker 실행
```
# 1. Git 저장소 클론 및 이동
git clone https://github.com/numberOnethemepark/themepark
cd themepark

# 2. 환경 변수 설정 (.env 파일 생성)
# 로컬 실행과 동일

# 3. 배포 스크립트 실행 또는 Docker Compose 실행
# 배포 스크립트 사용 시:
chmod +x deploy.sh
./deploy.sh

# 또는, Docker Compose로 직접 실행
docker-compose up -d

# 4. 실행 상태 확인 (예: Eureka는 http://localhost:8761, Gateway는 http://localhost:8080)
docker ps

```
<br><br>


## 트러블 슈팅
### [입장권 티켓팅 시 발생하는 동시성 문제 해결하기](https://velog.io/@dowonii_dev/%EC%9E%85%EC%9E%A5%EA%B6%8C-%ED%8B%B0%EC%BC%93%ED%8C%85-%EC%8B%9C-%EB%B0%9C%EC%83%9D%ED%95%98%EB%8A%94-%EB%8F%99%EC%8B%9C%EC%84%B1-%EB%AC%B8%EC%A0%9C-%ED%95%B4%EA%B2%B0%ED%95%98%EA%B8%B0)
### [선착순 구매 기능 성능 개선하기 - 비동기 전환](https://velog.io/@dowonii_dev/%EC%84%A0%EC%B0%A9%EC%88%9C-%EA%B5%AC%EB%A7%A4-%EA%B8%B0%EB%8A%A5-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0%ED%95%98%EA%B8%B0)
<br><br>

## 주요 기능
▶ 상품 서비스
- 상품 CRUD 구현 (=입장권)
    - QueryDSL의 Predicate와 Spring Data JPA Pageable을 활용해 동적 조건 검색 및 페이징 처리

▶ 재고 관리 (=이벤트 상품 제한 수량)
- 재고 조회 기능 구현
    - 캐싱된 데이터가 있을 경우 빠르게 조회
- 재고 감소/복구 기능 구현
    - 주문서비스로부터 RabbitMQ 비동기 메시지를 받아 티켓 수량의 감소 및 복구를 처리
    - 선착순 이벤트 상품이 품절될 경우 Slack 메시지를 발송해 관리자에게 품절 상태를 알림

▶ 티켓팅 구현 
    - Redisson의 분산 락을 통해 동시성 제어
    - Dead Letter Topic을 발행하여 정상적으로 처리되지 않은 메세지를 따로 보관하여 유실되지 않도록 관리

▶ 모니터링
- Prometheus & Grafana를 활용한 모니터링

## 개발 산출물
<details>
<summary>API 구현</summary>
<div>
<img src="https://github.com/user-attachments/assets/f6a6ab2e-7516-4d92-b0f3-54415a178a69" width="500">
</div>
</details>
<details>
<summary>인프라 설계도</summary>
<div>
<img src="https://github.com/user-attachments/assets/1a6bdf00-b0ac-48a0-a6ef-f07bba1099f9" width="500">
</div>
</details>
<details>
<summary>ERD</summary>
        <img src= "https://github.com/user-attachments/assets/47669b57-f89e-4539-a625-9eee562b076e" width="500">
</details>

<details>
<summary>Flow Chart</summary>
        <img src= "https://github.com/user-attachments/assets/51bfd0df-0e47-4bef-a900-6d7d8dbd867c" width="500">
        <img src= "https://github.com/user-attachments/assets/d2c71a53-2a0f-4b08-a223-8a3d44bfb497" width="500">
        <img src= "https://github.com/user-attachments/assets/b6500d74-f379-4b91-980e-3990fa7c4869" width="500">
        <img src= "https://github.com/user-attachments/assets/0a81e5a7-0d33-4ba0-a20d-c1dc16db7d21" width="500">
        <img src= "https://github.com/user-attachments/assets/2bebbcdd-4f65-45b1-ad00-9eff0eb45a5d" width="500">
</details>

<br><br>
