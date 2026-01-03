package com.voting.votingapp.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VoteDTO {
    private String pollId;
    private int optionIndex;

}
