package jbell.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    
    // 실제 운영 시에는 application.yml에 저장된 문자열을 불러와서 사용하는 것을 권장합니다.
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); 

    public String createAccessToken(String userId, String role) { // role 매개변수 추가
        long now = (new Date()).getTime();
        Date validity = new Date(now + 1000 * 60 * 15);
        
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role) // 권한 정보 추가
                .setIssuedAt(new Date(now))
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String userId) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + 1000 * 60 * 60 * 24 * 7); // 7일
        
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date(now))
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 아이디 추출
    public String getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
    
	 // 토큰에서 권한 추출하는 메서드 추가
    public String getRole(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                   .parseClaimsJws(token).getBody().get("role", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 여기에 로그를 남겨서 만료인지, 잘못된 토큰인지 구분할 수 있습니다.
            return false;
        }
    }
}