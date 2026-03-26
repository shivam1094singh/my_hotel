package com.myhotel.repository;

import com.myhotel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    
    Optional<Hotel> findByName(String name);
    
    List<Hotel> findByCity(String city);

    List<Hotel> findByCountry(String country);

    boolean existsByName(String name);
}
