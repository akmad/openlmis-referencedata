package org.openlmis.referencedata.service;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightQuery;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.util.messagekeys.SystemMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RightService {
  
  private static final String MESSAGEKEY_ERROR_UNAUTHORIZED = SystemMessageKeys.ERROR_UNAUTHORIZED;
  private static final String MESSAGEKEY_ERROR_UNAUTHORIZED_GENERIC = 
      SystemMessageKeys.ERROR_UNAUTHORIZED_GENERIC;
  
  @Autowired
  private UserRepository userRepository;

  /**
   * Check the client has the admin right specified.
   *
   * @param rightName the name of the right to check
   * @throws UnauthorizedException in case the client has got no right to access the resource
   */
  public void checkAdminRight(String rightName) {
    checkAdminRight(rightName, true);
  }

  /**
   * Check the client has the admin right specified.
   * 
   * @param rightName the name of the right to check
   * @param allowServiceTokens whether to allow service-level tokens with root access
   * @throws UnauthorizedException in case the client has got no right to access the resource
   */
  public void checkAdminRight(String rightName, boolean allowServiceTokens) {
    checkAdminRight(rightName, allowServiceTokens, null);
  }

  /**
   * Check the client has the admin right specified.
   *
   * @param rightName the name of the right to check
   * @param allowServiceTokens whether to allow service-level tokens with root access
   * @param userId id of the user that can bypass the right check e.g. to retrieve his own info
   * @throws UnauthorizedException in case the client has got no right to access the resource
   */
  public void checkAdminRight(String rightName, boolean allowServiceTokens, UUID userId) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext()
        .getAuthentication();

    if (allowServiceTokens && authentication.isClientOnly()) {
      // service-level tokens allowed and no user associated with the request
      return;
    } else if (!allowServiceTokens && authentication.isClientOnly()) {
      // service-level tokens not allowed and no user associated with the request
      throw new UnauthorizedException(new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, rightName));
    } else { // user-based client, check if user has right
      String username = ((User) authentication.getPrincipal()).getUsername();
      User user = userRepository.findOneByUsername(username);

      // bypass the right check if user id matches
      if (null != userId && userId.equals(user.getId())) {
        return;
      }

      if (user.hasRight(
          new RightQuery(Right.newRight(rightName, RightType.GENERAL_ADMIN)))) {
        return;
      }
    }

    // at this point, token is unauthorized
    throw new UnauthorizedException(new Message(MESSAGEKEY_ERROR_UNAUTHORIZED, rightName));
  }

  /**
   * Check the client is a trusted client ("root" access).
   */
  public void checkRootAccess() {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext()
        .getAuthentication();
    if (authentication.isClientOnly()) { // trusted client
      return;
    }

    // at this point, token is unauthorized
    throw new UnauthorizedException(new Message(MESSAGEKEY_ERROR_UNAUTHORIZED_GENERIC));
  }
}
