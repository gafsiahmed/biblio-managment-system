package com.bibliotheque.repository;

import com.bibliotheque.model.Reservation;
import com.bibliotheque.model.Resource;
import com.bibliotheque.model.User;
import com.bibliotheque.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    Optional<Reservation> findByUserAndResourceAndStatusIn(User user, Resource resource, List<ReservationStatus> statuses);
    
    List<Reservation> findByResourceAndStatusOrderByReservationDateAsc(Resource resource, ReservationStatus status);
    
    long countByResourceAndStatus(Resource resource, ReservationStatus status);

    List<Reservation> findByUser(User user);

    List<Reservation> findByStatus(ReservationStatus status);
}
