// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "./OwnableERC20.sol";

contract TokenFactory {
    address[] public allTokens;
    mapping(address => address[]) public tokensByCreator;

    event TokenCreated(
        address indexed creator,
        address indexed token,
        string name,
        string symbol,
        uint8  decimals,
        bool   mintable,
        bool   burnable,
        uint256 capRaw // 사람 기준 cap (decimals 미적용 값)
    );

    function createToken(
        string memory name,
        string memory symbol,
        uint8  decimals,
        uint256 initialSupply,  // 사람 기준
        bool   mintable,
        bool   burnable,
        uint256 cap             // 사람 기준(0=무제한)
    ) external returns (address tokenAddr) {
        OwnableERC20 t = new OwnableERC20(
            name, symbol, decimals, initialSupply, mintable, burnable, cap
        );
        tokenAddr = address(t);

        allTokens.push(tokenAddr);
        tokensByCreator[msg.sender].push(tokenAddr);

        emit TokenCreated(msg.sender, tokenAddr, name, symbol, decimals, mintable, burnable, cap);
    }

    function getAllTokens() external view returns (address[] memory) {
        return allTokens;
    }

    function getTokensByCreator(address creator) external view returns (address[] memory) {
        return tokensByCreator[creator];
    }
}