package org.icatproject.topcat.icatclient;

import java.util.Map;

import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.InternalException;
import org.icatproject.topcat.exceptions.TopcatException;

public interface ICATClientInterface {
    public String login(String authenticationType, Map<String, String> parameters) throws AuthenticationException, InternalException;
    public String getUserName(String icatSessionId) throws TopcatException;
    public String getFullName(String icatSessionId) throws TopcatException;
    public Boolean isSessionValid(String icatSessionId) throws TopcatException;
    public Long getRemainingMinutes(String icatSessionId) throws TopcatException;
    public void refresh(String icatSessionId) throws TopcatException;
    public void logout(String icatSessionId) throws TopcatException;
}
