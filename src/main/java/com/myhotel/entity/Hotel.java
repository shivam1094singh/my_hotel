package com.myhotel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "hotel")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String city;

    private String country;

    private String phoneNumber;

    private String email;

    private BigDecimal rating;

    private String description;

    @OneToMany(mappedBy = "hotel",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<Room> rooms;

}
