package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService paymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService seatReservationService) {
        this.paymentService = paymentService;
        this.seatReservationService = seatReservationService;
    }

    /**
     * Should only have private methods other than the one below.
     */

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if(accountId <= 0 || ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("Invalid account or no tickets requested.");
        }

        int totalTickets = 0;
        int adultTickets = 0;
        int totalCost = 0;
        int seatsToReserve = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            totalCost += switch (request.getTicketType()) {
                case ADULT -> {
                    seatsToReserve += request.getNoOfTickets();
                    adultTickets += request.getNoOfTickets();
                    yield request.getNoOfTickets() * 25;
                }
                case CHILD -> {
                    seatsToReserve += request.getNoOfTickets();
                    yield request.getNoOfTickets() * 15;
                }
                case INFANT -> 0;
            };
            totalTickets += request.getNoOfTickets();
        }

        if (adultTickets == 0) {
            throw new InvalidPurchaseException("Child or Infant tickets cannot be purchased without an Adult ticket.");
        }

        if (totalTickets > 25) {
            throw new InvalidPurchaseException("Cannot purchase more than 25 tickets at a time.");
        }

        paymentService.makePayment(accountId, totalCost);
        seatReservationService.reserveSeat(accountId, seatsToReserve);
    }
}
