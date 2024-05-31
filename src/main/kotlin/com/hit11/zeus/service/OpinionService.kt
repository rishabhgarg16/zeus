package com.hit11.zeus.service

import com.hit11.zeus.model.OpinionDataModel
import com.hit11.zeus.repository.OpinionRepository
import org.springframework.stereotype.Service

@Service
class OpinionService(private val repository: OpinionRepository) {

    fun getAllActiveOpinions(matchId: String): List<OpinionDataModel>? {
      return  repository.getAllActiveOpinionsByMatch(matchId.toInt())
    }
}
