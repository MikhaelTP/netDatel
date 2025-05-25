package com.netdatel.adminserviceapi.security;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean supports = parameter.hasParameterAnnotation(CurrentUserId.class)
                && parameter.getParameterType().equals(Integer.class);

        // ✅ AGREGAR ESTE DEBUG
        System.out.println("🔍 RESOLVER DEBUG - supportsParameter called: " + supports);
        System.out.println("  - Has @CurrentUserId: " + parameter.hasParameterAnnotation(CurrentUserId.class));
        System.out.println("  - Is Integer type: " + parameter.getParameterType().equals(Integer.class));

        return supports;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        // ✅ AGREGAR ESTE DEBUG
        System.out.println("🔍 RESOLVER DEBUG - resolveArgument called");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("🔍 RESOLVER DEBUG - Authentication: " + authentication);
        System.out.println("🔍 RESOLVER DEBUG - Principal type: " +
                (authentication != null ? authentication.getPrincipal().getClass() : "null"));

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Integer userId = userPrincipal.getUserId().intValue();

            System.out.println("🔍 RESOLVER DEBUG - Returning userId: " + userId);
            return userId;
        }

        System.out.println("❌ RESOLVER DEBUG - Returning null");
        return null;
    }
}