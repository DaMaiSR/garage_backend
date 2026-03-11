package com.cqupt.garage.utils;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class UserContext {

    private UserContext() {
    }

    public static Long getCurrentUserId() {
        Object userId = getAttr("currentUserId");
        if (userId == null) {
            return null;
        }
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return Long.valueOf(String.valueOf(userId));
    }

    public static String getCurrentUsername() {
        Object username = getAttr("currentUsername");
        return username == null ? null : String.valueOf(username);
    }

    public static String getCurrentRole() {
        Object role = getAttr("currentRole");
        return role == null ? null : String.valueOf(role);
    }

    private static Object getAttr(String key) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getAttribute(key, RequestAttributes.SCOPE_REQUEST);
    }
}
