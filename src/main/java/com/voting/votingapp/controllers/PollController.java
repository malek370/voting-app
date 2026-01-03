package com.voting.votingapp.controllers;

import com.voting.votingapp.Exceptions.IlligalVote;
import com.voting.votingapp.Exceptions.OptionOrPollNotFound;
import com.voting.votingapp.Services.PollService;
import com.voting.votingapp.model.Poll;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.voting.votingapp.DTO.VoteDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/polls")
public class PollController {
    private final PollService pollService;

    public PollController(PollService pollService) {
        this.pollService = pollService;
    }

    @PostMapping
    public Poll createPoll(@RequestBody Poll poll,@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("createPoll" );
        poll.setOwner(userDetails.getUsername());
        System.out.println(poll.toString());
        return  pollService.createPoll(poll);
    }

    @GetMapping

    public List<Poll> getAllPolls(@RequestParam String search, @AuthenticationPrincipal UserDetails userDetails) {
        List<Poll> resultPolls=new ArrayList<>();
        if(search.startsWith("ID:"))resultPolls= pollService.getById(search.substring(3)).stream().collect(Collectors.toList());
        else if (search.startsWith("OWNER:"))resultPolls= pollService.getPollsByOwner(search.substring(6)).stream().collect(Collectors.toList());
        else resultPolls =  pollService.getAllPolls(search);
        System.out.println("search param: "+search);
        System.out.println("owner of poll:  " + userDetails.getUsername());
        resultPolls.removeIf(poll ->poll.getUsernames().contains(userDetails.getUsername()));
        return resultPolls;
    }
    @GetMapping("/done")

    public List<Poll> getAllPollsDoneByUser(@AuthenticationPrincipal UserDetails userDetails) {
        List<Poll> resultPolls;
        resultPolls =  pollService.getAllPolls("");
        System.out.println("username voter:  " + userDetails.getUsername());
        resultPolls.removeIf(poll ->!poll.getUsernames().contains(userDetails.getUsername()));
        return resultPolls;
    }
    @GetMapping("/mypolls")

    public List<Poll> getAllPollsCreatedByUser(@AuthenticationPrincipal UserDetails userDetails) {
        List<Poll> resultPolls;
        System.out.println("username voter:  " + userDetails.getUsername());
        resultPolls= pollService.getPollsByOwner(userDetails.getUsername());
        resultPolls.forEach(poll -> {poll.setDisabled(poll.getUsernames().contains(userDetails.getUsername()));});
        return resultPolls;
    }
    @GetMapping("/{id}")
    public ResponseEntity<Poll> getPoll(@PathVariable String id) {
        return  pollService.getById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/vote")
    public ResponseEntity Vote(@RequestBody VoteDTO vote, @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Vote Called");
        System.out.println("Option Index: " + vote.getOptionIndex());
        System.out.println("Poll Id: " + vote.getPollId() );
        System.out.println("username voter:  " + userDetails.getUsername());
        try {
            pollService.vote(vote.getPollId(), vote.getOptionIndex(),userDetails.getUsername());
        } catch (IlligalVote e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (OptionOrPollNotFound e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity( HttpStatus.NO_CONTENT);
    }
//    @PostMapping("/seed")
//    public void multiple(@RequestBody List<Poll> polls) {
//        polls.forEach(pollService::createPoll);
//    }

}
