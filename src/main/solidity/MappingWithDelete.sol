// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract MappingWithDelete {
    mapping(address => uint256) private balances;
    address[] private keys;
    mapping(address => uint256) private keyIndex; // 주소 → 배열 인덱스

    function setValue(uint256 newValue) external {
        if (balances[msg.sender] == 0) {
            keys.push(msg.sender);
            keyIndex[msg.sender] = keys.length - 1; // 인덱스 기록
        }
        balances[msg.sender] = newValue;
    }

    function remove(address user) external {
        require(balances[user] != 0, "Not found");

        // 1) balances 초기화
        balances[user] = 0;

        // 2) keys 배열에서 제거
        uint256 idx = keyIndex[user];
        uint256 lastIdx = keys.length - 1;

        if (idx != lastIdx) {
            // 마지막 요소를 지우려는 자리로 이동
            address lastKey = keys[lastIdx];
            keys[idx] = lastKey;
            keyIndex[lastKey] = idx;
        }

        // 마지막 요소 삭제
        keys.pop();

        // keyIndex 초기화
        delete keyIndex[user];
    }

    function getKeys() external view returns (address[] memory) {
        return keys;
    }
}