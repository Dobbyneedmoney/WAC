# WAC: Web AI Consulting

**AI 기반 화상 모의면접 플랫폼**

> WebRTC와 AI(GPT, Whisper)를 활용하여 실전처럼 연습하고 정량적인 피드백을 받을 수 있는 화상 면접 연습 플랫폼입니다.

---

##  프로젝트 개요

- **프로젝트명**: WAC (Web AI Consulting)
- **개발기간**: 2025.07.01 ~ 2025.07.28
- **팀명**: 2조
- **주요기능**: 실시간 화상 면접 / AI 기반 음성 및 텍스트 피드백 / 면접 예약 및 결제 시스템 / 마이페이지 / 관리자 페이지

> 비대면 시대에 맞춘 실전형 면접 연습 환경 제공  
> GPT + Whisper + WebRTC 기반의 AI 피드백 및 화상면접 통합 시스템

---

##  기술 스택

### Backend
- Java 17 / Spring Boot 3 / Spring Security / JPA
- MySQL / REST API / WebSocket

### Frontend
- HTML5 / CSS3 / JavaScript / JSP / AJAX / Thymeleaf

### AI & Real-time
- OpenAI GPT / Whisper (음성 인식)
- WebRTC (화상 통신)
- PortOne (결제, 카카오페이)

### DevOps & Infra
- AWS EC2 / RDS(MySQL) / Route53
- GitHub + GitHub Flow
- OS: Windows & macOS (혼용)

---

## 주요 기능 요약

| 기능 구분 | 설명 |
|-----------|------|
| 회원가입/로그인 | 로컬 + 소셜(Kakao) 로그인, 사용자 유형별 권한 분리 |
| 마이페이지 | 예약 내역, 결제, 포인트, 피드백, 회원 정보 수정 |
| 화상면접 | WebRTC 기반 실시간 면접 / 녹화 / 개별 볼륨 조절 / 채팅 |
| AI 면접 | Whisper로 음성 분석 → GPT가 실시간 피드백 |
| 자소서/이력서 | AI 분석 및 문장력·논리성 기반 피드백 |
| 면접 예약 | 면접관과 일정 선택, 중복 차단, 포인트 차감 |
| 결제 시스템 | 카카오페이 결제, 포인트 환전 및 환불 처리 |
| 관리자 페이지 | 회원/예약/결제/환전 관리 및 통계 시각화 |
| 커뮤니티 | 게시판, 리뷰, 댓글 기능 포함 |

---

## 흐름도
<img width="1812" height="924" alt="image" src="https://github.com/user-attachments/assets/bd3f5efb-2e61-4bcf-932a-decab7d45b1d" />

## ERD
<img width="1546" height="1032" alt="image" src="https://github.com/user-attachments/assets/6138d45f-61ec-4041-9f5f-ca6495dd03cc" />



##  시스템 구조

- WebRTC + Janus: 화상면접
- Whisper → GPT: 음성 텍스트화 후 면접 답변 분석
- Spring Boot MVC: REST API, 인증, DB 연동
- MySQL + JPA: 사용자/예약/결제/포인트 등 관리
- PortOne: 포인트 충전 및 결제 처리
- AWS EC2: 서버 배포, RDS 연동

---

##  팀원 역할 분담

| 이름 | 역할 |
|------|------|
| **조현진** (팀장) | WebRTC/Janus, 채팅, DB설계, EC2 운영, Git 관리 |
| **김예원** | 로그인/회원가입, 마이페이지, 관리자 페이지 |
| **이슬** | 예약/결제, 후기/커뮤니티 게시판, CSS 및 레이아웃 |
| **표정현** | 기획, AI, 자소서/이력서 분석, 면접관 CRUD, 메인 UI/UX|

---

## 설치 및 실행 방법

```bash
# 프로젝트 클론
git clone https://github.com/your-repo-url.git

# IDE (STS4, IntelliJ 등)로 열기
# application.yml에 DB 및 OpenAI API 키 설정

# 빌드 및 실행
./gradlew bootRun

```
---
##  기대효과
- GPT+Whisper를 활용한 정량적 면접 피드백 제공
- WebRTC 기반 실시간 화상면접으로 실전감 향상
- AI 첨삭 기능으로 자소서/이력서 품질 향상
- 면접 복기 및 반복 학습 지원
- 비대면 면접 준비에 최적화된 올인원 플랫폼

##  활용 가능 분야
- 취업 준비생의 자기주도 모의면접 도구
- 대학 진로/취업센터의 AI 피드백 훈련 시스템
- 기업 HR팀 면접관 훈련 시스템
- 언어 훈련, 스피치 교육 등 다양한 확장 활용 가능

##  협업 문서

