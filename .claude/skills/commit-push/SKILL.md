---
name: commit-push
description: 변경사항을 커밋하고 원격에 푸시한다
disable-model-invocation: true
---

1. `git status`로 변경된 파일을 확인한다.
2. `git diff`로 변경 내용을 분석한다.
3. 변경 내용에 맞는 커밋 메시지를 작성한다.
   - 한글로 작성
   - 접두사: feat / fix / refactor / chore / docs
   - 형식: `feat: 간결한 설명`
4. 관련 파일만 `git add`하고 커밋한다.
5. `git push origin`으로 원격에 푸시한다.
