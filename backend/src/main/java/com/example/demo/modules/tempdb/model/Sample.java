
package com.example.demo.modules.tempdb.model;
import jakarta.persistence.*;
import lombok.Data;
import com.example.demo.modules.requests.model.Requests;

@Data
@Entity
@Table(name = "sample")
public class Sample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Requests request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private WIPbatch batch;

    @Column(nullable = false, unique = true)
    private String barcode;

    @Column(nullable = false)
    private String status;
}
