package com.cufcuf.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "wagons")
public class Wagon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "wagon_number")
    private Integer wagonNumber;

    @Column(name = "wagon_type")
    private String wagonType;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // GETTER ve SETTER
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public Integer getWagonNumber() { return wagonNumber; }
    public void setWagonNumber(Integer wagonNumber) { this.wagonNumber = wagonNumber; }

    public String getWagonType() { return wagonType; }
    public void setWagonType(String wagonType) { this.wagonType = wagonType; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}