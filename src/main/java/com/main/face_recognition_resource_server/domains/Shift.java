package com.main.face_recognition_resource_server.domains;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "shifts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_shift_name_organization",
                        columnNames = {"name", "organization_id"}
                )
        })
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shift_id_generator")
    @SequenceGenerator(name = "shift_id_generator", sequenceName = "shift_id_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String checkInTime;

    @Column(nullable = false)
    private String checkOutTime;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @OneToMany(mappedBy = "userShift")
    private List<User> usersInShift;

    @Column(nullable = false)
    private boolean isDefault;

    @Column(nullable = false)
    private boolean isSavedInProducer;

    private Date lastSavedInProducerDate;
}
