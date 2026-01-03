package com.voting.votingapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PollTest {

    private Poll poll;
    private OptionVote option1;
    private OptionVote option2;

    @BeforeEach
    void setUp() {
        option1 = new OptionVote();
        option1.setVoteOption("Option 1");
        option1.setVoteCount(0L);

        option2 = new OptionVote();
        option2.setVoteOption("Option 2");
        option2.setVoteCount(0L);

        poll = new Poll();
        poll.setId("poll-id");
        poll.setQuestion("What is your favorite color?");
        poll.setOptions(new ArrayList<>(Arrays.asList(option1, option2)));
        poll.setUsernames(new ArrayList<>());
        poll.setOwner("testowner");
        poll.setDateTimeEnd(LocalDateTime.now().plusDays(1));
        poll.setAllowMultipleVote(false);
    }

    @Test
    void poll_ShouldInitializeCorrectly() {
        // Assert
        assertEquals("poll-id", poll.getId());
        assertEquals("What is your favorite color?", poll.getQuestion());
        assertEquals(2, poll.getOptions().size());
        assertEquals("testowner", poll.getOwner());
        assertFalse(poll.isAllowMultipleVote());
        assertNotNull(poll.getUsernames());
        assertTrue(poll.getUsernames().isEmpty());
    }

    @Test
    void addUsername_ShouldAddUserToList() {
        // Act
        poll.getUsernames().add("user1");
        poll.getUsernames().add("user2");

        // Assert
        assertEquals(2, poll.getUsernames().size());
        assertTrue(poll.getUsernames().contains("user1"));
        assertTrue(poll.getUsernames().contains("user2"));
    }

    @Test
    void getOptions_ShouldReturnAllOptions() {
        // Assert
        assertNotNull(poll.getOptions());
        assertEquals(2, poll.getOptions().size());
        assertEquals("Option 1", poll.getOptions().get(0).getVoteOption());
        assertEquals("Option 2", poll.getOptions().get(1).getVoteOption());
    }

    @Test
    void setDisabled_ShouldChangeDisabledState() {
        // Act
        poll.setDisabled(true);

        // Assert
        assertTrue(poll.isDisabled());

        // Act
        poll.setDisabled(false);

        // Assert
        assertFalse(poll.isDisabled());
    }

    @Test
    void setAllowMultipleVote_ShouldChangeVotingRules() {
        // Act
        poll.setAllowMultipleVote(true);

        // Assert
        assertTrue(poll.isAllowMultipleVote());
    }

    @Test
    void dateTimeEnd_ShouldBeInFuture() {
        // Assert
        assertNotNull(poll.getDateTimeEnd());
        assertTrue(poll.getDateTimeEnd().isAfter(LocalDateTime.now()));
    }

    @Test
    void emptyPoll_ShouldInitializeWithEmptyCollections() {
        // Arrange
        Poll emptyPoll = new Poll();

        // Assert
        assertNotNull(emptyPoll.getOptions());
        assertNotNull(emptyPoll.getUsernames());
        assertTrue(emptyPoll.getOptions().isEmpty());
        assertTrue(emptyPoll.getUsernames().isEmpty());
    }
}
