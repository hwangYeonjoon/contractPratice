// 예시: src/main/java/com/proxy/DemoRunner.java
package com.proxy;

import com.proxy.contracts.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class DemoRunner {
    public static void main(String[] args) throws Exception {


        // 1) 직접 Web3j, Credentials, GasProvider 준비
        String rpcUrl = "http://192.168.1.115:8545";
        String privateKey = "5ef0e8a56dbb3bfa63d91983db6a9cb4f28de6efebcf7812e099896d005e946e";

        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        Credentials credentials = Credentials.create(privateKey);

        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        BigInteger gasLimit = BigInteger.valueOf(6_000_000);
        StaticGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
        String myAddress = credentials.getAddress();

        EthGetBalance balanceResponse = web3j
                .ethGetBalance(myAddress, org.web3j.protocol.core.DefaultBlockParameterName.LATEST)
                .send();
        System.out.println("myAddress : " + myAddress);
        System.out.println("My balance: " + balanceResponse.getBalance() + " wei");
        System.out.println("My balanceEasy: " + Convert.fromWei(balanceResponse.getBalance().toString(), Convert.Unit.ETHER).toPlainString());

        // 2) ContractService 직접 생성
        ContractService contractService = new ContractService(web3j, credentials, gasProvider);
//        TokenFactory t = contractService.deploy(TokenFactory.class);
//        System.out.println("deployed at: " + t.getContractAddress()); //0x77315b9625aef4d687b15346fbbf46c0ac5f0fdc
        TokenFactory factory= contractService.load(TokenFactory.class, "0x77315b9625aef4d687b15346fbbf46c0ac5f0fdc");

        String name = "YJTestToken";
        String symbol = "YTT";
        BigInteger decimals = BigInteger.valueOf(18);
        BigInteger initial = BigInteger.valueOf(1_000_000);
        boolean mintable = true;
        boolean burnable = true;
        BigInteger cap = BigInteger.valueOf(10_000_000); // 0 이면 무제한


        var receipt = factory.createToken(name, symbol, decimals, initial, mintable, burnable, cap).send();
        System.out.println("check : " + receipt);
        List<TokenFactory.TokenCreatedEventResponse> evs = factory.getTokenCreatedEvents(receipt);
        System.out.println("check evs :" + evs.toString());
        String tokenAddr = evs.get(0).token;
        System.out.println("New token: " + tokenAddr);

        OwnableERC20 token = OwnableERC20.load(tokenAddr, web3j, credentials, gasProvider);

// ✅ 3) 기본 정보 조회
        String tName = token.name().send();
        String tSymbol = token.symbol().send();
        BigInteger tDecimals = token.decimals().send();
        BigInteger tSupply = token.totalSupply().send();
        String tOwner = token.owner().send();

        System.out.println("📘 Token Info");
        System.out.println("Name      : " + tName);
        System.out.println("Symbol    : " + tSymbol);
        System.out.println("Decimals  : " + tDecimals);
        System.out.println("TotalSupply: " + tSupply);
        System.out.println("Owner     : " + tOwner);

// ✅ 4) 내 잔고 조회
        String myAddr = credentials.getAddress();
        BigInteger myBalance = token.balanceOf(myAddr).send();
        System.out.println("My Balance: " + myBalance + " (" +
                myBalance.divide(BigInteger.TEN.pow(tDecimals.intValue())) + " " + tSymbol + ")");


//         3) MappingWithDelete 배포 (주석 상태 유지)
//         MultiAssetBank mapping = contractService.deploy(MultiAssetBank.class);
//         System.out.println("Deployed at: " + mapping.getContractAddress());

//        // 4) 이미 배포된 컨트랙트 주소로 로드
//        String deployedAddress = "0xCFE3503Db6c49B9E6A0703C178F47CA12e5E6e9E";  // ★ 실제 주소 넣기
//          String deployaddress = "0xfb8c2302ae40576ce4c6606bc6483e07f8d02a50";
////        MappingWithDelete mapping = contractService.load(MappingWithDelete.class, deployedAddress);
//          MultiAssetBank bank = contractService.load(MultiAssetBank.class, deployaddress);
//         System.out.println("Deployed at: " + bank.getContractAddress());
//
//        String me = credentials.getAddress();
//         bank.depositCoin(new BigInteger("10000000000000000")).send();
//        System.out.println("[coin] deposit 0.01 OK");
//
//        // 2) 내 코인 잔액 확인 (컨트랙트 내부 잔액)
//        BigInteger myCoin = bank.balanceOf("0x0000000000000000000000000000000000000000", me).send();
//        System.out.println("[coin] balanceOf(me) in bank: " + myCoin + " COIN");


//        System.out.println("Loaded contract at: " + deployedAddress);
//
//        // 5) 값 저장
//        mapping.setValue(BigInteger.valueOf(123)).send();
//        System.out.println("Value set complete");
//
//        // 6) 키 조회
//        System.out.println("Keys: " + mapping.getKeys().send());
//
//        // 7) 삭제
//        mapping.remove(credentials.getAddress()).send();
//        System.out.println("After remove: " + mapping.getKeys().send());
    }

}