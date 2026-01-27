# 브랜치 전략 - Git Flow

**Git Flow**는 Vincent Driessen이 제안한 브랜칭 모델로, 복잡한 구조를 통해 대규모 프로젝트에서 명확하고 체계적인 개발 프로세스를 제공합니다.

> 반드시 Merge Request 올리기

### [**브랜치 구조**](https://dev-miyeon.tistory.com/7#%EB%B8%8C%EB%9E%9C%EC%B9%98%20%EA%B5%AC%EC%A1%B0-1)

- **master**: 프로덕션 코드가 위치하는 브랜치로, 최종 릴리즈된 안정된 버전만 포함
- **develop**: 개발 중인 코드를 통합하는 브랜치. 모든 새로운 기능이 여기에서 시작
- **feat**: 특정 기능을 개발하기 위한 임시 브랜치로, 작업이 완료되면 develop 브랜치로 병합
- **fix**: 기능 수정을 위한 임시 브랜치로, 작업이 완료되면 develop 브랜치로 병합
- **deploy**: 릴리즈 준비를 위한 브랜치로, master에서 분기하여 배포 테스트 진행

# 커밋 컨벤션

https://github.com/2kjin/DeployZ/blob/master/docs/GitConvention.md

## **Branch Convention**

디렉터리 구조는 **frontend** 와 **backend** 로 구분됩니다. 각 개발자들은 해당 디렉터리로 이동 후 작업하면 됩니다.

```
master
┗━━ develop
┃		┗━━ ✨feat/<기능>
┃		┗━━ ⛏fix/<기능>
┗━━ deploy
```

## **Branch Types**

- master: 제품으로 출시될 수 있는 브랜치
- develop: 개발 브랜치 → Release마다 master에 변합
- feat/<기능>: 기능 추가 → 완성 후 develop 브랜치에 병합
- fix/<기능>: 기능 수정 → 완성 후 develop 브랜치에 병합
- deploy: 배포 브랜치 → 완성 후 master에 병합

## **Commit Convention**

- 모든 커밋은 다음의 포맷을 지켜야 한다. 이때, 본문과 footer는 생략할 수 있다.

```
SubtaskName <emoji>Type: Subject

[Body]

예)
S14P11A309-8 ✨feat: 로그인 기능 추가
- JWT 로그인 구현
```

- Type은 다음의 종류를 확인하여 작성한다.

```
(gitmoji) <타입> : <제목>

<본문(optional)>

# 타입과 제목은 필수 입력, 최대 50글자
# 본문은 세 번째 줄부터 선택 입력, 최대 72글자

# --- COMMIT END ---
# <타입> 리스트
#   💫 chore             : 프로젝트 초기 구성등 기타사항.
#   ✨ feat              : 새로운 기능
#   ⛏ fix               : 버그 수정
#   💡 refactor          : 코드 리펙토링
#   🎨 style             : 스타일 (코드 포맷팅, 세미콜론 등 비즈니스 로직에 영향 없는 변경 사항)
#   💄 design            : CSS/UI 변경 내용
#   📝 docs              : 문서 (문서 추가, 수정, 삭제)
#   ✅ test              : 테스트 (테스트 코드 추가, 수정, 삭제: 비즈니스 로직에 변경 없음)
#   ⚡ update            : 전반적인 수정사항
#   🔥 remove             : 삭제
#   🚧 in progress       : 아직 완성하지는 못했지만 진행중인 부분을 커밋해야할 경우
#   🚑 HOTFIX            : 급하게 치명적인 버그를 고쳐야 하는 경우
#   ➕ add dependency    : dependency 추가
#   ✏ typo fix          : 오탈자 수정
# ------------------
#     제목은 명령문으로(동사형태로 시작) - English only
#     제목 끝에 마침표(.) 금지
#     제목과 본문을 한 줄 띄워 분리
#     본문은 "무엇을", "왜"를 설명한다.
#     본문은 한 줄을 작성하고 . 마침표를 찍어서 분리한다.
# ------------------
# 위에 해당사항이 없는 부분은 Gitmoji를 참고하여 적절한 커밋메시지 사용할 것
```

- Subject는 무슨 작업을 수행했는지 기입합니다. 영어로 하지 않아도 괜찮습니다.
  - 영어로 작성 할 경우에는 명령문 형태로 작성하며, 첫 글자는 대문자로 작성합니다.
- Body는 왜 그 작업을 수행했는지 등의 부연 설명을 기입합니다. 영어로 하지 않아도 괜찮습니다.

## **PR Convention**

- PR 제목은 다음과 같이 작성합니다.

`subjaskcode :emoji:Type: Subject

**S14P11A309-51** ✨Feat: 회원 가입 시 유효성 검사`

# Jira Convention

## 스프린트

- 매주 월요일 아침 `8시 30분`에 팀장이 생성 (월~일)

## 데일리 스크럼

- 매일 `아침 9시`에 (라이브강의 있으면 10시) 15분 회의
- 각자 Story & Task 할당 (`Story Point 최소 6이상`)

## Epic 컨벤션

### 네이밍

- [팀이름] 큰 작업 분류 단위
  - 예
  - [공통] 기획
  - `[FE] 로그인 UI 퍼블리싱`
  - `[BE] JWT 토큰 검증 로직 구현`
  - `[AI] 데이터 전처리 스크립트 작성`
- [팀이름] 종류
  - [공통]
  - [FE]
  - [BE]
  - [AI]

## Story 컨벤션

- 노션 기능 명세서랑 맞추기
- 기능 명세서 n개 묶어서 하나의 스토리 가능
  - 예) 강의 추가 / 수정 / 삭제 ⇒ 강의 관리
- Story에는 기능 이름 넣기

### 작업상태

- **`TODO`:** 작업 시작 전
- **`Progress`:** 현재 작업 중 (동시에 1인 1Progress만 가능)
- **`Done`:** 검토 완료 및 병합 완료

### Story Point

- 1 Story Point는 `3시간` 분량
- 주마다 총 `180 Story Points` 할당
- 한 사람당 `하루 최소 6 Story Points` 이슈 할당
- Story 담당자는 `FE팀장 경수님`, `BE팀장 민재님` 고정
- Story 보고자는 실제 작업자
  - 나중에 할당 가능

## Task 컨벤션

- Task는 기능이 아니라 기술적 완성도 작업 넣기
  - 예) JWT 로그인 변환

### 작업상태

- **`TODO`:** 작업 시작 전
- **`Progress`:** 현재 작업 중 (동시에 1인 1Progress만 가능)
- **`Done`:** 검토 완료 및 병합 완료
