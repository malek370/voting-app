package com.voting.votingapp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voting.votingapp.DTO.VoteDTO;
import com.voting.votingapp.Exceptions.IlligalVote;
import com.voting.votingapp.Exceptions.OptionOrPollNotFound;
import com.voting.votingapp.Services.PollService;
import com.voting.votingapp.filter.JwtAuthentificationFilter;
import com.voting.votingapp.model.OptionVote;
import com.voting.votingapp.model.Poll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PollController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthentificationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class PollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PollService pollService;

    private Poll testPoll;
    private OptionVote option1;
    private OptionVote option2;
    private VoteDTO voteDTO;

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

        voteDTO = new VoteDTO();
        voteDTO.setPollId("poll123");
        voteDTO.setOptionIndex(0);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreatePoll() throws Exception {
        // Arrange
        when(pollService.createPoll(any(Poll.class))).thenReturn(testPoll);

        // Act & Assert
        mockMvc.perform(post("/api/polls")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPoll)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("poll123"))
                .andExpect(jsonPath("$.question").value("What is your favorite color?"))
                .andExpect(jsonPath("$.owner").value("testuser"));

        verify(pollService, times(1)).createPoll(any(Poll.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAllPolls_WithTextSearch() throws Exception {
        // Arrange
        List<Poll> polls = Arrays.asList(testPoll);
        when(pollService.getAllPolls("color")).thenReturn(polls);

        // Act & Assert
        mockMvc.perform(get("/api/polls")
                        .param("search", "color"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("poll123"))
                .andExpect(jsonPath("$[0].question").value("What is your favorite color?"));

        verify(pollService, times(1)).getAllPolls("color");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAllPolls_WithIDSearch() throws Exception {
        // Arrange
        when(pollService.getById("poll123")).thenReturn(Optional.of(testPoll));

        // Act & Assert
        mockMvc.perform(get("/api/polls")
                        .param("search", "ID:poll123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("poll123"));

        verify(pollService, times(1)).getById("poll123");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAllPolls_WithOwnerSearch() throws Exception {
        // Arrange
        List<Poll> polls = Arrays.asList(testPoll);
        when(pollService.getPollsByOwner("testuser")).thenReturn(polls);

        // Act & Assert
        mockMvc.perform(get("/api/polls")
                        .param("search", "OWNER:testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].owner").value("testuser"));

        verify(pollService, times(1)).getPollsByOwner("testuser");
    }

    @Test
    @WithMockUser(username = "voter1")
    void testGetAllPolls_FilterOutVotedPolls() throws Exception {
        // Arrange
        Poll votedPoll = new Poll();
        votedPoll.setId("poll456");
        votedPoll.setQuestion("Another poll");
        votedPoll.setUsernames(new ArrayList<>(Arrays.asList("voter1")));
        
        List<Poll> polls = new ArrayList<>(Arrays.asList(testPoll, votedPoll));
        when(pollService.getAllPolls("")).thenReturn(polls);

        // Act & Assert
        mockMvc.perform(get("/api/polls")
                        .param("search", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("poll123"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(pollService, times(1)).getAllPolls("");
    }

    @Test
    @WithMockUser(username = "voter1")
    void testGetAllPollsDoneByUser() throws Exception {
        // Arrange
        testPoll.getUsernames().add("voter1");
        List<Poll> allPolls = Arrays.asList(testPoll);
        when(pollService.getAllPolls("")).thenReturn(allPolls);

        // Act & Assert
        mockMvc.perform(get("/api/polls/done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("poll123"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(pollService, times(1)).getAllPolls("");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetAllPollsCreatedByUser() throws Exception {
        // Arrange
        List<Poll> userPolls = Arrays.asList(testPoll);
        when(pollService.getPollsByOwner("testuser")).thenReturn(userPolls);

        // Act & Assert
        mockMvc.perform(get("/api/polls/mypolls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("poll123"))
                .andExpect(jsonPath("$[0].owner").value("testuser"));

        verify(pollService, times(1)).getPollsByOwner("testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetPoll_Found() throws Exception {
        // Arrange
        when(pollService.getById("poll123")).thenReturn(Optional.of(testPoll));

        // Act & Assert
        mockMvc.perform(get("/api/polls/poll123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("poll123"))
                .andExpect(jsonPath("$.question").value("What is your favorite color?"));

        verify(pollService, times(1)).getById("poll123");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetPoll_NotFound() throws Exception {
        // Arrange
        when(pollService.getById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/polls/nonexistent"))
                .andExpect(status().isNotFound());

        verify(pollService, times(1)).getById("nonexistent");
    }

    @Test
    @WithMockUser(username = "voter1")
    void testVote_Success() throws Exception {
        // Arrange
        doNothing().when(pollService).vote(anyString(), anyInt(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/polls/vote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteDTO)))
                .andExpect(status().isNoContent());

        verify(pollService, times(1)).vote("poll123", 0, "voter1");
    }

    @Test
    @WithMockUser(username = "voter1")
    void testVote_IllegalVote() throws Exception {
        // Arrange
        doThrow(new IlligalVote("You already voted"))
                .when(pollService).vote(anyString(), anyInt(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/polls/vote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You already voted"));

        verify(pollService, times(1)).vote("poll123", 0, "voter1");
    }

    @Test
    @WithMockUser(username = "voter1")
    void testVote_OptionOrPollNotFound() throws Exception {
        // Arrange
        doThrow(new OptionOrPollNotFound("Poll not found"))
                .when(pollService).vote(anyString(), anyInt(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/polls/vote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Poll not found"));

        verify(pollService, times(1)).vote("poll123", 0, "voter1");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreatePoll_SetsOwner() throws Exception {
        // Arrange
        Poll pollWithoutOwner = new Poll();
        pollWithoutOwner.setQuestion("Test question");
        pollWithoutOwner.setOptions(new ArrayList<>(Arrays.asList(option1)));
        
        when(pollService.createPoll(any(Poll.class))).thenAnswer(invocation -> {
            Poll poll = invocation.getArgument(0);
            poll.setId("newpoll123");
            return poll;
        });

        // Act & Assert
        mockMvc.perform(post("/api/polls")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pollWithoutOwner)))
                .andExpect(status().isOk());

        verify(pollService, times(1)).createPoll(argThat(poll -> 
            "testuser".equals(poll.getOwner())
        ));
    }
}