- [협업 가이드 문서](CONTRIBUTING.md)

##  기타 정보
- 본 프로젝트는 클라우드 데브옵스 자바 풀스택 개발자 과정 (KD) 훈련 과정 중 팀 프로젝트로 수행되었습니다.


# AI Chat & AI Voice 시각적 흐름도

## 🎯 AI Chat Flow

```mermaid
flowchart TD
    A[사용자 접속] --> B[세션 생성/확인]
    B --> C[채팅 페이지 로드]
    C --> D[사용자 메시지 입력]
    D --> E[GPT-4 API 호출]
    E --> F[AI 응답 생성]
    F --> G[대화 저장]
    G --> H[화면에 표시]
    H --> D
    
    style A fill:#e1f5fe
    style E fill:#fff3e0
    style F fill:#fff3e0
    style G fill:#f3e5f5
```

## 🎯 AI Voice Flow

```mermaid
flowchart TD
    A[음성 면접 시작] --> B[사용자 음성 녹음]
    B --> C[Whisper API<br/>음성→텍스트]
    C --> D[GPT-4 API<br/>분석]
    D --> E[상세 피드백 생성]
    E --> F[결과 저장]
    F --> G[다음 질문 제공]
    G --> B
    
    style A fill:#e1f5fe
    style C fill:#fff3e0
    style D fill:#fff3e0
    style E fill:#f3e5f5
```

## 🔄 비교 다이어그램

```mermaid
graph LR
    subgraph "AI Chat"
        A1[텍스트 입력] --> A2[GPT-4 API]
        A2 --> A3[즉시 응답]
    end
    
    subgraph "AI Voice"
        B1[음성 녹음] --> B2[Whisper API]
        B2 --> B3[텍스트 변환]
        B3 --> B4[GPT-4 API]
        B4 --> B5[상세 분석]
    end
    
    style A1 fill:#e1f5fe
    style A2 fill:#fff3e0
    style A3 fill:#f3e5f5
    style B1 fill:#e1f5fe
    style B2 fill:#fff3e0
    style B3 fill:#fff3e0
    style B4 fill:#fff3e0
    style B5 fill:#f3e5f5
```

## 🏗️ 시스템 아키텍처

```mermaid
graph TB
    subgraph "Frontend"
        A[사용자 인터페이스]
    end
    
    subgraph "Backend"
        B[AIController]
        C[AiVoiceController]
        D[AIService]
        E[AiVoiceService]
    end
    
    subgraph "External APIs"
        F[OpenAI GPT-4]
        G[OpenAI Whisper]
    end
    
    subgraph "Database"
        H[AiMessage]
        I[AiVoice]
        J[AiSession]
    end
    
    A --> B
    A --> C
    B --> D
    C --> E
    D --> F
    E --> G
    E --> F
    D --> H
    E --> I
    B --> J
    
    style A fill:#e1f5fe
    style F fill:#fff3e0
    style G fill:#fff3e0
    style H fill:#f3e5f5
    style I fill:#f3e5f5
    style J fill:#f3e5f5
```

## 📊 데이터 흐름

```mermaid
sequenceDiagram
    participant U as 사용자
    participant C as Controller
    participant S as Service
    participant API as OpenAI API
    participant DB as Database
    
    Note over U,DB: AI Chat Flow
    U->>C: 메시지 전송
    C->>S: 요청 처리
    S->>API: GPT-4 호출
    API->>S: 응답 반환
    S->>DB: 대화 저장
    S->>C: 결과 반환
    C->>U: 응답 표시
    
    Note over U,DB: AI Voice Flow
    U->>C: 음성 파일 업로드
    C->>S: 음성 처리 요청
    S->>API: Whisper API 호출
    API->>S: 텍스트 반환
    S->>API: GPT-4 분석 호출
    API->>S: 분석 결과 반환
    S->>DB: 결과 저장
    S->>C: 피드백 반환
    C->>U: 분석 결과 표시
```

## 🎨 사용자 경험 흐름

```mermaid
journey
    title AI 면접 시스템 사용자 경험
    section AI Chat
      로그인: 5: 사용자
      채팅 시작: 4: 사용자
      메시지 입력: 5: 사용자
      AI 응답: 5: 사용자
      대화 계속: 4: 사용자
    section AI Voice
      음성 면접 시작: 5: 사용자
      음성 녹음: 4: 사용자
      분석 대기: 3: 사용자
      피드백 확인: 5: 사용자
      다음 질문: 4: 사용자
```

이 다이어그램들은 GitHub, GitLab, Notion 등에서 자동으로 렌더링되어 시각적 흐름도를 보여줍니다. Mermaid 문법을 지원하는 플랫폼에서 바로 이미지로 변환됩니다! 

