package com.voting.votingapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@Embeddable
public class OptionVote {

    private  String VoteOption;
    private  Long VoteCount=0L;

}
