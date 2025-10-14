package com.proxy.makecontract;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * EIP-681 / EIP-831 기반 이더리움 결제 URI + QR 생성 데모
 * - ETH 전송 (value는 wei)
 * - ERC-20 transfer (수량은 최소단위)
 */
public class Eip681QrDemo {

    public static void main(String[] args) throws Exception {
        Path outDir = Path.of("eth-qrs");
        Files.createDirectories(outDir);

        // ===== 예시 입력 =====
        String recipient = "0x7cB57B5A97eAbe94205C07890BE4c1aD31E486A8"; // 수신 주소
        long chainId = 1L; // 메인넷=1, Sepolia=11155111 등

        // 1) 주소만 (스캔 시 지갑 열림)
        String ex1 = buildEthUri(recipient, chainId, null, null, null, Map.of());
        writeQrAndTxt(ex1, outDir, "ex1_eth_address_only");

        // 2) ETH 0.5 전송 요청 (자동 금액 채움)
        BigDecimal amountEth = new BigDecimal("0.5");
        String ex2 = buildEthUri(recipient, chainId, amountEth, null, null, Map.of());
        writeQrAndTxt(ex2, outDir, "ex2_eth_amount_0_5");

        // 3) 가스/가스가격 힌트까지 포함 (일부 지갑만 사용)
        String ex3 = buildEthUri(recipient, chainId, new BigDecimal("0.01"),
                BigInteger.valueOf(21000),              // gas (옵션)
                new BigInteger("15000000000"),          // gasPrice wei (15 gwei) (옵션)
                Map.of("nonce", "0"));                  // 추가 파라미터 예시
        writeQrAndTxt(ex3, outDir, "ex3_eth_amount_gas_options");

//        // 4) ERC-20 전송 요청 (USDT 12.34개) — 메인넷 USDT 컨트랙트 예시
//        String usdt = "0xdAC17F958D2ee523a2206206994597C13D831ec7";
//        String ex4 = buildErc20TransferUri(usdt, chainId, recipient,
//                new BigDecimal("12.34"), 6); // USDT는 6 decimals
//        writeQrAndTxt(ex4, outDir, "ex4_erc20_usdt_12_34");

        // 4) ERC-20 전송 요청 (USDT 30개) — 이더리움 메인넷 USDT 컨트랙트


        String usdt = "0xdAC17F958D2ee523a2206206994597C13D831ec7"; // Ethereum Mainnet USDT
        String exUSDT30 = buildErc20TransferDataUri(
                usdt,
                chainId,
                recipient,               // 수신자
                new BigDecimal("30"),    // 30 USDT
                6,                       // USDT decimals
                BigInteger.valueOf(90000),     // gas 힌트(옵션, 지갑이 무시할 수 있음)
                new BigInteger("15000000000")  // 15 gwei (옵션)
        );
        writeQrAndTxt(exUSDT30, outDir, "ex_usdt_30_data_uri");

        System.out.println("완료! eth-qrs/ 폴더에 PNG와 URI 텍스트가 생성되었습니다.");
    }

    /* ----------------- ETH 전송 (EIP-681) ----------------- */
    public static String buildEthUri(String toAddress,
                                     Long chainId,
                                     BigDecimal amountEth,         // ETH 단위 (소수점)
                                     BigInteger gas,               // 옵션
                                     BigInteger gasPriceWei,       // 옵션
                                     Map<String, String> extras) { // 옵션
        StringBuilder sb = new StringBuilder();
        sb.append("ethereum:").append(toAddress);
        if (chainId != null) sb.append("@").append(chainId);

        Map<String, String> params = new LinkedHashMap<>();
        if (amountEth != null) {
            // value는 wei 정수 문자열이어야 함 (EIP-681)
            params.put("value", toWei(amountEth).toString());
        }
        if (gas != null) params.put("gas", gas.toString());
        if (gasPriceWei != null) params.put("gasPrice", gasPriceWei.toString());
        if (extras != null) {
            extras.forEach((k, v) -> params.put(k, rfc3986(v)));
        }
        if (!params.isEmpty()) {
            sb.append("?").append(joinParams(params));
        }
        return sb.toString();
    }

