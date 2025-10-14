// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

interface IERC20 {
    function transfer(address to, uint256 amount) external returns (bool);
    function transferFrom(address from, address to, uint256 amount) external returns (bool);
    function balanceOf(address who) external view returns (uint256);
    function approve(address spender, uint256 amount) external returns (bool);
    function allowance(address owner, address spender) external view returns (uint256);
}

contract MultiAssetBank {
    // 네이티브 코인 식별자
    address public constant COIN = address(0);

    // token => user => amount
    mapping(address => mapping(address => uint256)) private _balances;

    // 유저별 보유 토큰 목록(인덱싱용)
    mapping(address => address[]) private _userTokens;
    mapping(address => mapping(address => uint256)) private _userTokenIdx;

    // 토큰별 총 예치액
    mapping(address => uint256) private _totalByToken;

    address public owner;
    bool public paused;

    // reentrancy guard
    uint256 private _locked = 1;

    event Deposited(address indexed token, address indexed user, uint256 amount);
    event Withdrawn(address indexed token, address indexed user, uint256 amount);
    event Paused();
    event Unpaused();

    error PausedError();
    error NotOwner();
    error ZeroAmount();
    error InsufficientBalance();
    error TransferFailed();
    error Reentrancy();

    modifier nonReentrant() {
        if (_locked != 1) revert Reentrancy();
        _locked = 2;
        _;
        _locked = 1;
    }

    modifier onlyOwner() {
        if (msg.sender != owner) revert NotOwner();
        _;
    }

    modifier whenNotPaused() {
        if (paused) revert PausedError();
        _;
    }

    constructor() {
        owner = msg.sender;
    }

    // -------- 내부 유틸 --------
    function _addUserTokenIfNew(address user, address token) internal {
        if (_userTokenIdx[user][token] == 0) {
            _userTokens[user].push(token);
            _userTokenIdx[user][token] = _userTokens[user].length; // index+1
        }
    }

    function _removeUserTokenIfZero(address user, address token) internal {
        if (_balances[token][user] == 0 && _userTokenIdx[user][token] != 0) {
            uint256 idxPlus = _userTokenIdx[user][token];
            uint256 idx = idxPlus - 1;
            uint256 last = _userTokens[user].length - 1;

            if (idx != last) {
                address lastToken = _userTokens[user][last];
                _userTokens[user][idx] = lastToken;
                _userTokenIdx[user][lastToken] = idx + 1;
            }
            _userTokens[user].pop();
            delete _userTokenIdx[user][token];
        }
    }

    // -------- 입금 --------
    function depositCoin() external payable whenNotPaused nonReentrant {
        if (msg.value == 0) revert ZeroAmount();

        _addUserTokenIfNew(msg.sender, COIN);
        _balances[COIN][msg.sender] += msg.value;
        _totalByToken[COIN] += msg.value;

        emit Deposited(COIN, msg.sender, msg.value);
    }

    function depositToken(address token, uint256 amount) external whenNotPaused nonReentrant {
        if (amount == 0) revert ZeroAmount();

        _addUserTokenIfNew(msg.sender, token);

        bool ok = IERC20(token).transferFrom(msg.sender, address(this), amount);
        if (!ok) revert TransferFailed();

        _balances[token][msg.sender] += amount;
        _totalByToken[token] += amount;

        emit Deposited(token, msg.sender, amount);
    }

    // -------- 출금 --------
    function withdrawCoin(uint256 amount) external whenNotPaused nonReentrant {
        if (amount == 0) revert ZeroAmount();
        uint256 bal = _balances[COIN][msg.sender];
        if (bal < amount) revert InsufficientBalance();

        _balances[COIN][msg.sender] = bal - amount;
        _totalByToken[COIN] -= amount;

        (bool ok, ) = payable(msg.sender).call{value: amount}("");
        if (!ok) revert TransferFailed();

        _removeUserTokenIfZero(msg.sender, COIN);
        emit Withdrawn(COIN, msg.sender, amount);
    }

    function withdrawToken(address token, uint256 amount) external whenNotPaused nonReentrant {
        if (amount == 0) revert ZeroAmount();
        uint256 bal = _balances[token][msg.sender];
        if (bal < amount) revert InsufficientBalance();

        _balances[token][msg.sender] = bal - amount;
        _totalByToken[token] -= amount;

        bool ok = IERC20(token).transfer(msg.sender, amount);
        if (!ok) revert TransferFailed();

        _removeUserTokenIfZero(msg.sender, token);
        emit Withdrawn(token, msg.sender, amount);
    }

    // -------- 조회 --------
    function balanceOf(address token, address user) external view returns (uint256) {
        return _balances[token][user];
    }

    function totalByToken(address token) external view returns (uint256) {
        return _totalByToken[token];
    }

    function userTokenCount(address user) external view returns (uint256) {
        return _userTokens[user].length;
    }

    function userTokenAt(address user, uint256 index) external view returns (address) {
        require(index < _userTokens[user].length, "index OOB");
        return _userTokens[user][index];
    }

    // ✅ 유저 전체 보유 자산 조회
    function portfolioOf(address user)
    external
    view
    returns (address[] memory tokens, uint256[] memory amounts)
    {
        uint256 n = _userTokens[user].length;
        tokens = new address[](n);
        amounts = new uint256[](n);
        for (uint256 i = 0; i < n; i++) {
            address t = _userTokens[user][i];
            tokens[i] = t;
            amounts[i] = _balances[t][user];
        }
    }

    // ✅ 부분조회 (offset, limit)
    function portfolioSlice(
        address user,
        uint256 offset,
        uint256 limit
    )
    external
    view
    returns (address[] memory tokens, uint256[] memory amounts)
    {
        uint256 n = _userTokens[user].length;

        // end = min(n, offset + limit)
        uint256 end = offset + limit;
        if (end > n) end = n;

        // offset > end 인 경우에도 len = 0 이 되도록 보정
        if (offset > end) offset = end;

        uint256 len = end - offset;             // 여기서 len 이 0 이 될 수 있음
        tokens  = new address[](len);           // len=0 이면 빈 배열 생성
        amounts = new uint256[](len);

        for (uint256 i = 0; i < len; i++) {
            address t = _userTokens[user][offset + i];
            tokens[i]  = t;
            amounts[i] = _balances[t][user];
        }
    }

    // -------- 관리자 --------
    function pause() external onlyOwner {
        paused = true;
        emit Paused();
    }

    function unpause() external onlyOwner {
        paused = false;
        emit Unpaused();
    }

    receive() external payable {
        if (msg.value > 0) {
            _addUserTokenIfNew(msg.sender, COIN);
            _balances[COIN][msg.sender] += msg.value;
            _totalByToken[COIN] += msg.value;
            emit Deposited(COIN, msg.sender, msg.value);
        }
    }
}