package com.proxy;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;

import java.lang.reflect.Method;

public class ContractService {

    private final Web3j web3j;
    private final Credentials credentials;
    private final ContractGasProvider gasProvider;

    // ★ 명시적 3-파라미터 생성자 (Lombok 불필요)
    public ContractService(Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        this.web3j = web3j;
        this.credentials = credentials;
        this.gasProvider = gasProvider;
    }

    /** 컨트랙트 배포 */
    @SuppressWarnings("unchecked")
    public <T extends Contract> T deploy(Class<T> contractClass) throws Exception {
        Method deployMethod = contractClass.getMethod(
                "deploy", Web3j.class, Credentials.class, ContractGasProvider.class);
        Object rc = deployMethod.invoke(null, web3j, credentials, gasProvider);
        return (T) ((org.web3j.protocol.core.RemoteCall<?>) rc).send();
    }

    /** 컨트랙트 로드 */
    @SuppressWarnings("unchecked")
    public <T extends Contract> T load(Class<T> contractClass, String address) throws Exception {
        Method loadMethod = contractClass.getMethod(
                "load", String.class, Web3j.class, Credentials.class, ContractGasProvider.class);
        return (T) loadMethod.invoke(null, address, web3j, credentials, gasProvider);
    }
}