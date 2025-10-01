// 예시: src/main/java/com/proxy/DemoRunner.java
package com.proxy;

import com.proxy.contracts.MappingWithDelete;
import com.proxy.contracts.SimpleStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
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
        BigInteger gasLimit = BigInteger.valueOf(1_000_000);
        StaticGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);

        // 2) ContractService 직접 생성
        ContractService contractService = new ContractService(web3j, credentials, gasProvider);

        // 3) MappingWithDelete 배포 (주석 상태 유지)
        // MappingWithDelete mapping = contractService.deploy(MappingWithDelete.class);
        // System.out.println("Deployed at: " + mapping.getContractAddress());

        // 4) 이미 배포된 컨트랙트 주소로 로드
        String deployedAddress = "0xCFE3503Db6c49B9E6A0703C178F47CA12e5E6e9E";  // ★ 실제 주소 넣기
        MappingWithDelete mapping = contractService.load(MappingWithDelete.class, deployedAddress);
        System.out.println("Loaded contract at: " + deployedAddress);

        // 5) 값 저장
        mapping.setValue(BigInteger.valueOf(123)).send();
        System.out.println("Value set complete");

        // 6) 키 조회
        System.out.println("Keys: " + mapping.getKeys().send());

        // 7) 삭제
        mapping.remove(credentials.getAddress()).send();
        System.out.println("After remove: " + mapping.getKeys().send());
    }

}