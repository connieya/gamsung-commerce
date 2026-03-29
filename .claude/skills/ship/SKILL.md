---
name: ship
description: >
  변경사항을 커밋·푸시하고 PR을 생성한 뒤 코드 리뷰까지 자동 수행한다.
  구현이 완료됐을 때 "/ship" 또는 "ship 해줘"로 호출한다.
---

# Ship 워크플로우

## 순서

### 1. 상태 파악 (병렬 실행)
```
git status
git diff
git log --oneline -5
```
변경 파일과 diff를 모두 확인한 뒤 다음 단계로 이동한다.

### 2. 커밋
- **커밋 메시지 규칙** (CLAUDE.md 준수)
  - 형식: `<type>(<scope>): <한글 subject>`
  - type/scope는 영문 소문자, subject와 본문은 반드시 한글
  - 예: `feat(sku): SKU 도메인 및 재고 차감 API 추가`
- 관련 파일만 `git add`한다 (.env, 빌드 산출물 제외)
- `git commit`으로 커밋한다

### 3. 푸시
```
git push -u origin <현재 브랜치명>
```

### 4. PR 생성
- 반드시 아래 명령 형식 사용:
  ```
  gh pr create --repo connieya/gamsung-commerce --base main \
    --title "<type>(<scope>): <한글 제목>" \
    --body "..."
  ```
- PR 본문 구조:
  ```
  ## Summary
  - <변경사항 bullet 1>
  - <변경사항 bullet 2>

  ## Test plan
  - [ ] <테스트 항목 1>
  - [ ] <테스트 항목 2>

  Closes #<이슈번호>   ← 연관 이슈가 있을 경우만 포함

  🤖 Generated with [Claude Code](https://claude.com/claude-code)
  ```
- PR URL을 사용자에게 출력한다

### 5. 코드 리뷰
PR 생성 시 GitHub Actions(`pr-review.yml`)가 자동으로 코드 리뷰를 수행한다.
- `claude-code-action` 기반으로 PR diff 분석 후 Files Changed 탭에 인라인 코멘트 게시
- 별도 에이전트 실행 불필요 — PR URL을 사용자에게 안내하고 종료

## 주의사항
- main 브랜치에서 직접 커밋·푸시 금지 — 반드시 feature 브랜치에서 실행
- PR merge 방식은 **Squash Merge** (리뷰어에게 안내)
- `CLAUDE.md`의 README.md 자동 동기화 규칙 트리거 여부를 커밋 전에 확인
