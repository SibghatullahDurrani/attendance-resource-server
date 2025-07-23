package com.main.face_recognition_resource_server.domains;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shifts")
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shift_id_generator")
    @SequenceGenerator(name = "shift_id_generator", sequenceName = "shift_id_sequence", allocationSize = 1)
    private Long id;

    private String name;

    private String checkInTime;

    private String checkOutTime;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @OneToMany(mappedBy = "userShift")
    private List<User> usersInShift;
}
