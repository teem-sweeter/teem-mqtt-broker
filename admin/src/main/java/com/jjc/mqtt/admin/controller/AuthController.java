package com.jjc.mqtt.admin.controller;

import com.jjc.mqtt.MoquetteProperties;
import com.jjc.mqtt.admin.security.JwtUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "鉴权", description = "系统鉴权相关")
@RestController
@RequestMapping("/v1")
public class AuthController {

    private final MoquetteProperties moquetteProperties;
    private final JwtUtils jwtUtils;

    public AuthController(MoquetteProperties moquetteProperties, JwtUtils jwtUtils) {
        this.moquetteProperties = moquetteProperties;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> check() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("initialSetup", false);
        result.put("msg", "ok");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> result = new HashMap<>();
        // 简单的登录验证
        if (moquetteProperties.getUsername().equals(request.getUsername()) && moquetteProperties.getPassword().equals(request.getPassword())) {
            result.put("code", 200);
            result.put("token", jwtUtils.generateToken(request.getUsername()));
            result.put("msg", "登录成功");
        } else {
            result.put("code", 401);
            result.put("msg", "用户名或密码错误");
        }
        return ResponseEntity.ok(result);
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
