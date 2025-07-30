package co.ke.tucode.logs.payloads;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import co.ke.tucode.logs.services.UserActivityLogService;

@Aspect
@Component
@RequiredArgsConstructor
public class ActivityLoggingAspect {

    private final UserActivityLogService logService;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object logUserActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        String username = "anonymous";
        try {
            username = SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception ignored) {
        }

        String module = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String activity = joinPoint.getSignature().getName().toLowerCase();

        if (activity.startsWith("get") || activity.startsWith("view") || activity.startsWith("login")) {
            return joinPoint.proceed(); // not a write action
        }

        Object result = joinPoint.proceed();

        // Unwrap ResponseEntity body if applicable
        Object unwrapped = result;
        if (result instanceof ResponseEntity) {
            unwrapped = ((ResponseEntity<?>) result).getBody();
        }

        String entityType = null;
        Long entityId = null;

        if (unwrapped != null) {
            try {
                Method getId = unwrapped.getClass().getMethod("getId");
                Object idValue = getId.invoke(unwrapped);
                if (idValue instanceof Long) {
                    entityId = (Long) idValue;
                } else if (idValue instanceof String && ((String) idValue).matches("\\d+")) {
                    entityId = Long.valueOf((String) idValue);
                }
                entityType = unwrapped.getClass().getSimpleName();
            } catch (Exception ignored) {
            }
        }

        if (!username.equalsIgnoreCase("Jakida@tucode.co.ke")) {
            logService.log(username, module, activity, "Executed successfully", entityType, entityId);
        }

        return result;
    }
}