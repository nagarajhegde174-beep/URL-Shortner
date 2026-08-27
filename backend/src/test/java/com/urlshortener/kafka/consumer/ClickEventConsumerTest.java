package com.urlshortener.kafka.consumer;

import com.urlshortener.analytics.dto.ClickEventDto;
import com.urlshortener.analytics.entity.Click;
import com.urlshortener.analytics.repository.ClickRepository;
import com.urlshortener.url.entity.Link;
import com.urlshortener.url.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClickEventConsumerTest {

    @Mock
    private ClickRepository clickRepository;
    @Mock
    private LinkRepository linkRepository;
    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private ClickEventConsumer clickEventConsumer;

    private ClickEventDto event;
    private Link link;

    @BeforeEach
    public void setUp() {
        event = ClickEventDto.builder()
                .eventId("event-uuid-123")
                .linkId(10L)
                .referrer("https://google.com")
                .userAgent("Mozilla/5.0 Chrome")
                .ipHash("64-hex-chars-sha256-hash-value-1234567890abcdef1234567890abcdef")
                .clickedAt(Instant.now())
                .build();

        link = Link.builder()
                .id(10L)
                .shortCode("abc123")
                .longUrl("https://example.com")
                .build();
    }

    @Test
    public void testConsume_Success() {
        when(linkRepository.findById(10L)).thenReturn(Optional.of(link));
        when(clickRepository.existsByLinkAndIpHashAndClickedAt(any(Link.class), anyString(), any(Instant.class)))
                .thenReturn(false);

        clickEventConsumer.consume(event, acknowledgment);

        verify(clickRepository).save(any(Click.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    public void testConsume_Duplicate_SkipsSave_Acknowledges() {
        when(linkRepository.findById(10L)).thenReturn(Optional.of(link));
        // Mock duplicate detected
        when(clickRepository.existsByLinkAndIpHashAndClickedAt(any(Link.class), anyString(), any(Instant.class)))
                .thenReturn(true);

        clickEventConsumer.consume(event, acknowledgment);

        verify(clickRepository, never()).save(any(Click.class));
        // Still acknowledges so message is not retried forever
        verify(acknowledgment).acknowledge();
    }

    @Test
    public void testConsume_DbFailure_DoesNotAcknowledge() {
        when(linkRepository.findById(10L)).thenReturn(Optional.of(link));
        when(clickRepository.existsByLinkAndIpHashAndClickedAt(any(Link.class), anyString(), any(Instant.class)))
                .thenReturn(false);

        // Database throw exception on save
        doThrow(new RuntimeException("DB offline")).when(clickRepository).save(any(Click.class));

        assertThrows(RuntimeException.class, () -> clickEventConsumer.consume(event, acknowledgment));

        // Verify save was attempted
        verify(clickRepository).save(any(Click.class));
        // Verify acknowledgment was NEVER called
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    public void testConsume_LinkNotFound_Acknowledges() {
        // Link deleted or not found
        when(linkRepository.findById(10L)).thenReturn(Optional.empty());

        clickEventConsumer.consume(event, acknowledgment);

        verify(clickRepository, never()).save(any(Click.class));
        // Acknowledge to prevent infinite retries for a garbage/non-existent link
        verify(acknowledgment).acknowledge();
    }
}
