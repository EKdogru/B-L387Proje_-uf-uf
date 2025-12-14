package com.cufcuf.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "seats")
public class Seat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "wagon_id")
    private Long wagonId;

    @Column(name = "seat_number")
    private Integer seatNumber;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @Column(name = "seat_type")
    private String seatType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // GETTER ve SETTER
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWagonId() { return wagonId; }
    public void setWagonId(Long wagonId) { this.wagonId = wagonId; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public String getSeatType() { return seatType; }
    public void setSeatType(String seatType) { this.seatType = seatType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}