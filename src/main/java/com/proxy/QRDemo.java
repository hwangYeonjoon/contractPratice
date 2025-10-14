package com.proxy;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BIP-21 Bitcoin URI 생성 + QR 코드(PNG) 출력 데모
 */
public class QRDemo {

    public static void main(String[] args) throws Exception {
        String address = "1Btqg2tgL8aW8tEtvbvufdhMHSSj4Y65qG";
//        1Btqg2tgL8aW8tEtvbvufdhMHSSj4Y65qG
        // 출력 폴더
        Path outDir = Path.of("qrs");
        Files.createDirectories(outDir);

        // 1) Just the address
        String ex1 = buildBip21Uri(address, null, null, null, Map.of());
        writeQr(ex1, outDir.resolve("ex1_address_only.png"));
        writeText(ex1, outDir.resolve("ex1_address_only.txt"));

        // 2) Address with name (label)
        String ex2 = buildBip21Uri(address, null, "Luke-Jr", null, Map.of());
        writeQr(ex2, outDir.resolve("ex2_with_label.png"));
        writeText(ex2, outDir.resolve("ex2_with_label.txt"));

        // 3) Request 20.30 BTC to "Luke-Jr"
        String ex3 = buildBip21Uri(address, new BigDecimal("20.3"), "Luke-Jr", null, Map.of());
        writeQr(ex3, outDir.resolve("ex3_amount_and_label.png"));
        writeText(ex3, outDir.resolve("ex3_amount_and_label.txt"));

        // 4) Request 50 BTC with message
        String ex4 = buildBip21Uri(address, new BigDecimal("50"), "Luke-Jr", "Donation for project xyz", Map.of());
        writeQr(ex4, outDir.resolve("ex4_amount_label_message.png"));
        writeText(ex4, outDir.resolve("ex4_amount_label_message.txt"));

        // 5) Future version with unknown *required* params (invalid by spec)
        Map<String, String> reqUnknown = new LinkedHashMap<>();
        reqUnknown.put("req-somethingyoudontunderstand", "50");
        reqUnknown.put("req-somethingelseyoudontget", "999");
        String ex5 = buildBip21Uri(address, null, null, null, reqUnknown);
        writeQr(ex5, outDir.resolve("ex5_required_unknown_params_INVALID.png"));
        writeText(ex5, outDir.resolve("ex5_required_unknown_params_INVALID.txt"));

        // 6) Future version with unknown but *non-required* params (valid)
        Map<String, String> unknown = new LinkedHashMap<>();
        unknown.put("somethingyoudontunderstand", "50");
        unknown.put("somethingelseyoudontget", "999");
        String ex6 = buildBip21Uri(address, null, null, null, unknown);
        writeQr(ex6, outDir.resolve("ex6_optional_unknown_params_VALID.png"));
        writeText(ex6, outDir.resolve("ex6_optional_unknown_params_VALID.txt"));

        System.out.println("완료! qrs/ 폴더에 PNG와 URI 텍스트가 생성되었습니다.");
    }

    /**
     * BIP-21 URI 생성
     *
     * @param address  필수: 비트코인 주소
     * @param amount   선택: BTC 단위 금액 (소수점, 마침표 '.' 사용)
     * @param label    선택: 수신자 표시용 이름
     * @param message  선택: 메모/메시지
     * @param extras   선택: 추가 파라미터 (key=value), 'req-'로 시작하면 미지원 시 invalid 취급 대상
     */
    public static String buildBip21Uri(String address,
                                       BigDecimal amount,
                                       String label,
                                       String message,
                                       Map<String, String> extras) {

        StringBuilder sb = new StringBuilder();
        sb.append("bitcoin:").append(address);

        Map<String, String> params = new LinkedHashMap<>();
        if (amount != null) {
            // BIP-21은 10진수 표기, 소수점(.) 사용. 과학적 표기 금지
            params.put("amount", formatAmount(amount));
        }
        if (label != null && !label.isEmpty()) {
            params.put("label", rfc3986Encode(label));
        }
        if (message != null && !message.isEmpty()) {
            params.put("message", rfc3986Encode(message));
        }
        if (extras != null && !extras.isEmpty()) {
            for (Map.Entry<String, String> e : extras.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                // 키는 그대로, 값만 RFC3986 인코딩 (스펙 상 파라미터 값은 인코딩 필요)
                params.put(k, rfc3986Encode(v));
            }
        }

        if (!params.isEmpty()) {
            sb.append("?");
            sb.append(joinParams(params));
        }

        return sb.toString();
    }

    private static String joinParams(Map<String, String> params) {
        StringBuilder qs = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) qs.append("&");
            first = false;
            qs.append(e.getKey()).append("=").append(e.getValue());
        }
        return qs.toString();
    }

    /**
     * BIP-21 amount는 10진수 문자열(소수점 .)이어야 하며
     * 불필요한 0 또는 과학적 표기 없이 출력합니다.
     */
    private static String formatAmount(BigDecimal amount) {
        // 소수점 이하 8자리까지 사용하는 경우가 많으므로, 트레일링 제로 제거
        BigDecimal stripped = amount.stripTrailingZeros();
        // 과학적 표기 방지
        DecimalFormat df = new DecimalFormat("0.################");
        df.setMaximumFractionDigits(Math.max(0, stripped.scale()));
        return df.format(stripped);
    }

    /**
     * RFC 3986 스타일 인코딩
     * - java.net.URLEncoder는 공백을 '+'로 바꾸므로, 이를 '%20'으로 다시 치환
     * - 안전한 문자 외에는 퍼센트 인코딩
     */
    private static String rfc3986Encode(String raw) {
        String enc = URLEncoder.encode(raw, StandardCharsets.UTF_8)
                .replace("+", "%20");
        // URLEncoder는 '*'도 인코딩하므로 굳이 되돌릴 필요는 없음 (엄격 인코딩 유지)
        return enc;
    }

    /**
     * QR 코드 PNG 생성
     */
    private static void writeQr(String content, Path outputPng) throws IOException, WriterException {
        int size = 360; // 필요시 512/720 등으로 키우세요
        Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.MARGIN, 1 // 여백 최소화
        );
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        MatrixToImageWriter.writeToPath(matrix, "PNG", outputPng);
    }

    /**
     * URI 문자열도 파일로 남겨 확인 가능하게
     */
    private static void writeText(String content, Path outputTxt) throws IOException {
        Files.writeString(outputTxt, content + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }
}
