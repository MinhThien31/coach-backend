package com.minhthien.web.coach.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.service.PayOSGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PayOSGatewayServiceImpl implements PayOSGatewayService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${payos.client-id:}")
    private String clientId;

    @Value("${payos.api-key:}")
    private String apiKey;

    @Value("${payos.checksum-key:}")
    private String checksumKey;

    @Value("${payos.partner-code:}")
    private String partnerCode;

    @Value("${payos.base-url:https://api-merchant.payos.vn}")
    private String baseUrl;

    @Value("${payos.return-url:}")
    private String returnUrl;

    @Value("${payos.cancel-url:}")
    private String cancelUrl;

    @Override
    public PaymentLinkData createPaymentLink(Long orderCode, Long amount, String description, String buyerName, String buyerEmail, String buyerPhone) {
        ensureConfigured();

        String finalReturnUrl = appendQueryParam(returnUrl, "orderCode", String.valueOf(orderCode));
        String finalCancelUrl = appendQueryParam(cancelUrl, "orderCode", String.valueOf(orderCode));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderCode", orderCode);
        payload.put("amount", amount);
        payload.put("description", description);
        payload.put("buyerName", sanitizeText(buyerName));
        payload.put("buyerEmail", sanitizeText(buyerEmail));
        payload.put("buyerPhone", sanitizeText(buyerPhone));
        payload.put("items", List.of(Map.of(
                "name", "Nap vi",
                "quantity", 1,
                "price", amount
        )));
        payload.put("cancelUrl", finalCancelUrl);
        payload.put("returnUrl", finalReturnUrl);
        payload.put("expiredAt", Instant.now().plusSeconds(15 * 60).getEpochSecond());
        payload.put("signature", createCreatePaymentSignature(amount, orderCode, description, finalReturnUrl, finalCancelUrl));
        payload.values().removeIf(Objects::isNull);

        JsonNode dataNode = callPayOS(HttpMethod.POST, "/v2/payment-requests", payload);
        return new PaymentLinkData(
                dataNode.path("orderCode").asLong(orderCode),
                dataNode.path("amount").asLong(amount),
                textOrNull(dataNode, "paymentLinkId"),
                textOrNull(dataNode, "checkoutUrl"),
                textOrNull(dataNode, "qrCode"),
                textOrNull(dataNode, "status")
        );
    }

    @Override
    public PaymentRequestStatusData getPaymentRequestStatus(Long orderCode) {
        ensureConfigured();
        JsonNode dataNode = callPayOS(HttpMethod.GET, "/v2/payment-requests/" + orderCode, null);
        return new PaymentRequestStatusData(
                dataNode.path("orderCode").asLong(orderCode),
                dataNode.path("amount").asLong(0L),
                dataNode.path("amountPaid").asLong(0L),
                textOrNull(dataNode, "status"),
                firstNonBlank(textOrNull(dataNode, "paymentLinkId"), textOrNull(dataNode, "id"))
        );
    }

    @Override
    public VerifiedWebhookData verifyWebhook(Map<String, Object> webhookPayload) {
        ensureConfigured();
        if (webhookPayload == null || webhookPayload.isEmpty()) {
            throw new BadRequestException("Webhook PayOS không có dữ liệu");
        }

        String signature = asString(webhookPayload.get("signature"));
        if (!StringUtils.hasText(signature)) {
            throw new BadRequestException("Webhook PayOS thiếu signature");
        }

        Map<String, Object> data = objectMapper.convertValue(webhookPayload.get("data"), new TypeReference<>() {});
        if (data == null || data.isEmpty()) {
            throw new BadRequestException("Webhook PayOS thiếu data");
        }

        String expectedSignature = createSignatureFromObject(data);
        if (!signature.equals(expectedSignature)) {
            throw new BadRequestException("Webhook PayOS signature không hợp lệ");
        }

        return new VerifiedWebhookData(
                asLong(data.get("orderCode")),
                asLong(data.get("amount")),
                asString(data.get("reference")),
                asString(data.get("paymentLinkId")),
                asString(data.getOrDefault("code", webhookPayload.get("code"))),
                asString(data.getOrDefault("desc", webhookPayload.get("desc"))),
                Boolean.TRUE.equals(webhookPayload.get("success")),
                webhookPayload
        );
    }

    private JsonNode callPayOS(HttpMethod method, String path, Object body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);
            if (StringUtils.hasText(partnerCode)) {
                headers.set("x-partner-code", partnerCode);
            }

            HttpEntity<?> entity = body == null
                    ? new HttpEntity<>(null, headers)
                    : new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + path,
                    method,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String code = root.path("code").asText();
            if (!"00".equals(code)) {
                throw new BadRequestException("PayOS trả về lỗi: " + root.path("desc").asText("Unknown error"));
            }
            return root.path("data");
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Không gọi được PayOS: " + ex.getMessage());
        }
    }

    private void ensureConfigured() {
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(apiKey) || !StringUtils.hasText(checksumKey)) {
            throw new BadRequestException("Thiếu cấu hình PayOS: payos.client-id, payos.api-key hoặc payos.checksum-key");
        }
        if (!StringUtils.hasText(returnUrl) || !StringUtils.hasText(cancelUrl)) {
            throw new BadRequestException("Thiếu cấu hình PayOS return/cancel url");
        }
    }

    private String createCreatePaymentSignature(Long amount, Long orderCode, String description, String returnUrl, String cancelUrl) {
        String data = "amount=" + amount
                + "&cancelUrl=" + emptyIfNull(cancelUrl)
                + "&description=" + emptyIfNull(description)
                + "&orderCode=" + orderCode
                + "&returnUrl=" + emptyIfNull(returnUrl);
        return hmacSha256(data, checksumKey);
    }

    private String createSignatureFromObject(Map<String, Object> data) {
        Map<String, Object> sorted = new TreeMap<>(data);
        StringBuilder builder = new StringBuilder();
        Iterator<Map.Entry<String, Object>> iterator = sorted.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            builder.append(entry.getKey()).append("=").append(convertValue(entry.getValue()));
            if (iterator.hasNext()) {
                builder.append("&");
            }
        }
        return hmacSha256(builder.toString(), checksumKey);
    }

    private String convertValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Map<?, ?> mapValue) {
            return toJson(sortNestedMap(mapValue));
        }
        if (value instanceof Collection<?> collection) {
            List<Object> normalized = new ArrayList<>();
            for (Object item : collection) {
                if (item instanceof Map<?, ?> itemMap) {
                    normalized.add(sortNestedMap(itemMap));
                } else {
                    normalized.add(item);
                }
            }
            return toJson(normalized);
        }
        return String.valueOf(value);
    }

    private Map<String, Object> sortNestedMap(Map<?, ?> source) {
        Map<String, Object> result = new TreeMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nestedMap) {
                result.put(String.valueOf(entry.getKey()), sortNestedMap(nestedMap));
            } else if (value instanceof Collection<?> collection) {
                List<Object> normalized = new ArrayList<>();
                for (Object item : collection) {
                    if (item instanceof Map<?, ?> nestedItemMap) {
                        normalized.add(sortNestedMap(nestedItemMap));
                    } else {
                        normalized.add(item);
                    }
                }
                result.put(String.valueOf(entry.getKey()), normalized);
            } else {
                result.put(String.valueOf(entry.getKey()), value);
            }
        }
        return result;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Không thể tạo chữ ký PayOS");
        }
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new BadRequestException("Không thể ký dữ liệu PayOS: " + ex.getMessage());
        }
    }

    private String appendQueryParam(String url, String key, String value) {
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String sanitizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return StringUtils.hasText(text) ? text : null;
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }
}
