package com.vignesh.springsecuritypg.security;

import com.vignesh.springsecuritypg.model.Authorities;
import com.vignesh.springsecuritypg.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtTokenService {

    private JwtParser jwtParser;

    private Key key;

    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("#{${security.jwt.valid-for: 30} * 1000}")
    private long validFor;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @PostConstruct
    protected void init() throws NoSuchAlgorithmException {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        key = new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
        jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    public String getToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    public boolean isValidToken(String token) {
        try {
            jwtParser.parseClaimsJws(token);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            log.error("Invalid jwt token : {}", e.getMessage());
            return false;
        }
        return true;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();
        List<Authorities> authorities = ((List<String>) claims.get("roles", List.class)).stream().map(r -> Authorities.valueOf(r)).collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
    }

    public String createToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put("roles", user.getAuthorities());
        Date now = new Date();
        Date validity = new Date(now.getTime() + validFor);
        return Jwts.builder().setExpiration(validity)
                .setIssuedAt(now)
                .setClaims(claims).signWith(key).compact();
    }

}
