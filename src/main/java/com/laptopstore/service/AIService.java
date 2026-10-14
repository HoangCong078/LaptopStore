package com.laptopstore.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * Service tư vấn laptop qua OpenRouter (API chuẩn OpenAI chat/completions).
 * - Gửi kèm dữ liệu kho để AI không bịa sản phẩm.
 * - Nhớ lịch sử hội thoại gần nhất.
 * - Luôn trả về chuỗi, không ném exception ra UI.
 */
public class AIService {

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    // openrouter/free: router tự chọn model miễn phí đang hoạt động (id model free hay bị gỡ không báo trước)
    private static final String DEFAULT_MODEL = "openrouter/free";
    private static final int MAX_HISTORY_MESSAGES = 12;

    private static final String SYSTEM_PROMPT = """
            Bạn là trợ lý AI nội bộ hỗ trợ nhân viên bán hàng tại cửa hàng LaptopStore.
            - Bạn đang nói chuyện trực tiếp với NHÂN VIÊN BÁN HÀNG của cửa hàng (không phải khách hàng).
            - Hãy cung cấp thông tin, cấu hình, giá bán và tình trạng tồn kho dựa trên DANH SÁCH SẢN PHẨM TRONG KHO dưới đây để nhân viên tư vấn cho khách.
            - Trả lời chuyên nghiệp, ngắn gọn, cung cấp số liệu chính xác giúp nhân viên chốt đơn nhanh chóng.
            - Trả lời bằng tiếng Việt, có thể dùng **in đậm** và gạch đầu dòng "- ".
            """;

    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private final Supplier<String> inventoryProvider;
    private final List<JsonObject> history = new ArrayList<>();

    private String apiKey = "";
    private String model = DEFAULT_MODEL;

    public AIService(Supplier<String> inventoryProvider) {
        this.inventoryProvider = inventoryProvider;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        loadConfig();
    }

    public AIService() {
        this(null);
    }

    private void loadConfig() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                System.err.println("Không tìm thấy config.properties trong resources!");
                return;
            }
            Properties p = new Properties();
            p.load(in);
            this.apiKey = p.getProperty("OPENAI_API_KEY", p.getProperty("AI_API_KEY", "")).trim();
            String m = p.getProperty("OPENAI_MODEL", p.getProperty("AI_MODEL", DEFAULT_MODEL)).trim();
            this.model = m.isEmpty() ? DEFAULT_MODEL : m;
        } catch (Exception e) {
            System.err.println("Lỗi đọc cấu hình: " + e.getMessage());
        }
    }

    private JsonObject message(String role, String content) {
        JsonObject msg = new JsonObject();
        msg.addProperty("role", role);
        msg.addProperty("content", content);
        return msg;
    }

    public synchronized void clearHistory() {
        history.clear();
    }

    public synchronized String askAI(String prompt) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("PASTE_")) {
            return "Lỗi: Chưa cấu hình OPENAI_API_KEY trong file config.properties!";
        }

        try {
            // 1. Dữ liệu kho
            String inventory = "";
            if (inventoryProvider != null) {
                try {
                    inventory = inventoryProvider.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (inventory == null || inventory.isBlank()) {
                inventory = "(Kho hiện chưa có dữ liệu sản phẩm)";
            }

            // 2. Messages: system + lịch sử + câu hỏi mới
            JsonArray messages = new JsonArray();
            messages.add(message("system", SYSTEM_PROMPT + "\nDANH SÁCH SẢN PHẨM TRONG KHO:\n" + inventory));
            for (JsonObject h : history) {
                messages.add(h);
            }
            messages.add(message("user", prompt));

            JsonObject root = new JsonObject();
            root.addProperty("model", model);
            root.add("messages", messages);
            root.addProperty("temperature", 0.4);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENROUTER_URL))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "https://laptopstore.app") // tuỳ chọn
                    .header("X-Title", "LaptopStore App")              // tuỳ chọn
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(root), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();

            if (code != 200) {
                System.err.println("OpenRouter Error Code: " + code + " | Body: " + response.body());
                return switch (code) {
                    case 400 -> "Yêu cầu không hợp lệ (400). Kiểm tra lại tên model trong config.";
                    case 401 -> "API Key không hợp lệ hoặc đã bị thu hồi (401). Hãy tạo key mới trên openrouter.ai.";
                    case 402 -> "Tài khoản OpenRouter hết credit (402). Hãy dùng model miễn phí (:free) hoặc nạp thêm.";
                    case 403 -> "Không có quyền dùng model này hoặc nội dung bị chặn (403).";
                    case 404 -> "Model không tồn tại hoặc đã bị gỡ (404). Đổi OPENAI_MODEL=openrouter/free trong config.";
                    case 408 -> "Yêu cầu quá thời gian (408), vui lòng thử lại.";
                    case 429 -> "Model đang bị giới hạn lượt gọi (429). Đợi một lát rồi thử lại.";
                    case 500, 502, 503 -> "Máy chủ AI đang quá tải, vui lòng thử lại sau (" + code + ").";
                    default -> "Trợ lý AI gặp lỗi hệ thống (Mã: " + code + ").";
                };
            }

            String answer = extractText(gson.fromJson(response.body(), JsonObject.class));
            if (answer == null || answer.isBlank()) {
                return "AI không trả về nội dung. Bạn thử hỏi lại hoặc đổi model nhé.";
            }

            // 3. Lưu lịch sử khi thành công
            history.add(message("user", prompt));
            history.add(message("assistant", answer));
            while (history.size() > MAX_HISTORY_MESSAGES) {
                history.remove(0);
            }
            return answer;

        } catch (HttpTimeoutException e) {
            return "AI phản hồi quá lâu, vui lòng thử lại.";
        } catch (IOException e) {
            e.printStackTrace();
            return "Không thể kết nối đến hệ thống AI. Vui lòng kiểm tra lại kết nối mạng!";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Yêu cầu bị gián đoạn.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Có lỗi khi xử lý phản hồi từ AI.";
        }
    }

    /** Lấy choices[0].message.content an toàn; trả null nếu thiếu. */
    private String extractText(JsonObject json) {
        if (json == null) return null;

        // OpenRouter đôi khi trả HTTP 200 kèm trường "error"
        if (json.has("error")) {
            System.err.println("OpenRouter trả về lỗi trong body: " + json.get("error"));
            return null;
        }
        if (!json.has("choices") || !json.get("choices").isJsonArray()) return null;

        JsonArray choices = json.getAsJsonArray("choices");
        if (choices.size() == 0) return null;

        JsonObject first = choices.get(0).getAsJsonObject();
        if (!first.has("message") || !first.get("message").isJsonObject()) return null;

        JsonElement content = first.getAsJsonObject("message").get("content");
        if (content == null || content.isJsonNull()) return null;
        return content.getAsString();
    }
}