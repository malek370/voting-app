package com.voting.votingapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String Id;
    private String question;
    @ElementCollection
    private List<OptionVote> Options = new ArrayList<>();
    private LocalDateTime dateTimeEnd;
    private boolean AllowMultipleVote;
    @ElementCollection
    private List<String> usernames = new ArrayList<>();
    private String owner;
    @Transient
    private boolean disabled;
//    @ElementCollection(fetch = FetchType.EAGER)
//    private List<Long> Votes = new ArrayList<>();
}
