package com.bing.demo.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局统一异常处理。
 *
 * <p>controller 不再各自 catch 兜底，未捕获异常统一路由到这里。
 *
 * <p>两个关键细节：
 * <ol>
 *   <li>先 {@code response.reset()} 清掉 controller 已设置的
 *       Content-Type/Content-Disposition 头——否则 500 的 JSON 错误体仍带
 *       {@code attachment} 头，浏览器会把它当附件下载而不是显示错误。</li>
 *   <li>响应已提交（如下载中途 IO 失败、客户端断连）时状态码无法改写，
 *       返回 {@code null} 表示"已处理"，只记录日志，避免向已提交的流写错误体污染响应。</li>
 * </ol>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e,
            HttpServletResponse response) {
        log.error("Unhandled exception", e);

        if (response.isCommitted()) {
            return null;
        }

        response.reset();
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", "Internal Server Error");
        body.put("message", String.valueOf(e.getMessage()));
        return ResponseEntity.internalServerError().body(body);
    }
}
