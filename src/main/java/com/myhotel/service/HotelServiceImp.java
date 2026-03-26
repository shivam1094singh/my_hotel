package com.myhotel.service;

import com.myhotel.dto.HotelDto;
import com.myhotel.dto.HotelUpdateDto;
import com.myhotel.entity.Hotel;
import com.myhotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImp implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    @Override
    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));
    }


    @Override
    public HotelDto convertToDto(Hotel hotel) {
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        List<Hotel> hotels = hotelRepository.findAll();
        return hotels.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<HotelDto> getHotelsByCity(String city) {
        List<Hotel> hotels = hotelRepository.findByCity(city);
        return hotels.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<HotelDto> getHotelsByCountry(String country) {
        List<Hotel> hotels = hotelRepository.findByCountry(country);
        return hotels.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public HotelDto createHotel(HotelUpdateDto hotelDto) {
        if(hotelRepository.existsByName(hotelDto.getName())){
            throw new RuntimeException(("Hotel already exists with this name:"+ hotelDto.getName()));
        }

        Hotel hotel = new Hotel();
        hotel.setName(hotelDto.getName());
        hotel.setAddress(hotelDto.getAddress());
        hotel.setCity(hotelDto.getCity());
        hotel.setEmail(hotelDto.getEmail());
        hotel.setCountry(hotelDto.getCountry());
        hotel.setDescription(hotelDto.getDescription());
        hotel.setRating(hotelDto.getRating());
        hotel.setPhoneNumber(hotelDto.getPhoneNumber());

        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("Hotel created successfully with name {}:",savedHotel);
        return convertToDto(savedHotel);
    }


    @Override
    public HotelDto updateHotel(Long id, HotelUpdateDto updateDto) {
        Hotel hotel = getHotelById(id);


        if (updateDto.getName() != null && !updateDto.getName().equals(hotel.getName())) {
            if (hotelRepository.existsByName(updateDto.getName())) {
                throw new RuntimeException("Hotel already exists with name: " + updateDto.getName());
            }
            hotel.setName(updateDto.getName());
        }

        if (updateDto.getAddress() != null) {
            hotel.setAddress(updateDto.getAddress());
        }

        if (updateDto.getCity() != null) {
            hotel.setCity(updateDto.getCity());
        }

        if (updateDto.getCountry() != null) {
            hotel.setCountry(updateDto.getCountry());
        }

        if (updateDto.getPhoneNumber() != null) {
            hotel.setPhoneNumber(updateDto.getPhoneNumber());
        }

        if (updateDto.getEmail() != null) {
            hotel.setEmail(updateDto.getEmail());
        }

        if (updateDto.getRating() != null) {
            hotel.setRating(updateDto.getRating());
        }

        if (updateDto.getDescription() != null) {
            hotel.setDescription(updateDto.getDescription());
        }

        Hotel updatedHotel = hotelRepository.save(hotel);
        log.info("Hotel updated with name: {}", updatedHotel.getName());
        return convertToDto(updatedHotel);
    }

    @Override
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)){
            throw new RuntimeException("Hotel not found with id: "+id);
        }
        hotelRepository.deleteById(id);
        log.info("Hotel is deleted with id:{}",id);
    }

}


