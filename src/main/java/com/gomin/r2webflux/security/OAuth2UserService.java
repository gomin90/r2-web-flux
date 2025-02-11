package com.gomin.r2webflux.security;

import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OAuth2UserService extends DefaultReactiveOAuth2UserService {

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) {
        return super.loadUser(userRequest)
                .map(oauth2User -> new CustomOAuth2User(oauth2User));
    }
}

class CustomOAuth2User implements OAuth2User {
    private final OAuth2User oauth2User;

    public CustomOAuth2User(OAuth2User oauth2User) {
        this.oauth2User = oauth2User;
    }

    @Override
    public java.util.Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return oauth2User.getAttribute("login");  // Github는 'login'을 기본 식별자로 사용
    }
}
