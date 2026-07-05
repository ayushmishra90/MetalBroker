package com.hugoserve.metalbroker.security.impl;

import static com.hugoserve.metalbroker.utils.ResponseErrorBuilder.error;
import static com.hugoserve.metalbroker.utils.constants.ErrorCodes.AUTH_INVALID;

import com.google.protobuf.util.Timestamps;
import com.hugoserve.metalbroker.domain.db.UserDB;
import com.hugoserve.metalbroker.facade.AuthFacade;
import com.hugoserve.metalbroker.proto.MetalRatesProto.*;
import com.hugoserve.metalbroker.security.AuthService;
import com.hugoserve.metalbroker.security.JwtService;
import com.hugoserve.metalbroker.security.SecurityUser;
import com.hugoserve.metalbroker.utils.DbProtoMapper;
import com.hugoserve.metalbroker.utils.ProtoJson;
import com.hugoserve.metalbroker.utils.ProtoJsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthFacade authFacade;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ===================== REGISTER =====================
    @Override
    public String register(String body) {
        RegisterRequest req = DbProtoMapper.parseRegister(body);
        String email = req.getEmail().trim();
        String password = req.getPassword();

        if (email.isEmpty())
            return error(AUTH_INVALID,"Email must not be empty");

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"))
            return error(AUTH_INVALID,"Invalid email format");

        if (password.length() < 6)
            return error(AUTH_INVALID,"Password must be at least 6 chars");

        if (authFacade.findByEmail(email) != null)
            return error(AUTH_INVALID,"Email already exists");

        long userId = authFacade.createUser(
                email,
                passwordEncoder.encode(password),
                UserRole.USER.name()
        );

        UserDB user = authFacade.findByEmail(email);
        SecurityUser securityUser = new SecurityUser(user);

        String access = jwtService.generateAccessToken(securityUser);
        String refresh = jwtService.generateRefreshToken(securityUser);

        authFacade.updateRefreshToken(userId, refresh);

        return ok(success(user, access, refresh, "User Registered"));
    }

    // ===================== LOGIN =====================
    @Override
    public String login(String body) {
        LoginRequest req =
                ProtoJsonParser.parse(body, LoginRequest.newBuilder()).build();

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.getEmail(),
                            req.getPassword()
                    )
            );

            SecurityUser principal = (SecurityUser) auth.getPrincipal();
            UserDB user = principal.getUser();

            String access = jwtService.generateAccessToken(principal);
            String refresh = jwtService.generateRefreshToken(principal);

            authFacade.updateRefreshToken(user.getId(), refresh);

            return ok(success(user, access, refresh, "Logged In"));

        } catch (Exception e) {
            return error(AUTH_INVALID,"Invalid email or password");
        }
    }

    // ===================== REFRESH =====================
    @Override
    public String refresh(String body) {
        RefreshTokenRequest req =
                ProtoJsonParser.parse(body, RefreshTokenRequest.newBuilder()).build();

        String token = req.getRefreshToken();
        String email;

        try {
            email = jwtService.extractUsername(token);
        } catch (Exception e) {
            return sessionExpired();
        }

        UserDB user = authFacade.findByEmail(email);

        if (user == null || !token.equals(user.getCurrentRefreshToken()))
            return sessionExpired();

        if (!jwtService.isTokenValid(token, new SecurityUser(user)))
            return sessionExpired();

        String newAccess =
                jwtService.generateAccessToken(new SecurityUser(user));

        return ok(success(user, newAccess, user.getCurrentRefreshToken(),"Token Refreshed"));
    }

    // ===================== LOGOUT =====================
    @Override
    public String logout(String body) {
        LogoutRequest req =
                ProtoJsonParser.parse(body, LogoutRequest.newBuilder()).build();

        String token = req.getRefreshToken();

        if (token == null || token.isEmpty())
            return sessionExpired();

        String email;

        try {
            email = jwtService.extractUsername(token);
        } catch (Exception e) {
            return sessionExpired();
        }

        UserDB user = authFacade.findByEmail(email);

        if (user == null || !token.equals(user.getCurrentRefreshToken()))
            return sessionExpired();

        authFacade.updateRefreshToken(user.getId(), null);

        return ok(
                AuthResponse.newBuilder()
                        .setSuccess(true)
                        .setCode("LOGGED_OUT")
                        .setMessage("Logged out successfully")
                        .build()
        );
    }

    // ===================== HELPERS =====================
    private AuthResponse success(UserDB user, String access, String refresh, String msg) {
        long now = System.currentTimeMillis();

        return AuthResponse.newBuilder()
                .setSuccess(true)
                .setRole(user.getRole())
                .setCode("AUTH_VALID")
                .setMessage(msg)
                .setTokens(
                        TokenPair.newBuilder()
                                .setAccessToken(access)
                                .setRefreshToken(refresh)
                                .setAccessExpiresAt(
                                        Timestamps.fromMillis(
                                                now + JwtService.ACCESS_EXPIRY
                                        )
                                )
                                .setRefreshExpiresAt(
                                        Timestamps.fromMillis(
                                                now + JwtService.REFRESH_EXPIRY
                                        )
                                )
                                .build()
                )
                .build();
    }

    private String ok(AuthResponse res) {
        return ProtoJson.print(res);
    }

    private String sessionExpired() {
        return error("AUTH_FAIL","Session expired, please login");
    }
}
