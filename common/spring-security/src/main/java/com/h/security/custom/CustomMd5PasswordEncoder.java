package com.h.security.custom;

import com.h.common.utils.Md5Utils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomMd5PasswordEncoder implements PasswordEncoder {
    public String encode(CharSequence rawPassword) {
        return Md5Utils.encrypt(rawPassword.toString());
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encodedPassword.equals(Md5Utils.encrypt(rawPassword.toString()));
    }
}
