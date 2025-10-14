// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

/**
 * 최소 구현 ERC20 + Ownable + (옵션) Mint/Burn/Cap
 * - 총 공급/잔액/승인: 표준 방식
 * - 소수점: constructor에서 지정
 * - 초깃값: initialSupply * 10**decimals 를 owner에게 민팅
 * - 민팅: onlyOwner && mintable == true && (cap==0 || totalSupply+amount <= cap)
 * - 소각: burn/burnFrom 는 burnable == true 일 때만
 */
contract OwnableERC20 {
    // --- ERC20 표준 저장소 ---
    string public name;
    string public symbol;
    uint8  public decimals;
    uint256 public totalSupply;

    mapping(address => uint256) public balanceOf;
    mapping(address => mapping(address => uint256)) public allowance;

    // --- 소유권/정책 ---
    address public owner;
    bool    public mintable;
    bool    public burnable;
    uint256 public cap; // 0이면 무제한

    // --- 이벤트 ---
    event Transfer(address indexed from, address indexed to, uint256 value);
    event Approval(address indexed owner, address indexed spender, uint256 value);
    event OwnershipTransferred(address indexed previousOwner, address indexed newOwner);
    event Mint(address indexed to, uint256 amount);
    event Burn(address indexed from, uint256 amount);

    // --- 오류 ---
    error NotOwner();
    error MintingDisabled();
    error BurningDisabled();
    error CapExceeded();
    error InsufficientBalance();
    error InsufficientAllowance();
    error ZeroAddress();
    error ZeroAmount();

    modifier onlyOwner() {
        if (msg.sender != owner) revert NotOwner();
        _;
    }

    constructor(
        string memory _name,
        string memory _symbol,
        uint8  _decimals,
        uint256 _initialSupply,   // 사람 기준 (예: 1_000_000)
        bool _mintable,
        bool _burnable,
        uint256 _cap              // 사람 기준; 0이면 무제한
    ) {
        if (bytes(_name).length == 0 || bytes(_symbol).length == 0) revert ZeroAmount();
        name = _name;
        symbol = _symbol;
        decimals = _decimals;
        owner = msg.sender;
        mintable = _mintable;
        burnable = _burnable;

        uint256 mul = 10 ** uint256(_decimals);
        uint256 init = _initialSupply * mul;
        uint256 capRaw = (_cap == 0) ? 0 : _cap * mul;
        cap = capRaw; // 0 or scaled cap

        if (cap != 0 && init > cap) revert CapExceeded();

        // 초기 발행 -> owner
        if (init > 0) {
            totalSupply = init;
            balanceOf[owner] += init;
            emit Transfer(address(0), owner, init);
            emit Mint(owner, init);
        }

        emit OwnershipTransferred(address(0), owner);
    }

    // --- 소유권 이전 ---
    function transferOwnership(address newOwner) external onlyOwner {
        if (newOwner == address(0)) revert ZeroAddress();
        emit OwnershipTransferred(owner, newOwner);
        owner = newOwner;
    }

    // --- ERC20 표준 함수 ---
    function transfer(address to, uint256 amount) external returns (bool) {
        _transfer(msg.sender, to, amount);
        return true;
    }

    function approve(address spender, uint256 amount) external returns (bool) {
        allowance[msg.sender][spender] = amount;
        emit Approval(msg.sender, spender, amount);
        return true;
    }

    function transferFrom(address from, address to, uint256 amount) external returns (bool) {
        uint256 allowed = allowance[from][msg.sender];
        if (allowed < amount) revert InsufficientAllowance();
        allowance[from][msg.sender] = allowed - amount;
        _transfer(from, to, amount);
        emit Approval(from, msg.sender, allowance[from][msg.sender]);
        return true;
    }

    function _transfer(address from, address to, uint256 amount) internal {
        if (to == address(0)) revert ZeroAddress();
        if (amount == 0) revert ZeroAmount();
        uint256 bal = balanceOf[from];
        if (bal < amount) revert InsufficientBalance();
        unchecked {
            balanceOf[from] = bal - amount;
            balanceOf[to] += amount;
        }
        emit Transfer(from, to, amount);
    }

    // --- 민팅/소각 ---
    function mint(address to, uint256 amount) external onlyOwner {
        if (!mintable) revert MintingDisabled();
        if (to == address(0)) revert ZeroAddress();
        if (amount == 0) revert ZeroAmount();
        if (cap != 0 && totalSupply + amount > cap) revert CapExceeded();

        totalSupply += amount;
        balanceOf[to] += amount;
        emit Transfer(address(0), to, amount);
        emit Mint(to, amount);
    }

    // 소유자/사용자 누구나 자신의 토큰 소각 (burnable=true)
    function burn(uint256 amount) external {
        if (!burnable) revert BurningDisabled();
        uint256 bal = balanceOf[msg.sender];
        if (amount == 0) revert ZeroAmount();
        if (bal < amount) revert InsufficientBalance();

        unchecked {
            balanceOf[msg.sender] = bal - amount;
            totalSupply -= amount;
        }
        emit Transfer(msg.sender, address(0), amount);
        emit Burn(msg.sender, amount);
    }

    // 승인받은 물량 소각
    function burnFrom(address from, uint256 amount) external {
        if (!burnable) revert BurningDisabled();
        uint256 allowed = allowance[from][msg.sender];
        if (allowed < amount) revert InsufficientAllowance();
        uint256 bal = balanceOf[from];
        if (bal < amount) revert InsufficientBalance();

        allowance[from][msg.sender] = allowed - amount;
        unchecked {
            balanceOf[from] = bal - amount;
            totalSupply -= amount;
        }
        emit Approval(from, msg.sender, allowance[from][msg.sender]);
        emit Transfer(from, address(0), amount);
        emit Burn(from, amount);
    }
}