package com.voting.votingapp.repositories;
import com.voting.votingapp.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


public interface PollRepository extends JpaRepository<Poll, String> {
    List<Poll> findByQuestionContainingIgnoreCase(@RequestParam String question);
    List<Poll> findByowner( String owner);

}