    /* ----------------- ERC-20 transfer (EIP-681) ----------------- */
    public static String buildErc20TransferUri(String tokenContract,
                                               Long chainId,
                                               String toAddress,
                                               BigDecimal tokenAmount, // 토큰 단위 (소수점)
                                               int decimals) {
        StringBuilder sb = new StringBuilder();
        sb.append("ethereum:").append(tokenContract);
        if (chainId != null) sb.append("@").append(chainId);
        sb.append("/transfer");

        Map<String, String> params = new LinkedHashMap<>();
        params.put("address", toAddress);
        // uint256은 최소단위(atomic units) 정수
        BigInteger atomic = toAtomicUnits(tokenAmount, decimals);
        params.put("uint256", atomic.toString());

        sb.append("?").append(joinParams(params));
        return sb.toString();
    }

    /* ----------------- 유틸 ----------------- */
    private static BigInteger toWei(BigDecimal eth) {
        return eth.multiply(new BigDecimal("1000000000000000000")).toBigIntegerExact();
    }
    private static BigInteger toAtomicUnits(BigDecimal amount, int decimals) {
        BigDecimal factor = new BigDecimal(BigInteger.TEN.pow(decimals));
        return amount.multiply(factor).toBigIntegerExact();
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
    private static String rfc3986(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static void writeQrAndTxt(String content, Path outDir, String base) throws Exception {
        // QR
        int size = 380;
        Map<EncodeHintType, Object> hints = Map.of(EncodeHintType.MARGIN, 1);
        BitMatrix m = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        MatrixToImageWriter.writeToPath(m, "PNG", outDir.resolve(base + ".png"));
        // 텍스트
        Files.writeString(outDir.resolve(base + ".txt"), content + System.lineSeparator());
    }

    // === 새로 추가 ===
    // === 유틸 3개 추가 ===

    // === 새로 추가 ===
    public static String buildErc20TransferDataUri(String tokenContract,
                                                   Long chainId,
                                                   String toAddress,
                                                   BigDecimal tokenAmount,
                                                   int decimals,
                                                   BigInteger gas,          // 옵션
                                                   BigInteger gasPriceWei)  // 옵션
    {
        // 1) transfer(address,uint256) 함수 시그니처 0xa9059cbb
        String method = "0xa9059cbb";

        // 2) 파라미터 ABI 인코딩 (32바이트 왼쪽 패딩)
        BigInteger atomic = toAtomicUnits(tokenAmount, decimals); // 30 USDT -> 30_000_000 (6 decimals)
        String addrHex = strip0x(toAddress).toLowerCase();        // 40 hex chars
        if (addrHex.length() != 40) throw new IllegalArgumentException("Invalid address");

        String data = method
                + pad32(addrHex)                    // address
                + pad32(atomic.toString(16));       // uint256

        StringBuilder sb = new StringBuilder();
        sb.append("ethereum:").append(tokenContract);
        if (chainId != null) sb.append("@").append(chainId);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("data", data);
        params.put("value", "0"); // 토큰 전송은 ETH 전송금액 0
        if (gas != null) params.put("gas", gas.toString());
        if (gasPriceWei != null) params.put("gasPrice", gasPriceWei.toString());

        sb.append("?").append(joinParams(params));
        return sb.toString();
    }

    private static String strip0x(String s) {
        return (s.startsWith("0x") || s.startsWith("0X")) ? s.substring(2) : s;
    }
    private static String pad32(String hexNoPrefix) {
        String h = hexNoPrefix.toLowerCase();
        if (h.length() > 64) throw new IllegalArgumentException("hex too long");
        return "0".repeat(64 - h.length()) + h;
    }
}
