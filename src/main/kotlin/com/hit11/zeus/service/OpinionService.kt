package com.obsidian.warhammer.service


import com.hit11.zeus.repository.OpinionRepository
import com.obsidian.warhammer.repository.model.OpinionDataModel
import org.springframework.stereotype.Service

@Service
class OpinionService(private val repository: OpinionRepository) {

    fun getAllActiveOpinions(matchId: Int): List<OpinionDataModel>? = repository.getAllActiveOpinionsByMatch(matchId)
}
