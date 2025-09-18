# redis 명령어

- docker exec -it redis-master redis-cli

- keys *

- TYPE rank:product:all:20250911

## ZSet

- zscore rank:product:all:20250911 1

- ZRANGE rank:product:all:20250911 0 -1 WITHSCORES

- ZREVRANGE rank:product:all:20250911 0 19 WITHSCORES
  - 상위 20개 조회

- ZRANGE rank:product:all:20250911 0 19 WITHSCORES
  - 하위 20개 조회

- DEL rank:product:all:20250911

- ZREVRANK rank:product:all:20250911 1


## Hash

- HGETALL product-like:1


- hget product:detail:1 productName 

- hmget product:detail:1 productName brandName 
