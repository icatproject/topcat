package org.icatproject.topcat.icatclient;

import java.util.Map;
import java.util.List;

import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.InternalException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.domain.CartItem;

public interface ICATClientInterface {
    public String login(String authenticationType, Map<String, String> parameters) throws AuthenticationException, InternalException;
    public String getUserName(String icatSessionId) throws TopcatException;
    public Boolean isAdmin(String icatSessionId);
    public String getEntityName(String icatSessionId, String entityType, Long entityId) throws TopcatException;
    public List<CartItem> getCartItems(String icatSessionId, Map<String, List<Long>> entityTypeEntityIds) throws TopcatException;
    public String getFullName(String icatSessionId) throws TopcatException;
    public Boolean isSessionValid(String icatSessionId) throws TopcatException;
    public Long getRemainingMinutes(String icatSessionId) throws TopcatException;
    public void refresh(String icatSessionId) throws TopcatException;
    public void logout(String icatSessionId) throws TopcatException;
}
