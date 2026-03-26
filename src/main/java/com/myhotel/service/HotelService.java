package com.myhotel.service;

import com.myhotel.dto.HotelDto;
import com.myhotel.dto.HotelUpdateDto;
import com.myhotel.entity.Hotel;

import java.util.List;

public interface HotelService {

    Hotel getHotelById(Long id);

    HotelDto convertToDto(Hotel hotel);

    List<HotelDto> getAllHotels();

    List<HotelDto> getHotelsByCity(String city);

    List<HotelDto> getHotelsByCountry(String country);

    HotelDto createHotel(HotelUpdateDto hotelDto);

    HotelDto updateHotel(Long id, HotelUpdateDto updateDto);

    void deleteHotel(Long id);
}