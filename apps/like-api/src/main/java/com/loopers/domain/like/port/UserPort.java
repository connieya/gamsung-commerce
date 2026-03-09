package com.loopers.domain.like.port;

public interface UserPort {

    UserInfo getUser(String userId);

    record UserInfo(Long id, String userId, String email) {}
}
