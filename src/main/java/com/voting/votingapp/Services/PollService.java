package com.voting.votingapp.Services;

import com.voting.votingapp.Exceptions.IlligalVote;
import com.voting.votingapp.Exceptions.OptionOrPollNotFound;
import com.voting.votingapp.model.OptionVote;
import com.voting.votingapp.model.Poll;
import com.voting.votingapp.repositories.PollRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PollService {


    private final PollRepository pollRepository;

    public PollService(PollRepository pollRepository) {
        this.pollRepository = pollRepository;
    }

    public Poll createPoll(Poll poll) {
        System.out.println("createPoll service" );
        System.out.println(poll.toString());
        return  pollRepository.save(poll);
    }
    public List<Poll> getAllPolls(String search) {
        return  pollRepository.findByQuestionContainingIgnoreCase(search);
    }

    public Optional<Poll> getById(String id) {
        return  pollRepository.findById(id);
    }

    public List<Poll> getPollsByOwner(String owner) {
        return pollRepository.findByowner(owner);
    }
    public void vote(String pollId, int optionIndex,String username)throws OptionOrPollNotFound,IlligalVote {
        if(pollId==null)throw new OptionOrPollNotFound("Poll id is null");
        var poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new OptionOrPollNotFound("Poll not found"));
        OptionVote option;
        try {
            option  = poll.getOptions().get(optionIndex);
        }
        catch (IndexOutOfBoundsException e) {
            throw new OptionOrPollNotFound("Option not found");
        }
        if(poll.getUsernames().contains(username)) {throw new IlligalVote("You already voted");
        }
        option.setVoteCount(option.getVoteCount() + 1);
        poll.getUsernames().add(username);
        pollRepository.save(poll);
    }
}
