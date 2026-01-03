package com.voting.votingapp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voting.votingapp.DTO.VoteDTO;
import com.voting.votingapp.Exceptions.IlligalVote;
import com.voting.votingapp.Exceptions.OptionOrPollNotFound;
import com.voting.votingapp.Services.PollService;
import com.voting.votingapp.model.OptionVote;
import com.voting.votingapp.model.Poll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(PollController.class)
@AutoConfigureMockMvc(addFilters = false)
class PollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PollService pollService;

    private Poll testPoll;
    private VoteDTO voteDTO;

    @BeforeEach
    void setUp() {
        OptionVote option1 = new OptionVote();
        option1.setVoteOption("Option 1");
        option1.setVoteCount(0L);

        OptionVote option2 = new OptionVote();
        option2.setVoteOption("Option 2");
        option2.setVoteCount(0L);

        testPoll = new Poll();
        testPoll.setId("poll-id");
        testPoll.setQuestion("Test Question?");
        testPoll.setOptions(new ArrayList<>(Arrays.asList(option1, option2)));
        testPoll.setUsernames(new ArrayList<>());
        testPoll.setOwner("testowner");
        testPoll.setDateTimeEnd(LocalDateTime.now().plusDays(1));

        voteDTO = new VoteDTO();
        voteDTO.setPollId("poll-id");
        voteDTO.setOptionIndex(0);
    }

    @Test
    @WithMockUser(username = "testuser")
    void createPoll_ShouldReturnCreatedPoll() throws Exception {
        // Arrange
        when(pollService.createPoll(any(Poll.class))).thenReturn(testPoll);

        // Act & Assert
        mockMvc.perform(post("/api/polls")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPoll)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("poll-id"))
                .andExpect(jsonPath("$.question").value("Test Question?"));

        verify(pollService).createPoll(any(Poll.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAllPolls_WithSearchParam_ShouldReturnPolls() throws Exception {
        // Arrange
        List<Poll> polls = Arrays.asList(testPoll);
        when(pollService.getAllPolls("test")).thenReturn(polls);

        // Act & Assert
        mockMvc.perform(get("/api/polls")
                        .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("poll-id"))
                .andExpect(jsonPath("$[0].question").value("Test Question?"));

        verify(pollService).getAllPolls("test");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAllPolls_WithIdSearch_ShouldReturnSpecificPoll() throws Exception {
        // Arrange
        when(pollService.getById("poll-id")).thenReturn(Optional.of(testPoll));

        // Act & Assert
        mockMvc.perform(get("/api/polls")
                        .param("search", "ID:poll-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("poll-id"));

        verify(pollService).getById("poll-id");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAllPolls_WithOwnerSearch_ShouldReturnOwnerPolls() throws Exception {
        // Arrange
        List<Poll> polls = Arrays.asList(testPoll);
        when(pollService.getPollsByOwner("testowner")).thenReturn(polls);

        // Act & Assert
        mockMvc.perform(get("/api/polls")
                        .param("search", "OWNER:testowner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].owner").value("testowner"));

        verify(pollService).getPollsByOwner("testowner");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAllPollsDoneByUser_ShouldReturnVotedPolls() throws Exception {
        // Arrange
        testPoll.getUsernames().add("testuser");
        List<Poll> polls = Arrays.asList(testPoll);
        when(pollService.getAllPolls("")).thenReturn(polls);

        // Act & Assert
        mockMvc.perform(get("/api/polls/done"))
                .andExpect(status().isOk());

        verify(pollService).getAllPolls("");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAllPollsCreatedByUser_ShouldReturnUserPolls() throws Exception {
        // Arrange
        List<Poll> polls = Arrays.asList(testPoll);
        when(pollService.getPollsByOwner("testuser")).thenReturn(polls);

        // Act & Assert
        mockMvc.perform(get("/api/polls/mypolls"))
                .andExpect(status().isOk());

        verify(pollService).getPollsByOwner("testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getPoll_WithValidId_ShouldReturnPoll() throws Exception {
        // Arrange
        when(pollService.getById("poll-id")).thenReturn(Optional.of(testPoll));

        // Act & Assert
        mockMvc.perform(get("/api/polls/poll-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("poll-id"));

        verify(pollService).getById("poll-id");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getPoll_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(pollService.getById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/polls/invalid-id"))
                .andExpect(status().isNotFound());

        verify(pollService).getById("invalid-id");
    }

    @Test
    @WithMockUser(username = "testuser")
    void vote_WithValidData_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(pollService).vote(anyString(), anyInt(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/polls/vote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteDTO)))
                .andExpect(status().isNoContent());

        verify(pollService).vote("poll-id", 0, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void vote_WhenAlreadyVoted_ShouldReturnBadRequest() throws Exception {
        // Arrange
        doThrow(new IlligalVote("You already voted"))
                .when(pollService).vote(anyString(), anyInt(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/polls/vote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteDTO)))
                .andExpect(status().isBadRequest());

        verify(pollService).vote("poll-id", 0, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void vote_WithInvalidPoll_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new OptionOrPollNotFound("Poll not found"))
                .when(pollService).vote(anyString(), anyInt(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/polls/vote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteDTO)))
                .andExpect(status().isNotFound());

        verify(pollService).vote("poll-id", 0, "testuser");
    }
}
