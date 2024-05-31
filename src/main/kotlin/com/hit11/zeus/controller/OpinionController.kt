package com.obsidian.warhammer.controller

import com.obsidian.warhammer.repository.model.OpinionDataModel
import com.obsidian.warhammer.service.OpinionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/opinion")
class OpinionController(private val service: OpinionService) {

    @GetMapping("/active")
    fun getAllOpinions(): List<OpinionDataModel> = service.getAllOpinions()

    @GetMapping("/{id}")
    fun getOpinionById(@PathVariable id: Int): ResponseEntity<OpinionDataModel> {
        val opinion = service.getOpinionById(id)
        return if (opinion != null) {
            ResponseEntity.ok(opinion)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createOpinion(@RequestBody opinion: OpinionDataModel): ResponseEntity<OpinionDataModel> {
        val savedOpinion = service.saveOpinion(opinion)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOpinion)
    }

    @PutMapping("/{id}")
    fun updateOpinion(@PathVariable id: Int, @RequestBody opinion: OpinionDataModel): ResponseEntity<OpinionDataModel> {
        val updatedOpinion = service.updateOpinion(id, opinion)
        return if (updatedOpinion != null) {
            ResponseEntity.ok(updatedOpinion)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteOpinion(@PathVariable id: Int): ResponseEntity<Void> {
        return if (service.getOpinionById(id) != null) {
            service.deleteOpinion(id)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
