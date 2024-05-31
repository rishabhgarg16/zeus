package com.hit11.zeus.controller


import com.hit11.zeus.model.OpinionDataModel
import com.hit11.zeus.service.OpinionService

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/opinion")
class OpinionController(private val service: OpinionService) {

    @GetMapping("/active/{matchId}")
    fun getAllOpinions(@PathVariable("matchId") matchId: String): List<OpinionDataModel>? {
        return service.getAllActiveOpinions(matchId)
    }

//    @GetMapping("/{id}")
//    fun getOpinionById(@PathVariable id: Int): ResponseEntity<OpinionDataModel> {
//        val opinion = service.getOpinionById(id)
//        return if (opinion != null) {
//            ResponseEntity.ok(opinion)
//        } else {
//            ResponseEntity.notFound().build()
//        }
//    }

//    @PostMapping
//    fun createOpinion(@RequestBody opinion: OpinionDataModel): ResponseEntity<OpinionDataModel> {
//        val savedOpinion = service.saveOpinion(opinion)
//        return ResponseEntity.status(HttpStatus.CREATED).body(savedOpinion)
//    }

//    @PutMapping("/{id}")
//    fun updateOpinion(@PathVariable id: Int, @RequestBody opinion: OpinionDataModel): ResponseEntity<OpinionDataModel> {
//        val updatedOpinion = service.updateOpinion(id, opinion)
//        return if (updatedOpinion != null) {
//            ResponseEntity.ok(updatedOpinion)
//        } else {
//            ResponseEntity.notFound().build()
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    fun deleteOpinion(@PathVariable id: Int): ResponseEntity<Void> {
//        return if (service.getOpinionById(id) != null) {
//            service.deleteOpinion(id)
//            ResponseEntity.noContent().build()
//        } else {
//            ResponseEntity.notFound().build()
//        }
//    }
}
