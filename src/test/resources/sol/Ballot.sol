// SPDX-License-Identifier: MIT
pragma solidity ^"0.8.0";

contract Ballot {

    struct Candidate {
        bool voted;
        address addr;
        uint32 voteAmount;
    }
    
    event VoteEvent(
        address indexed voter,
        address indexed candidate,
        uint256 date,
        string description
    );

    uint public expirationTime;
    Candidate[] public candidates;
    mapping(address => uint32) private candidatesMap;
    uint32 private winnerIndex;

    constructor(address[] memory candidateAddrs, uint _expirationTime) {

        expirationTime = _expirationTime;
        winnerIndex = 0;
        for (uint32 i = 0; i<candidateAddrs.length; i++) {
            candidates.push(Candidate({
                voted:false,
                addr: candidateAddrs[i],
                voteAmount: 0
            }));
            candidatesMap[candidateAddrs[i]] = i+1;
        }
    }
    
    function vote(address candidateAddr) public {

        //check
        require(!isFinished(), "Voting is finished");
        require(isCandidateContains(msg.sender), "Voter not found");
        require(isCandidateContains(candidateAddr), "Candidate not found");
     
        //get voter
        Candidate storage voter = candidates[getCandidateIndex(msg.sender)];
        require(!voter.voted, "Voter already voted");
        //get candidate
        uint32 candidateIndex = getCandidateIndex(candidateAddr);
        Candidate storage candidate = candidates[candidateIndex];
        //change state
        voter.voted = true;
        candidate.voteAmount++;
        //set winner
        Candidate storage winner = candidates[winnerIndex];
        if (winner.voteAmount<candidate.voteAmount) {
            winnerIndex = candidateIndex;
        }
        //event
        emit VoteEvent(msg.sender, candidateAddr, 100500, "Custom log text" );
    }

    function isFinished() public view returns (bool){
        return expirationTime < block.timestamp;
    }
    
    //this function was created only for getWinner work
    function finish() payable public { 
        require(isFinished(), "Voting is not finished");
    }
    
    function getWinner() public view returns (address addr,uint32 voteAmount) {
        require(isFinished(), "Voting is not finished");
        Candidate storage winner = candidates[winnerIndex];
        require(winner.voteAmount>0, "Nobody voited");
        return (winner.addr, winner.voteAmount);
    }

    function isCandidateContains(address addr) public view returns(bool){
        return candidatesMap[addr]>0;
    }

    function getCandidateIndex(address addr) private view returns (uint32) {
        return candidatesMap[addr]-1;
    }
    
}