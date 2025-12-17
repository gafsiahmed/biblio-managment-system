package com.bibliotheque.service;

import com.bibliotheque.model.Reservation;
import com.bibliotheque.model.Resource;
import com.bibliotheque.model.User;
import com.bibliotheque.model.enums.ReservationStatus;
import com.bibliotheque.repository.ReservationRepository;
import com.bibliotheque.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final EmailService emailService;

    @Transactional
    public Reservation createReservation(User user, Resource resource) {
        // Check if user already has an active reservation
        reservationRepository.findByUserAndResourceAndStatusIn(user, resource, 
                List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED))
            .ifPresent(r -> {
                throw new IllegalStateException("Vous avez déjà une réservation active pour cette ressource.");
            });

        // If copies are available, we shouldn't be here (logic should be handled by caller), 
        // but if we are, we queue anyway or throw? 
        // User requirement: "Si availableCopies = 0 -> reservation en file d'attente".
        // We assume this method is called when copies = 0.

        long count = reservationRepository.countByResourceAndStatus(resource, ReservationStatus.PENDING);
        
        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .reservationNumber(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .reservationDate(LocalDateTime.now())
                .status(ReservationStatus.PENDING)
                .positionInQueue((int) count + 1)
                .build();

        Reservation saved = reservationRepository.save(reservation);

        // Notify User
        emailService.sendReservationConfirmation(
                user.getEmail(), 
                user.getFirstName(), 
                resource.getTitle(), 
                saved.getReservationNumber(), 
                saved.getPositionInQueue()
        );

        return saved;
    }

    @Transactional
    public void processReturn(Resource resource) {
        List<Reservation> queue = reservationRepository.findByResourceAndStatusOrderByReservationDateAsc(resource, ReservationStatus.PENDING);
        
        if (!queue.isEmpty()) {
            Reservation next = queue.get(0);
            
            next.setStatus(ReservationStatus.APPROVED);
            next.setNotificationSentDate(LocalDateTime.now());
            next.setExpiryDate(LocalDateTime.now().plusHours(48)); 
            
            reservationRepository.save(next);

            emailService.sendReservationAvailable(
                    next.getUser().getEmail(), 
                    next.getUser().getFirstName(), 
                    resource.getTitle(), 
                    next.getExpiryDate().toString() 
            );
            
            resource.setAvailableCopies(resource.getAvailableCopies() - 1);
            resourceRepository.save(resource);
            
            updateQueuePositions(resource);
        }
    }

    private void updateQueuePositions(Resource resource) {
        List<Reservation> queue = reservationRepository.findByResourceAndStatusOrderByReservationDateAsc(resource, ReservationStatus.PENDING);
        for (int i = 0; i < queue.size(); i++) {
            Reservation r = queue.get(i);
            if (r.getPositionInQueue() != i + 1) {
                r.setPositionInQueue(i + 1);
                reservationRepository.save(r);
            }
        }
    }
}
