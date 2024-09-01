//package com.hit11.zeus.model.recon
//
//import java.time.Instant
//import javax.persistence.Column
//import javax.persistence.Entity
//import javax.persistence.GeneratedValue
//import javax.persistence.GenerationType
//import javax.persistence.Id
//import javax.persistence.PrePersist
//import javax.persistence.PreUpdate
//import javax.persistence.Table
//
//
//data class  ReconEvent(
//    val eventType: String
//)
//@Entity
//@Table(name = "recon_event")
//data class ReconEventEntity(
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    val id: Int = 0,
//    val eventType: String,
//    @Column(name = "created_at", nullable = false, updatable = false)
//    var createdAt: Instant = Instant.now(),
//    @Column(name = "updated_at", nullable = false)
//    var updatedAt: Instant = Instant.now()
//) {
//    @PrePersist
//    fun prePersist() {
//        val now = Instant.now()
//        createdAt = now
//        updatedAt = now
//    }
//
//    @PreUpdate
//    fun preUpdate() {
//        updatedAt = Instant.now()
//    }
//}