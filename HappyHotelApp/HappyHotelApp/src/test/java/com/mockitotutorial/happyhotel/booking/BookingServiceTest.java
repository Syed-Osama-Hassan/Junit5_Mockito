package com.mockitotutorial.happyhotel.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

class BookingServiceTest {
    private BookingService bookingService;
    private PaymentService paymentServiceMock;
    private RoomService roomServiceMock;
    private BookingDAO bookingDAOMock;
    private MailSender mailSenderMock;

    @BeforeEach
    void setup() {
        paymentServiceMock = mock(PaymentService.class);
        roomServiceMock = mock(RoomService.class);
        bookingDAOMock = mock(BookingDAO.class);
        mailSenderMock = mock(MailSender.class);
        bookingService = new BookingService(paymentServiceMock, roomServiceMock,
                bookingDAOMock, mailSenderMock);
    }

    @Test
    void shouldCount_AvailablePlaces_When_NoRoomsAvailable() {
        // Given
        int expected = 0;

        // When
        int actual = bookingService.getAvailablePlaceCount();

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void shouldCount_AvailablePlaces_When_MultipleRoomsAvailable() {
        // Given
        List<Room> rooms = Arrays.asList(new Room("Room 1", 5), new Room("Room 2", 10));
        when(roomServiceMock.getAvailableRooms()).thenReturn(rooms);
        int expected = rooms.stream().map(Room::getCapacity).reduce(0, Integer::sum);

        // When
        int actual = bookingService.getAvailablePlaceCount();

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void shouldCount_AvailablePlaces_When_MultipleTimeCalled() {
        // Given
        List<Room> rooms = Arrays.asList(new Room("Room 1", 5), new Room("Room 2", 10));
        when(roomServiceMock.getAvailableRooms()).thenReturn(rooms).thenReturn(Collections.emptyList());
        int expectedFirstCall = rooms.stream().map(Room::getCapacity).reduce(0, Integer::sum);
        int expectedSecondsCall = 0;

        // When
        int actualFirstCall = bookingService.getAvailablePlaceCount();
        int actualSecondCall = bookingService.getAvailablePlaceCount();

        //Then
        assertAll( () -> assertEquals(expectedFirstCall, actualFirstCall),
                () -> assertEquals(expectedSecondsCall, actualSecondCall)
                );

    }

    @Test
    void should_CalculateCorrectPrice_When_CorrectInput() {
        // Given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2022, 1, 1),
                LocalDate.of(2022, 1, 10), 10, false);
        double expectedPrice = 50.0 * bookingRequest.getGuestCount() * 9;

        // When
        double actual = bookingService.calculatePrice(bookingRequest);

        // Then
        assertEquals(expectedPrice, actual);
    }

    @Test
    void calculatePriceEuro() {
    }

    @Test
    void should_ThrowException_When_NoRoomAvailable() {
        // Given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2022, 1, 1),
                LocalDate.of(2022, 1, 10), 10, false);
        when(bookingService.makeBooking(bookingRequest)).thenThrow(BusinessException.class);

        // When
        Executable executable = () -> bookingService.makeBooking(bookingRequest);

        // Then
        assertThrows(BusinessException.class, executable);
    }

    @Test
    void should_NotCompleteBooking_When_PriceTooHigh() {
        // Given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2022, 1, 1),
                LocalDate.of(2022, 1, 10), 10, true);
        when(paymentServiceMock.pay(any(), anyDouble())).thenThrow(BusinessException.class);

        // When
        Executable executable = () -> bookingService.makeBooking(bookingRequest);

        // Then
        assertThrows(BusinessException.class, executable);
    }

    @Test
    void should_InvokePaymentService_When_Prepaid() {
        // Given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2022, 1, 1),
                LocalDate.of(2022, 1, 10), 10, true);

        // When
        bookingService.makeBooking(bookingRequest);

        // Then
        verify(paymentServiceMock, times(1)).pay(bookingRequest, 4500.0);
        verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    void should_NotInvokePaymentService_When_NotPrepaid() {
        // Given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2022, 1, 1),
                LocalDate.of(2022, 1, 10), 10, false);

        // When
        bookingService.makeBooking(bookingRequest);

        // Then
        verify(paymentServiceMock, never()).pay(any(), anyDouble());
    }

    @Test
    void cancelBooking() {
    }

}