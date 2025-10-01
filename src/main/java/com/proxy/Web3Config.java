package com.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

@Configuration
public class Web3Config {

    @Value("${web3.rpc-url:http://127.0.0.1:8545}")
    private String rpcUrl;

    @Value("${web3.private-key}")
    private String privateKey;

    // 가스 리밋은 네트워크 성격에 따라 조절 (EIP-1559 네트워크면 StaticEIP1559GasProvider로 교체)
    @Value("${web3.gas-limit:1000000}")
    private long gasLimit;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(rpcUrl));
    }

    @Bean
    public Credentials credentials() {
        return Credentials.create(privateKey);
    }

    @Bean
    public StaticGasProvider gasProvider(Web3j web3j) throws Exception {
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice(); // legacy
        return new StaticGasProvider(gasPrice, BigInteger.valueOf(gasLimit));
    }
}
