import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TicketServiceImplTest {

    private TicketPaymentService paymentService;
    private SeatReservationService seatReservationService;
    private TicketService ticketService;

    @BeforeEach
    public void setUp() {
        paymentService = Mockito.mock(TicketPaymentService.class);
        seatReservationService = Mockito.mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(paymentService, seatReservationService);
    }

    @Test
    public void shouldCalculateTotalCostForAdultChildAndInfant() {
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest infantTicket = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        ticketService.purchaseTickets(1L, adultTicket, childTicket, infantTicket);

        verify(paymentService).makePayment(1L, 65);
        verify(seatReservationService).reserveSeat(1L, 3);
    }

    @Test
    public void shouldCalculateSeatsCorrectly() {
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3);
        TicketTypeRequest infantTicket = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);

        ticketService.purchaseTickets(1L, adultTicket, childTicket, infantTicket);

        verify(seatReservationService).reserveSeat(1L, 5);
    }

    @Test
    public void shouldThrowExceptionWhenNoAdultTickets() {
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);

        InvalidPurchaseException thrown = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, childTicket),
                "Expected InvalidPurchaseException"
        );

        assertTrue(thrown.getMessage().contains("Child or Infant tickets cannot be purchased without an Adult ticket"));
    }

    @Test
    public void shouldThrowExceptionWhenMoreThan25Tickets() {
        TicketTypeRequest adultTickets = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26);

        InvalidPurchaseException thrown = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, adultTickets),
                "Expected InvalidPurchaseException"
        );

        assertTrue(thrown.getMessage().contains("Cannot purchase more than 25 tickets at a time"));
    }

    @Test
    public void shouldThrowExceptionForInvalidAccountId() {
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);

        InvalidPurchaseException thrown = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(0L, adultTicket),
                "Expected InvalidPurchaseException"
        );

        assertTrue(thrown.getMessage().contains("Invalid account or no tickets requested"));
    }

    @Test
    public void shouldThrowExceptionForEmptyTicketRequest() {
        InvalidPurchaseException thrown = assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L),
                "Expected InvalidPurchaseException"
        );

        assertTrue(thrown.getMessage().contains("Invalid account or no tickets requested"));
    }

}