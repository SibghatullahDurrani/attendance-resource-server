package com.main.face_recognition_resource_server.auth;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, CustomAuthentication> {
  @Override
  public CustomAuthentication convert(Jwt source) {
    List<String> rolesString = source.getClaimAsStringList("ROLES");
    List<GrantedAuthority> authorities = new ArrayList<>();
    rolesString.forEach(authority -> authorities.add(new SimpleGrantedAuthority(authority)));

    return new CustomAuthentication(source, authorities);
  }
}
