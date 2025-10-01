// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract SimpleStorage {
    uint256 private value;

    event ValueChanged(uint256 newValue);

    // 값 저장 (트랜잭션 발생)
    function set(uint256 newValue) external {
        value = newValue;
        emit ValueChanged(newValue);
    }

    // 값 조회 (view 함수, 가스 소모 없음)
    function get() external view returns (uint256) {
        return value;
    }
}