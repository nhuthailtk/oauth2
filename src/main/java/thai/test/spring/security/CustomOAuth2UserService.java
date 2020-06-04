package thai.test.spring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
        if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        User user = userRepository.findByUsername(oAuth2UserInfo.getEmail());
        if(user != null) {
            updateExistingUser(user, oAuth2UserInfo);
        } else {
            registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }
        UserPrincipal userPrincipal = userService.findByUsername(oAuth2UserInfo.getEmail());
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
        return userPrincipal;
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();

        user.setUsername(oAuth2UserInfo.getEmail());
        user.setImage(oAuth2UserInfo.getImageUrl());
        user.setPassword("$2a$10$mMWdGvsPjWcCGP0e3Y7mxuCm10vsaMcuIGuf0SOvs2KwJ3QIZbawq");
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setCreatedBy(1l);
        user.setUpdatedBy(1l);
        Date date = new Date();
        user.setId(date.getTime());


        user = userRepository.save(user);
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(2l);
        userRoleRepository.save(userRole);

        return user;
    }

    private User updateExistingUser(User user, OAuth2UserInfo oAuth2UserInfo) {
        user.setPassword("$2a$10$mMWdGvsPjWcCGP0e3Y7mxuCm10vsaMcuIGuf0SOvs2KwJ3QIZbawq");
        user.setImage(oAuth2UserInfo.getImageUrl());
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setCreatedBy(1l);
        user.setUpdatedBy(1l);
        UserRole userRole = userRoleRepository.findByUserId(user.getId());
        if(userRole == null){
            userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(2l);
            userRoleRepository.save(userRole);
        }
        return userRepository.save(user);
    }

}
