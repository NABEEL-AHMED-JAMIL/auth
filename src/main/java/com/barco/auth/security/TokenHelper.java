package com.barco.auth.security;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author Nabeel.amd
 */
@Component
@Scope("prototype")
public class TokenHelper {

    @Value("${jwt.app.name}")
    private String APP_NAME;

    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.expires_in}")
    private int EXPIRES_IN;

    @Value("${jwt.header}")
    private String AUTH_HEADER;

    private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    public TokenHelper() { }

    public String getAPP_NAME() { return APP_NAME; }
    public void setAPP_NAME(String APP_NAME) { this.APP_NAME = APP_NAME; }

    public String getSECRET() { return SECRET; }
    public void setSECRET(String SECRET) { this.SECRET = SECRET; }

    public int getEXPIRES_IN() { return EXPIRES_IN; }
    public void setEXPIRES_IN(int EXPIRES_IN) { this.EXPIRES_IN = EXPIRES_IN; }

    public String getAUTH_HEADER() { return AUTH_HEADER; }
    public void setAUTH_HEADER(String AUTH_HEADER) { this.AUTH_HEADER = AUTH_HEADER; }


    // will return the name of the user like email or username
    public String getUsernameFromToken(String token) {
        String username;
        try {
            username = this.getClaimsFromToken(token).getSubject();
        } catch (Exception ex) {
            username = null;
        }
        return username;
    }

    public String generateToken(String username) {
        return Jwts.builder().setIssuer(APP_NAME).setSubject(username).setIssuedAt(generateCurrentDate())
            .setExpiration(generateExpirationDate()).signWith(SIGNATURE_ALGORITHM, SECRET).compact();
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(this.SECRET)
                .parseClaimsJws(token).getBody();
        } catch (Exception ex) {
            claims = null;
        }
        return claims;
    }

    public String getToken( HttpServletRequest request ) {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith("Bearer ")) { return authHeader.substring(7); }
        return null;
    }

    private long getCurrentTimeMillis() { return DateTime.now().getMillis(); }

    private Date generateCurrentDate() { return new Date(getCurrentTimeMillis()); }

    private Date generateExpirationDate() { return new Date(getCurrentTimeMillis() + this.EXPIRES_IN * 1000); }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
