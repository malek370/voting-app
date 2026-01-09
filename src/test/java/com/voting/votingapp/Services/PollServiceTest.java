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
        testPoll.setId("poll123");
        testPoll.setQuestion("What is your favorite color?");
        testPoll.setOptions(new ArrayList<>(Arrays.asList(option1, option2)));
        testPoll.setOwner("testuser");
        testPoll.setUsernames(new ArrayList<>());
        testPoll.setDateTimeEnd(LocalDateTime.now().plusDays(7));
        testPoll.setAllowMultipleVote(false);
    }

    @Test
    void testCreatePoll() {
        // Arrange
        when(pollRepository.save(any(Poll.class))).thenReturn(testPoll);

        // Act
        Poll result = pollService.createPoll(testPoll);

        // Assert
        assertNotNull(result);
        assertEquals("poll123", result.getId());
        assertEquals("What is your favorite color?", result.getQuestion());
        verify(pollRepository, times(1)).save(testPoll);
    }

    @Test
    void testGetAllPolls() {
        // Arrange
        String searchTerm = "favorite";
        List<Poll> expectedPolls = Arrays.asList(testPoll);
        when(pollRepository.findByQuestionContainingIgnoreCase(searchTerm)).thenReturn(expectedPolls);

        // Act
        List<Poll> result = pollService.getAllPolls(searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPoll.getQuestion(), result.get(0).getQuestion());
        verify(pollRepository, times(1)).findByQuestionContainingIgnoreCase(searchTerm);
    }

    @Test
    void testGetAllPollsWithEmptySearch() {
        // Arrange
        List<Poll> expectedPolls = Arrays.asList(testPoll);
        when(pollRepository.findByQuestionContainingIgnoreCase("")).thenReturn(expectedPolls);

        // Act
        List<Poll> result = pollService.getAllPolls("");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pollRepository, times(1)).findByQuestionContainingIgnoreCase("");
    }

    @Test
    void testGetById_Found() {
        // Arrange
        when(pollRepository.findById("poll123")).thenReturn(Optional.of(testPoll));

        // Act
        Optional<Poll> result = pollService.getById("poll123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("poll123", result.get().getId());
        verify(pollRepository, times(1)).findById("poll123");
    }

    @Test
    void testGetById_NotFound() {
        // Arrange
        when(pollRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<Poll> result = pollService.getById("nonexistent");

        // Assert
        assertFalse(result.isPresent());
        verify(pollRepository, times(1)).findById("nonexistent");
    }

    @Test
    void testGetPollsByOwner() {
        // Arrange
        String owner = "testuser";
        List<Poll> expectedPolls = Arrays.asList(testPoll);
        when(pollRepository.findByowner(owner)).thenReturn(expectedPolls);

        // Act
        List<Poll> result = pollService.getPollsByOwner(owner);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(owner, result.get(0).getOwner());
        verify(pollRepository, times(1)).findByowner(owner);
    }

    @Test
    void testVote_Success() throws OptionOrPollNotFound, IlligalVote {
        // Arrange
        String pollId = "poll123";
        int optionIndex = 0;
        String username = "voter1";
        when(pollRepository.findById(pollId)).thenReturn(Optional.of(testPoll));
        when(pollRepository.save(any(Poll.class))).thenReturn(testPoll);

        // Act
        pollService.vote(pollId, optionIndex, username);

        // Assert
        assertEquals(1L, testPoll.getOptions().get(0).getVoteCount());
        assertTrue(testPoll.getUsernames().contains(username));
        verify(pollRepository, times(1)).findById(pollId);
        verify(pollRepository, times(1)).save(testPoll);
    }

    @Test
    void testVote_PollIdNull() {
        // Act & Assert
        assertThrows(OptionOrPollNotFound.class, () -> {
            pollService.vote(null, 0, "voter1");
        });
    }

    @Test
    void testVote_PollNotFound() {
        // Arrange
        when(pollRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OptionOrPollNotFound.class, () -> {
            pollService.vote("nonexistent", 0, "voter1");
        });
        verify(pollRepository, times(1)).findById("nonexistent");
    }

    @Test
    void testVote_OptionNotFound() {
        // Arrange
        when(pollRepository.findById("poll123")).thenReturn(Optional.of(testPoll));

        // Act & Assert
        assertThrows(OptionOrPollNotFound.class, () -> {
            pollService.vote("poll123", 10, "voter1");
        });
        verify(pollRepository, times(1)).findById("poll123");
        verify(pollRepository, never()).save(any());
    }

    @Test
    void testVote_UserAlreadyVoted() {
        // Arrange
        String username = "voter1";
        testPoll.getUsernames().add(username);
        when(pollRepository.findById("poll123")).thenReturn(Optional.of(testPoll));

        // Act & Assert
        assertThrows(IlligalVote.class, () -> {
            pollService.vote("poll123", 0, username);
        });
        verify(pollRepository, times(1)).findById("poll123");
        verify(pollRepository, never()).save(any());
    }

    @Test
    void testVote_MultipleUsers() throws OptionOrPollNotFound, IlligalVote {
        // Arrange
        when(pollRepository.findById("poll123")).thenReturn(Optional.of(testPoll));
        when(pollRepository.save(any(Poll.class))).thenReturn(testPoll);

        // Act
        pollService.vote("poll123", 0, "voter1");
        
        // Reset mock for second vote
        testPoll.setUsernames(new ArrayList<>(Arrays.asList("voter1")));
        when(pollRepository.findById("poll123")).thenReturn(Optional.of(testPoll));
        
        pollService.vote("poll123", 1, "voter2");

        // Assert
        assertTrue(testPoll.getUsernames().contains("voter1"));
        assertTrue(testPoll.getUsernames().contains("voter2"));
        verify(pollRepository, times(2)).findById("poll123");
        verify(pollRepository, times(2)).save(testPoll);
    }
}
