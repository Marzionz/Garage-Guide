package com.TeamOne411.backend.service;

import com.TeamOne411.backend.entity.Garage;
import com.TeamOne411.backend.entity.schedule.GarageCalendar;
import com.TeamOne411.backend.repository.GarageCalendarRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Back-end service interface for managing the Garage Calendar
 */
@Service
public class GarageCalendarService {

    private final GarageCalendarRepository garageCalendarRepository;

    public GarageCalendarService(GarageCalendarRepository garageScheduleRepository) {
        this.garageCalendarRepository = garageScheduleRepository;
    }

    public void saveGarageCalendar(GarageCalendar garageCalendar){
        garageCalendarRepository.save(garageCalendar);
    }

    public void deleteGarageCalendar(GarageCalendar garageCalendar){
        garageCalendarRepository.delete(garageCalendar);
    }

    public GarageCalendar findByGarage(Garage garage){
        return garageCalendarRepository.findByGarage(garage);
    }

}
