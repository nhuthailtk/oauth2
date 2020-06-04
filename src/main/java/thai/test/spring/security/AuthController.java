package thai.test.spring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private TokenService verificationTokenService;

    @Autowired
    private JwtUtil jwtUtil;
    @PostMapping("/register")
    public User register(@RequestBody User user){
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setCreatedBy(1l);
        user.setUpdatedBy(1l);
        Date date = new Date();
        user.setId(date.getTime());
        return userService.createUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user){
        UserPrincipal userPrincipal = userService.findByUsername(user.getUsername());
        System.out.println(!new BCryptPasswordEncoder().matches(user.getPassword(), userPrincipal.getPassword()));
        if (null == user || !new BCryptPasswordEncoder().matches(user.getPassword(), userPrincipal.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("tài khoản hoặc mật khẩu không chính xác");
        }
        Token token = new Token();
        token.setToken(jwtUtil.generateToken(userPrincipal));
        token.setTokenExpDate(jwtUtil.generateExpirationDate());
        token.setCreatedBy(userPrincipal.getUserId());
        token.setCreatedAt(new Date());
        token.setUpdatedAt(new Date());
        token.setCreatedBy(1l);
        token.setUpdatedBy(1l);
        Date date = new Date();
        token.setId(date.getTime());
        tokenService.createToken(token);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(jwtUtil.generateRefreshToken(userPrincipal));
        refreshToken.setTokenExpDate(jwtUtil.generateRefreshExpirationDate());
        refreshToken.setCreatedBy(userPrincipal.getUserId());
        refreshToken.setCreatedAt(new Date());
        refreshToken.setUpdatedAt(new Date());
        refreshToken.setCreatedBy(1l);
        refreshToken.setUpdatedBy(1l);
      //  refreshToken.setId(date.getTime());
        tokenService.createReToken(refreshToken);
        return ResponseEntity.ok(new TokenResponsePayload(token.getToken(),refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshPayload refreshPayload){
        String userName = null;
        try {
            userName = jwtUtil.getUserIdFromRefreshToken(refreshPayload.refresh_token);
        } catch (Exception e) {
            return ResponseEntity.ok("loi mat roi");
        }
        UserPrincipal userPrincipal = userService.findByUsername(userName);
        Token token = new Token();
        token.setToken(jwtUtil.generateToken(userPrincipal));
        token.setTokenExpDate(jwtUtil.generateExpirationDate());
        token.setCreatedBy(userPrincipal.getUserId());
        token.setCreatedAt(new Date());
        token.setUpdatedAt(new Date());
        token.setCreatedBy(1l);
        token.setUpdatedBy(1l);
        Date date = new Date();
        token.setId(date.getTime());
        tokenService.createToken(token);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(jwtUtil.generateRefreshToken(userPrincipal));
        refreshToken.setTokenExpDate(jwtUtil.generateRefreshExpirationDate());
        refreshToken.setCreatedBy(userPrincipal.getUserId());
        refreshToken.setCreatedAt(new Date());
        refreshToken.setUpdatedAt(new Date());
        refreshToken.setCreatedBy(1l);
        refreshToken.setUpdatedBy(1l);
        //refreshToken.setId(date.getTime());
        tokenService.createReToken(refreshToken);
        return ResponseEntity.ok(new TokenResponsePayload(token.getToken(),refreshToken.getToken()));

    }

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        jwt = jwt.substring(7);
        UserPrincipal user = null;
        try {
            user = jwtUtil.getUserFromToken(jwt);
        } catch (Exception e) {
            return ResponseEntity.ok("loi mat roi");
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/hello")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity hello(){
        return ResponseEntity.ok("hello");
    }
}