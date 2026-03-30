package com.xxr.lingtuthinktank.aop;

import com.xxr.lingtuthinktank.annotation.LogSensitive;
import com.xxr.lingtuthinktank.model.entity.admin.SensitiveOperationLog;
import com.xxr.lingtuthinktank.model.entity.user.User;
import com.xxr.lingtuthinktank.service.admin.SensitiveOperationLogService;
import com.xxr.lingtuthinktank.service.user.UserService;
import com.xxr.lingtuthinktank.utils.NetUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 敏感操作日志切面
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    private static final int MAX_REQUEST_PARAMS_LENGTH = 2048;

    private static final int MAX_ERROR_MESSAGE_LENGTH = 512;

    @Resource
    private UserService userService;

    @Resource
    private SensitiveOperationLogService sensitiveOperationLogService;

    @Resource
    private ObjectMapper objectMapper;

    @Around("@annotation(logSensitive)")
    public Object doInterceptor(ProceedingJoinPoint point, LogSensitive logSensitive) throws Throwable {
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        User loginUser = null;
        if (request != null) {
            try {
                loginUser = userService.getLoginUserPermitNull(request);
            } catch (Exception e) {
                log.debug("获取登录用户失败，继续记录匿名日志", e);
            }
        }

        SensitiveOperationLog operationLog = new SensitiveOperationLog();
        if (loginUser != null) {
            operationLog.setUserId(loginUser.getId());
            operationLog.setUserName(loginUser.getUserName());
        }
        operationLog.setIp(request != null ? NetUtils.getIpAddress(request) : "unknown");
        operationLog.setRequestPath(request != null ? request.getRequestURI() : "N/A");
        operationLog.setHttpMethod(request != null ? request.getMethod() : "N/A");
        operationLog.setDescription(logSensitive.description());
        operationLog.setMethodName(point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());
        operationLog.setRequestParams(buildRequestParams(point.getArgs()));

        try {
            Object result = point.proceed();
            operationLog.setStatus("SUCCESS");
            return result;
        } catch (Throwable e) {
            operationLog.setStatus("FAIL");
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                errorMessage = e.getClass().getSimpleName();
            }
            operationLog.setErrorMessage(truncate(errorMessage, MAX_ERROR_MESSAGE_LENGTH));
            throw e;
        } finally {
            operationLog.setDurationMs(System.currentTimeMillis() - startTime);
            persistLog(operationLog);
        }
    }

    private void persistLog(SensitiveOperationLog operationLog) {
        try {
            boolean saved = sensitiveOperationLogService.save(operationLog);
            if (!saved) {
                log.error("敏感操作日志保存失败: {}", operationLog.getDescription());
            }
        } catch (Exception e) {
            log.error("敏感操作日志落库异常", e);
        }
    }

    private String buildRequestParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        List<Object> sanitizedArgs = new ArrayList<>();
        for (Object arg : args) {
            sanitizedArgs.add(sanitizeArg(arg));
        }
        try {
            return truncate(objectMapper.writeValueAsString(sanitizedArgs), MAX_REQUEST_PARAMS_LENGTH);
        } catch (Exception e) {
            return truncate(Arrays.toString(sanitizedArgs.toArray()), MAX_REQUEST_PARAMS_LENGTH);
        }
    }

    private Object sanitizeArg(Object arg) {
        if (arg == null) {
            return null;
        }
        if (arg instanceof HttpServletRequest) {
            return "[HttpServletRequest]";
        }
        if (arg instanceof HttpServletResponse) {
            return "[HttpServletResponse]";
        }
        if (arg instanceof MultipartFile) {
            MultipartFile multipartFile = (MultipartFile) arg;
            return "[MultipartFile:" + multipartFile.getOriginalFilename() + "]";
        }
        if (arg instanceof MultipartFile[]) {
            MultipartFile[] files = (MultipartFile[]) arg;
            List<String> fileNames = new ArrayList<>();
            for (MultipartFile file : files) {
                fileNames.add(file == null ? "null" : file.getOriginalFilename());
            }
            return fileNames;
        }
        if (arg instanceof byte[]) {
            return "[byte[]:" + ((byte[]) arg).length + "]";
        }
        return arg;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}
