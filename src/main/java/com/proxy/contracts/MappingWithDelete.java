package com.proxy.contracts;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.10.0.
 */
@SuppressWarnings("rawtypes")
public class MappingWithDelete extends Contract {
    public static final String BINARY = "6080604052348015600f57600080fd5b506103f88061001f6000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c80632150c5181461004657806329092d0e146100645780635524107714610079575b600080fd5b61004e61008c565b60405161005b91906102da565b60405180910390f35b610077610072366004610326565b6100ee565b005b610077610087366004610356565b610255565b606060018054806020026020016040519081016040528092919081815260200182805480156100e457602002820191906000526020600020905b81546001600160a01b031681526001909101906020018083116100c6575b5050505050905090565b6001600160a01b03811660009081526020819052604081205490036101455760405162461bcd60e51b8152602060048201526009602482015268139bdd08199bdd5b9960ba1b604482015260640160405180910390fd5b6001600160a01b03811660009081526020818152604080832083905560029091528120546001805491929161017a919061036f565b90508082146102025760006001828154811061019857610198610396565b600091825260209091200154600180546001600160a01b0390921692508291859081106101c7576101c7610396565b600091825260208083209190910180546001600160a01b0319166001600160a01b039485161790559290911681526002909152604090208290555b6001805480610213576102136103ac565b60008281526020808220830160001990810180546001600160a01b03191690559092019092556001600160a01b03949094168152600290935250506040812055565b3360009081526020819052604081205490036102c85760018054808201825560008290527fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf60180546001600160a01b0319163317905580546102b7919061036f565b336000908152600260205260409020555b33600090815260208190526040902055565b602080825282518282018190526000918401906040840190835b8181101561031b5783516001600160a01b03168352602093840193909201916001016102f4565b509095945050505050565b60006020828403121561033857600080fd5b81356001600160a01b038116811461034f57600080fd5b9392505050565b60006020828403121561036857600080fd5b5035919050565b8181038181111561039057634e487b7160e01b600052601160045260246000fd5b92915050565b634e487b7160e01b600052603260045260246000fd5b634e487b7160e01b600052603160045260246000fdfea26469706673582212208b83ebc24d66b61bfb6b5f8e290c5c1a93dc137cc95d4825e6608518ec0ae67864736f6c634300081c0033";

    public static final String FUNC_GETKEYS = "getKeys";

    public static final String FUNC_REMOVE = "remove";

    public static final String FUNC_SETVALUE = "setValue";

    @Deprecated
    protected MappingWithDelete(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected MappingWithDelete(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected MappingWithDelete(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected MappingWithDelete(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<List> getKeys() {
        final Function function = new Function(FUNC_GETKEYS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> remove(String user) {
        final Function function = new Function(
                FUNC_REMOVE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, user)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setValue(BigInteger newValue) {
        final Function function = new Function(
                FUNC_SETVALUE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(newValue)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static MappingWithDelete load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new MappingWithDelete(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static MappingWithDelete load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new MappingWithDelete(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static MappingWithDelete load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new MappingWithDelete(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static MappingWithDelete load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new MappingWithDelete(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<MappingWithDelete> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(MappingWithDelete.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<MappingWithDelete> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(MappingWithDelete.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<MappingWithDelete> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(MappingWithDelete.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<MappingWithDelete> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(MappingWithDelete.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }
}
