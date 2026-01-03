package com.voting.votingapp.Services;

import com.voting.votingapp.Exceptions.IlligalVote;
import com.voting.votingapp.Exceptions.OptionOrPollNotFound;
import com.voting.votingapp.model.OptionVote;
import com.voting.votingapp.model.Poll;
import com.voting.votingapp.repositories.PollRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PollServiceTest {

    @Mock
    private PollRepository pollRepository;

    @InjectMocks
    private PollService pollService;

    private Poll testPoll;
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

        testPoll = new Poll();
        testPoll.setId("poll-id");
        testPoll.setQuestion("Test Question?");
        testPoll.setOptions(new ArrayList<>(Arrays.asList(option1, option2)));
        testPoll.setUsernames(new ArrayList<>());
        testPoll.setOwner("testowner");
        testPoll.setDateTimeEnd(LocalDateTime.now().plusDays(1));
    }

    @Test
    void createPoll_ShouldReturnSavedPoll() {
        // Arrange
        when(pollRepository.save(any(Poll.class))).thenReturn(testPoll);

        // Act
        Poll result = pollService.createPoll(testPoll);

        // Assert
        assertNotNull(result);
        assertEquals("Test Question?", result.getQuestion());
        assertEquals("poll-id", result.getId());
        verify(pollRepository).save(testPoll);
    }

    @Test
    void getAllPolls_WithSearchQuery_ShouldReturnMatchingPolls() {
        // Arrange
        List<Poll> polls = Arrays.asList(testPoll);
        when(pollRepository.findByQuestionContainingIgnoreCase("Test")).thenReturn(polls);

        // Act
        List<Poll> result = pollService.getAllPolls("Test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Question?", result.get(0).getQuestion());
        verify(pollRepository).findByQuestionContainingIgnoreCase("Test");
    }

    @Test
    void getById_WithValidId_ShouldReturnPoll() {
        // Arrange
        when(pollRepository.findById("poll-id")).thenReturn(Optional.of(testPoll));

        // Act
        Optional<Poll> result = pollService.getById("poll-id");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("poll-id", result.get().getId());
        verify(pollRepository).findById("poll-id");
    }

    @Test
    void getById_WithInvalidId_ShouldReturnEmpty() {
        // Arrange
        when(pollRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act
        Optional<Poll> result = pollService.getById("invalid-id");

        // Assert
        assertFalse(result.isPresent());
        verify(pollRepository).findById("invalid-id");
    }

    @Test
    void getPollsByOwner_ShouldReturnOwnersPolls() {
        // Arrange
        List<Poll> polls = Arrays.asList(testPoll);
        when(pollRepository.findByowner("testowner")).thenReturn(polls);

        // Act
        List<Poll> result = pollService.getPollsByOwner("testowner");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testowner", result.get(0).getOwner());
        verify(pollRepository).findByowner("testowner");
    }

    @Test
    void vote_WithValidData_ShouldIncreaseVoteCount() throws OptionOrPollNotFound, IlligalVote {
        // Arrange
        when(pollRepository.findById("poll-id")).thenReturn(Optional.of(testPoll));
        when(pollRepository.save(any(Poll.class))).thenReturn(testPoll);

        // Act
        pollService.vote("poll-id", 0, "voter1");

        // Assert
        verify(pollRepository).findById("poll-id");
        verify(pollRepository).save(testPoll);
        assertEquals(1L, testPoll.getOptions().get(0).getVoteCount());
        assertTrue(testPoll.getUsernames().contains("voter1"));
    }

    @Test
    void vote_WithNullPollId_ShouldThrowOptionOrPollNotFound() {
        // Act & Assert
        OptionOrPollNotFound exception = assertThrows(OptionOrPollNotFound.class,
                () -> pollService.vote(null, 0, "voter1"));
        assertEquals("Poll id is null", exception.getMessage());
        verify(pollRepository, never()).save(any(Poll.class));
    }

    @Test
    void vote_WithInvalidPollId_ShouldThrowOptionOrPollNotFound() {
        // Arrange
        when(pollRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        OptionOrPollNotFound exception = assertThrows(OptionOrPollNotFound.class,
                () -> pollService.vote("invalid-id", 0, "voter1"));
        assertEquals("Poll not found", exception.getMessage());
        verify(pollRepository).findById("invalid-id");
        verify(pollRepository, never()).save(any(Poll.class));
    }

    @Test
    void vote_WithInvalidOptionIndex_ShouldThrowOptionOrPollNotFound() {
        // Arrange
        when(pollRepository.findById("poll-id")).thenReturn(Optional.of(testPoll));

        // Act & Assert
        OptionOrPollNotFound exception = assertThrows(OptionOrPollNotFound.class,
                () -> pollService.vote("poll-id", 10, "voter1"));
        assertEquals("Option not found", exception.getMessage());
        verify(pollRepository).findById("poll-id");
        verify(pollRepository, never()).save(any(Poll.class));
    }

    @Test
    void vote_WhenUserAlreadyVoted_ShouldThrowIlligalVote() {
        // Arrange
        testPoll.getUsernames().add("voter1");
        when(pollRepository.findById("poll-id")).thenReturn(Optional.of(testPoll));

        // Act & Assert
        IlligalVote exception = assertThrows(IlligalVote.class,
                () -> pollService.vote("poll-id", 0, "voter1"));
        assertEquals("You already voted", exception.getMessage());
        verify(pollRepository).findById("poll-id");
        verify(pollRepository, never()).save(any(Poll.class));
    }

    @Test
    void vote_MultipleUsersVoting_ShouldIncrementCorrectly() throws OptionOrPollNotFound, IlligalVote {
        // Arrange
        when(pollRepository.findById("poll-id")).thenReturn(Optional.of(testPoll));
        when(pollRepository.save(any(Poll.class))).thenReturn(testPoll);

        // Act
        pollService.vote("poll-id", 0, "voter1");
        testPoll = pollRepository.save(testPoll); // Simulate repository save
        
        when(pollRepository.findById("poll-id")).thenReturn(Optional.of(testPoll));
        pollService.vote("poll-id", 1, "voter2");

        // Assert
        verify(pollRepository, atLeast(2)).findById("poll-id");
        verify(pollRepository, atLeast(2)).save(testPoll);
    }
}
