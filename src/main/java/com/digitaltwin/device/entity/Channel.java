package com.digitaltwin.device.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "channel")
@ToString(exclude = {"devices"})  // 排除devices字段避免toString循环引用
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

    @JsonIgnore
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Device> devices;
    
//    @JsonIgnore
//    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<Point> points;

    @Column(length = 2000)
    private String opcUaConfig;
}