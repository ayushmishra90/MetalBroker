package com.hugoserve.metalbroker.facade;

import com.hugoserve.metalbroker.domain.dao.UserDAO;
import com.hugoserve.metalbroker.domain.db.UserDB;
import com.hugoserve.metalbroker.service.proto.auth.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final UserDAO userDao;

    public UserDB findByEmail(String email) {
        return userDao.findByEmail(email).orElse(null);
    }

    public long createUser(String email, String password, String role) {
        return userDao.insert(email, password, role);
    }

    public void updateRefreshToken(long userId, String refreshToken) {
        userDao.updateRefreshToken(userId, refreshToken);
    }

    // ---------- PROTO (EXTERNAL / API SAFE) ----------
    public UserEntity findByEmailProto(String email) {
        return userDao.findByEmailProto(email).orElse(null);
    }
}
