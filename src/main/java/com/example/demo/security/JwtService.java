package com.example.demo.security;

import com.example.demo.domain.Rol;
import com.example.demo.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(usuario.email())
                .claim("id", usuario.id())
                .claim("rol", usuario.rol().name())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(secretKey)
                .compact();
    }

    public UsuarioAutenticado extraerUsuarioAutenticado(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Number idNumber = claims.get("id", Number.class);
        String email = claims.getSubject();
        String rolStr = claims.get("rol", String.class);

        if (idNumber == null || rolStr == null) {
            throw new JwtException("Token JWT incompleto: faltan claims requeridos");
        }

        Rol rol = Rol.valueOf(rolStr);
        return new UsuarioAutenticado(idNumber.longValue(), email, rol);
    }
}

