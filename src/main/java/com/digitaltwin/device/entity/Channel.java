package com.digitaltwin.device.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "channel")
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "server_url", nullable = false)
    private String serverUrl;
    
    @Column(length = 500)
    private String description;
}